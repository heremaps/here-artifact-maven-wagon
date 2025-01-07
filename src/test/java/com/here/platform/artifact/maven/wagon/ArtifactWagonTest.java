/*
 * Copyright (C) 2018-2025 HERE Europe B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSE
 */
package com.here.platform.artifact.maven.wagon;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.util.EntityUtils;
import org.apache.maven.wagon.repository.Repository;
import org.apache.maven.wagon.resource.Resource;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.here.platform.artifact.maven.wagon.model.RegisterResponse;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ArtifactWagonTest {

  private static ProtocolVersion protocolVersion = new ProtocolVersion("http", 1, 0);
  private ArtifactWagon artifactWagon;
  private Repository repository;
  private Map<String, HttpResponse> responses;

  @Before
  public void setup() throws IllegalAccessException {
    responses = new HashMap<>();
    repository = new Repository("example-repo", "here+https://example.com/artifact");

    artifactWagon =
        new ArtifactWagon() {

          @Override
          String getDefaultArtifactServiceUrl() {
            return "https://artifact.api.platform.here.com/v1/artifact";
          }

          @Override
          public Repository getRepository() {
            return ArtifactWagonTest.this.repository;
          }

          @Override
          protected Properties loadHereProperties() {
            Properties properties = new Properties();
            properties.setProperty("here.user.id", "test-user-id");
            properties.setProperty("here.client.id", "test-client-id");
            properties.setProperty("here.access.key.id", "test-access-key-id");
            properties.setProperty("here.access.key.secret", "test-access-key-secret");
            properties.setProperty(
                "here.token.endpoint.url", "https://account.api.here.com/oauth2/token");

            return properties;
          }

          @Override
          protected CloseableHttpResponse execute(HttpUriRequest httpMethod) {
            HttpResponse response;
            String key = httpMethod.getMethod() + ":" + httpMethod.getURI().toString();
            if (responses.containsKey(key)) {
              response = responses.get(key);
            } else {
              response = mock(CloseableHttpResponse.class);
              when(response.getStatusLine())
                  .thenReturn(new BasicStatusLine(protocolVersion, 404, "Not Found"));
            }
            return (CloseableHttpResponse)
                Proxy.newProxyInstance(
                    getClass().getClassLoader(),
                    new Class[] {CloseableHttpResponse.class},
                    (proxy, method, args) -> {
                      final String mname = method.getName();
                      if (mname.equals("close")) {
                        EntityUtils.consume(((HttpResponse) proxy).getEntity());
                        return null;
                      } else {
                        try {
                          return method.invoke(response, args);
                        } catch (final InvocationTargetException ex) {
                          final Throwable cause = ex.getCause();
                          if (cause != null) {
                            throw cause;
                          } else {
                            throw ex;
                          }
                        }
                      }
                    });
          }
        };
  }

  @Test
  public void testStandardUrl() {
    assertEquals(
        "http://example.com/artifact",
        artifactWagon.getURL(new Repository("here-artifact", "http://example.com/artifact")));
  }

  @Test
  public void testGetHereUrls() {
    assertEquals(
        "http://example.com/artifact",
        artifactWagon.getURL(new Repository("here-artifact", "here+http://example.com/artifact")));
    assertEquals(
        "https://example.com/artifact",
        artifactWagon.getURL(new Repository("here-artifact", "here+https://example.com/artifact")));
  }

  @Test
  public void testGetUrlBasedOnPlaceholderProtocol() {
    assertEquals(
        "https://artifact.api.platform.here.com/v1/artifact",
        artifactWagon.getURL(
            new Repository("here-artifact", "here+artifact-service://artifact-service")));
  }

  @Test
  public void testGetStream() throws Exception {
    RegisterResponse registerResponse =
        new RegisterResponse(
            "com.example.group",
            "test-artifact",
            "hrn:here:artifact:::com.example.group:test-artifact",
            "hrn:here:artifact:::com.example.group",
            true);
    ObjectMapper objectMapper = new ObjectMapper();
    String content = objectMapper.writeValueAsString(registerResponse);
    responses.put(
        "GET:https://example.com/artifact/register/com.example.group/test-artifact",
        byteResponse(content.getBytes(StandardCharsets.UTF_8)));
    responses.put(
        "GET:here+https://example.com/artifact/hrn:here:artifact:::com.example.group:test-artifact:1.0/test-artifact.jar",
        byteResponse(content.getBytes(StandardCharsets.UTF_8)));

    Resource resource = new Resource("com.example.group/test-artifact/1.0/test-artifact.jar");
    String output =
        IOUtils.toString(artifactWagon.getInputStream(resource), StandardCharsets.UTF_8);
    assertEquals(content, output);
  }

  private HttpResponse byteResponse(byte[] response) {
    BasicHttpResponse httpResponse = new BasicHttpResponse(protocolVersion, 200, "Found");
    httpResponse.setEntity(new ByteArrayEntity(response, 0, response.length));
    return httpResponse;
  }
}
