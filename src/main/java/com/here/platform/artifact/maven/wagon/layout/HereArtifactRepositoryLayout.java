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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.codehaus.plexus.component.annotations.Component;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Component(role = ArtifactRepositoryLayout.class, hint = "here")
public class HereArtifactRepositoryLayout extends DefaultRepositoryLayout {

  private static final char PATH_SEPARATOR = '/';
  private static final char GROUP_SEPARATOR = '.';
  private static final char ARTIFACT_SEPARATOR = '-';
  private static final int PATH_CAPACITY = 128;

  @Override
  public String getId() {
    return "here";
  }

  @Override
  public String pathOf(Artifact artifact) {
    ArtifactHandler artifactHandler = artifact.getArtifactHandler();

    StringBuilder path = new StringBuilder(PATH_CAPACITY);

    path.append(artifact.getGroupId()).append(PATH_SEPARATOR); // group as single element
    path.append(artifact.getArtifactId()).append(PATH_SEPARATOR);
    path.append(artifact.getBaseVersion()).append(PATH_SEPARATOR);
    path.append(artifact.getArtifactId()).append(ARTIFACT_SEPARATOR).append(artifact.getVersion());

    if (artifact.hasClassifier()) {
      path.append(ARTIFACT_SEPARATOR).append(artifact.getClassifier());
    }

    if (isNotEmpty(artifactHandler.getExtension())) {
      path.append(GROUP_SEPARATOR).append(artifactHandler.getExtension());
    }

    return path.toString();
  }

  @Override
  public String pathOfRemoteRepositoryMetadata(ArtifactMetadata metadata) {
    StringBuilder path = new StringBuilder(PATH_CAPACITY);

    path.append(metadata.getGroupId()).append(PATH_SEPARATOR);
    if (!metadata.storedInGroupDirectory()) {
      path.append(metadata.getArtifactId()).append(PATH_SEPARATOR);

      if (metadata.storedInArtifactVersionDirectory()) {
        path.append(metadata.getBaseVersion()).append(PATH_SEPARATOR);
      } else {
        path.append("NONE").append(PATH_SEPARATOR);
      }
    }

    path.append(metadata.getRemoteFilename());

    return path.toString();
  }
}
