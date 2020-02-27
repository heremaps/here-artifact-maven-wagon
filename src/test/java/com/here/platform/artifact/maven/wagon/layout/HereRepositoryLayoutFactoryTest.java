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
package com.here.platform.artifact.maven.wagon.layout;

import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.spi.connector.layout.RepositoryLayout;
import org.eclipse.aether.transfer.NoRepositoryLayoutException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class HereRepositoryLayoutFactoryTest {

  private HereRepositoryLayoutFactory factory;
  private RepositorySystemSession repositorySystemSession;

  @Before
  public void setup() {
    factory = new HereRepositoryLayoutFactory();
    repositorySystemSession = mock(RepositorySystemSession.class);
  }

  @Test
  public void testCreateDefaultLayout() throws NoRepositoryLayoutException {
    RepositoryLayout layout =
        factory.newInstance(repositorySystemSession, repository("http://example.com", null));
    assertNotNull(layout);
    assertFalse(layout instanceof HereRepositoryLayout);
  }

  @Test
  public void testCreateDefaultLayoutHereUrl() throws NoRepositoryLayoutException {
    RepositoryLayout layout =
        factory.newInstance(repositorySystemSession, repository("here+http://example.com", null));
    assertNotNull(layout);
    assertTrue(layout instanceof HereRepositoryLayout);
  }

  @Test
  public void testCreateHereLayout() throws NoRepositoryLayoutException {
    RepositoryLayout layout =
        factory.newInstance(repositorySystemSession, repository("http://example.com", "here"));
    assertNotNull(layout);
    assertTrue(layout instanceof HereRepositoryLayout);
  }

  private RemoteRepository repository(String url, String layout) {
    return new RemoteRepository.Builder("test", null, url)
        .setContentType(layout != null ? layout : "default")
        .build();
  }
}
