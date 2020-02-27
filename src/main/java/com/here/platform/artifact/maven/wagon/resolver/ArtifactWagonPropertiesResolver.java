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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Artifact Wagon properties resolver. Resolves schema hrn prefix and default artifact service url
 * based on here token url.
 */
public class ArtifactWagonPropertiesResolver {

  private static final String TOKEN_PROD_URL = "https://account.api.here.com/oauth2/token";
  private static final String TOKEN_STAGING_URL = "https://stg.account.api.here.com/oauth2/token";
  private static final String TOKEN_CN_PROD_URL =
      "https://elb.cn-northwest-1.account.hereapi.cn/oauth2/token";
  private static final String TOKEN_CN_STAGING_URL =
      "https://elb.cn-northwest-1.account.sit.hereapi.cn/oauth2/token";

  private static final String PROD_ARTIFACT_SERVICE_URL =
      "https://artifact.api.platform.here.com/v1/artifact";
  private static final String STAGING_ARTIFACT_SERVICE_URL =
      "https://artifact.api.platform.in.here.com/v1/artifact";
  private static final String CN_PROD_ARTIFACT_SERVICE_URL =
      "https://artifact.api.platform.hereolp.cn/v1/artifact";
  private static final String CN_STAGING_ARTIFACT_SERVICE_URL =
      "https://artifact.api.platform.in.hereolp.cn/v1/artifact";

  private static final Map<String, String> ARTIFACT_SERVICE_URLS_MAP =
      initializeArtifactServiceUrlsMap();

  /**
   * Resolves schema default artifact service url based on here token url.
   *
   * @param tokenUrl here token url
   * @return resolved default artifact service url
   */
  public String resolveArtifactServiceUrl(String tokenUrl) {
    return Optional.ofNullable(ARTIFACT_SERVICE_URLS_MAP.get(tokenUrl))
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    String.format("Unknown token endpoint: %s", tokenUrl)));
  }

  private static Map<String, String> initializeArtifactServiceUrlsMap() {
    HashMap<String, String> map = new HashMap<>();
    map.put(TOKEN_PROD_URL, PROD_ARTIFACT_SERVICE_URL);
    map.put(TOKEN_STAGING_URL, STAGING_ARTIFACT_SERVICE_URL);
    map.put(TOKEN_CN_PROD_URL, CN_PROD_ARTIFACT_SERVICE_URL);
    map.put(TOKEN_CN_STAGING_URL, CN_STAGING_ARTIFACT_SERVICE_URL);

    return map;
  }
}
