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
package com.here.platform.artifact.maven.wagon.layout;

import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.internal.impl.Maven2RepositoryLayoutFactory;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.spi.connector.layout.RepositoryLayout;
import org.eclipse.aether.spi.connector.layout.RepositoryLayoutFactory;
import org.eclipse.aether.transfer.NoRepositoryLayoutException;

import javax.inject.Named;

@Named("here")
public class HereRepositoryLayoutFactory implements RepositoryLayoutFactory {

  private final Maven2RepositoryLayoutFactory mavenLayoutFactory;

  public HereRepositoryLayoutFactory() {
    this.mavenLayoutFactory = new Maven2RepositoryLayoutFactory();
  }

  @Override
  public RepositoryLayout newInstance(RepositorySystemSession session, RemoteRepository repository)
      throws NoRepositoryLayoutException {
    if ("here".equalsIgnoreCase(repository.getContentType())
        || repository.getProtocol().startsWith("here+")) {
      RemoteRepository defaultRepository = new RemoteRepository.Builder(repository)
          .setContentType("default")
          .build();
      RepositoryLayout repositoryLayout = mavenLayoutFactory.newInstance(session, defaultRepository);
      return (RepositoryLayout) HereRepositoryLayoutDecorator.newInstance(repositoryLayout);
    }

    return mavenLayoutFactory.newInstance(session, repository);
  }

  @Override
  public float getPriority() {
    return Float.MAX_VALUE;
  }
}
