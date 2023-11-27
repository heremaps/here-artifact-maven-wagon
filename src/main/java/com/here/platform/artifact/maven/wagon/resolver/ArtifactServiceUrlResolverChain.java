/*
 * Copyright (C) 2018-2023 HERE Europe B.V.
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
import com.here.platform.artifact.maven.wagon.util.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.function.Supplier;

/**
 * Class responsible for resolving Artifact Service URL.
 * Implementation use 3 strategies to resolve URL: cached in memory, cached in temporary file, request to lookup API
 */
public class ArtifactServiceUrlResolverChain {

  private static final Logger LOG = LoggerFactory.getLogger(ArtifactServiceUrlResolverChain.class);

  private final List<ArtifactServiceUrlResolver> resolversChain;

  public ArtifactServiceUrlResolverChain(Supplier<CloseableHttpClient> httpClientFactory, ObjectMapper objectMapper) {
    this.resolversChain = Arrays.asList(
        new ArtifactServiceUrlInMemoryCachedResolver(),
        new ArtifactServiceUrlFileResolver(),
        new ArtifactServiceUrlLookupResolver(httpClientFactory, objectMapper)
    );
  }

  public String resolveArtifactServiceUrl(String tokenUrl) {
    String resolvedUrl = "";
    Stack<ArtifactServiceUrlResolver> usedResolvers = new Stack<>();
    for (ArtifactServiceUrlResolver artifactServiceUrlResolver : this.resolversChain) {
      LOG.debug("Resolving URL using " + artifactServiceUrlResolver.getClass());
      resolvedUrl = artifactServiceUrlResolver.resolveArtifactServiceUrl(tokenUrl);
      if (!StringUtils.isEmpty(resolvedUrl)) {
        LOG.debug("Resolved URL: " + resolvedUrl);
        break;
      }
      usedResolvers.push(artifactServiceUrlResolver);
    }
    if (!StringUtils.isEmpty(resolvedUrl) && !usedResolvers.isEmpty()) {
      populateResolvedUrl(usedResolvers, tokenUrl, resolvedUrl);
    }
    return resolvedUrl;
  }

  /**
   * Send resolved URL to all used resolvers in reverse order
   * @param usedResolvers
   * @param tokenUrl
   * @param resolvedUrl
   */
  private void populateResolvedUrl(Stack<ArtifactServiceUrlResolver> usedResolvers, String tokenUrl, String resolvedUrl) {
    for (ArtifactServiceUrlResolver usedResolver : usedResolvers) {
      usedResolver.afterUrlResolved(tokenUrl, resolvedUrl);
    }
  }

}
