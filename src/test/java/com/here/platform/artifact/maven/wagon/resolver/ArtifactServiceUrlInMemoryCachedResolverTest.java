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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ArtifactServiceUrlInMemoryCachedResolverTest {

  @Test
  public void testCachingMultipleUrls(){
    ArtifactServiceUrlInMemoryCachedResolver resolver = new ArtifactServiceUrlInMemoryCachedResolver();
    resolver.afterUrlResolved("tokenUrl1", "resolvedUrl1");
    resolver.afterUrlResolved("tokenUrl2", "resolvedUrl2");
    assertEquals("resolvedUrl1", resolver.resolveArtifactServiceUrl("tokenUrl1"));
    assertEquals("resolvedUrl2", resolver.resolveArtifactServiceUrl("tokenUrl2"));
  }

}