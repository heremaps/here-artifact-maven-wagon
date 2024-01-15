/*
 * Copyright (C) 2018-2024 HERE Europe B.V.
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
package com.here.platform.artifact.maven.wagon.resolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicStatusLine;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ArtifactServiceUrlLookupResolverTest {

  private static ProtocolVersion protocolVersion = new ProtocolVersion("http", 1, 0);

  @Test
  public void testSuccessfulResolution() throws IOException {
    CloseableHttpClient httpClientMock = mock(CloseableHttpClient.class);
    CloseableHttpResponse lookupResponseMock = mock(CloseableHttpResponse.class, RETURNS_DEEP_STUBS);
    when(lookupResponseMock.getStatusLine()).thenReturn(new BasicStatusLine(protocolVersion, 200, "OK"));
    when(lookupResponseMock.getEntity().getContent()).thenReturn(new ByteArrayInputStream("[{\"baseURL\": \"resolvedUrl\"}]".getBytes()));
    when(httpClientMock.execute(any())).thenReturn(lookupResponseMock);
    ArtifactServiceUrlLookupResolver resolver = new ArtifactServiceUrlLookupResolver(() -> httpClientMock, new ObjectMapper());
    String resolvedUrl = resolver.resolveArtifactServiceUrl("https://account.api.here.com/oauth2/token");
    assertEquals("resolvedUrl/artifact", resolvedUrl);
  }

  @Test(expected = RuntimeException.class)
  public void testNon2xxResponseFromLookupApiResolution() throws IOException {
    CloseableHttpClient httpClientMock = mock(CloseableHttpClient.class);
    CloseableHttpResponse lookupResponseMock = mock(CloseableHttpResponse.class, RETURNS_DEEP_STUBS);
    when(lookupResponseMock.getStatusLine()).thenReturn(new BasicStatusLine(protocolVersion, 500, "Server Error"));
    when(httpClientMock.execute(any())).thenReturn(lookupResponseMock);
    ArtifactServiceUrlLookupResolver resolver = new ArtifactServiceUrlLookupResolver(() -> httpClientMock, new ObjectMapper());
    String resolvedUrl = resolver.resolveArtifactServiceUrl("https://account.api.here.com/oauth2/token");
    assertEquals("resolvedUrl/artifact", resolvedUrl);
  }

}