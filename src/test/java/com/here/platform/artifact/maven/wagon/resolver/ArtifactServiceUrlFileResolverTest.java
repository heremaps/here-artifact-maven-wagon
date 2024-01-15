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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class ArtifactServiceUrlFileResolverTest {

  @Before
  public void cleanupCacheFile() {
    File file = new File(System.getProperty("java.io.tmpdir"), "artifact_wagon_url_cache.properties");
    file.delete();
  }

  @Test
  public void testResolutionWhenFileDoNotExistsFromFile() {
    ArtifactServiceUrlFileResolver resolver = new ArtifactServiceUrlFileResolver();
    String resolvedUrl = resolver.resolveArtifactServiceUrl("https://account.api.here.com/oauth2/token");
    Assert.assertEquals("", resolvedUrl);
  }

  @Test
  public void testSuccessfulResolutionFromFile() {
    ArtifactServiceUrlFileResolver resolver = new ArtifactServiceUrlFileResolver();
    resolver.afterUrlResolved("https://account.api.here.com/oauth2/token", "https://artifact.api.platform.here.com/");
    resolver.afterUrlResolved("https://stg.account.api.here.com/oauth2/token", "https://artifact.api.platform.sit.here.com/");
    Assert.assertEquals("https://artifact.api.platform.here.com/", resolver.resolveArtifactServiceUrl("https://account.api.here.com/oauth2/token"));
    Assert.assertEquals("https://artifact.api.platform.sit.here.com/", resolver.resolveArtifactServiceUrl("https://stg.account.api.here.com/oauth2/token"));
  }

}