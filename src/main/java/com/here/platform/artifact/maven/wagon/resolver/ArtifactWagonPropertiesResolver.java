/*
 * Copyright (C) 2018-2020 HERE Europe B.V.
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
import com.here.platform.artifact.maven.wagon.RequestExecutor;
import com.here.platform.artifact.maven.wagon.model.LookupPlatformApisResponse;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Artifact Wagon properties resolver. Resolves schema hrn prefix and default artifact service url
 * based on here token url.
 */
public class ArtifactWagonPropertiesResolver {

  private static final String TOKEN_PROD_URL = "https://account.api.here.com/oauth2/token";

  private static final String TOKEN_STAGING_URL = "https://stg.account.api.here.com/oauth2/token";

  private static final String API_LOOKUP_PROD_URL = "https://api-lookup.data.api.platform.here.com/lookup/v1";

  private static final String API_LOOKUP_STAGING_URL = "https://api-lookup.data.api.platform.sit.here.com/lookup/v1";

  private static final String TOKEN_CN_PROD_URL = "https://account.hereapi.cn/oauth2/token";

  private static final String TOKEN_CN_STAGING_URL = "https://account.sit.hereapi.cn/oauth2/token";

  private static final String API_LOOKUP_CN_PROD_URL = "https://api-lookup.data.api.platform.hereolp.cn/lookup/v1/";

  private static final String API_LOOKUP_CN_STAGING_URL =
      "https://api-lookup.data.api.platform.in.hereolp.cn/lookup/v1/";

  // Regional domains are used until the services up on the target domains
  private static final String TOKEN_CN_REGIONAL_PROD_URL =
      "https://elb.cn-northwest-1.account.hereapi.cn/oauth2/token";

  private static final String TOKEN_CN_REGIONAL_STAGING_URL =
      "https://elb.cn-northwest-1.account.sit.hereapi.cn/oauth2/token";

  private static final Map<String, String> URL_MAPPING;

  static {
    Map<String, String> map = new HashMap<>();
    map.put(TOKEN_PROD_URL, API_LOOKUP_PROD_URL);
    map.put(TOKEN_STAGING_URL, API_LOOKUP_STAGING_URL);
    map.put(TOKEN_CN_PROD_URL, API_LOOKUP_CN_PROD_URL);
    map.put(TOKEN_CN_STAGING_URL, API_LOOKUP_CN_STAGING_URL);

    map.put(TOKEN_CN_REGIONAL_PROD_URL, API_LOOKUP_CN_PROD_URL);
    map.put(TOKEN_CN_REGIONAL_STAGING_URL, API_LOOKUP_CN_STAGING_URL);
    URL_MAPPING = Collections.unmodifiableMap(map);
  }


  private final RequestExecutor requestExecutor;

  private final ObjectMapper objectMapper;

  public ArtifactWagonPropertiesResolver(RequestExecutor requestExecutor, ObjectMapper objectMapper) {
    this.requestExecutor = requestExecutor;
    this.objectMapper = objectMapper;
  }

  /**
   * Resolves schema default artifact service url based on here token url.
   *
   * @param tokenUrl here token url
   * @return resolved default artifact service url
   */
  public String resolveArtifactServiceUrl(String tokenUrl) {
    String artifactApiLookupUrl = getApiLookupUrl(tokenUrl) + "/platform/apis/artifact/v1";
    HttpGet httpGet = new HttpGet(artifactApiLookupUrl);

    try {
      try (CloseableHttpResponse httpResponse = requestExecutor.apply(httpGet)) {
        StatusLine statusLine = httpResponse.getStatusLine();
        int status = statusLine.getStatusCode();
        if (status != HttpStatus.SC_OK) {
          throw new RuntimeException("Unable to resolve Artifact Service URL. Status: " + statusLine);
        }
        HttpEntity responseEntity = httpResponse.getEntity();
        return Stream.of(objectMapper.readValue(responseEntity.getContent(), LookupPlatformApisResponse[].class))
            .findFirst()
            .map(r -> r.getBaseURL() + "/artifact")
            .orElseThrow(() -> new RuntimeException("No Artifact Service URL found via Lookup API"));
      }
    } catch (IOException | HttpException exp) {
      String msg = String.format("Error during resolving Artifact Service URL: %s", exp.getMessage());
      throw new RuntimeException(msg, exp);
    }
  }

  private String getApiLookupUrl(String tokenUrl) {
    String endpoint = tokenUrl.trim();
    if (!URL_MAPPING.containsKey(endpoint)) {
      throw new RuntimeException("Unknown token endpoint: " + endpoint);
    }
    return URL_MAPPING.get(endpoint);
  }

}
