/*
 * Copyright (C) 2015-2019 HERE Europe B.V.
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.metadata.Metadata;
import org.eclipse.aether.spi.connector.layout.RepositoryLayout;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Based on {@link
 * org.eclipse.aether.internal.impl.Maven2RepositoryLayoutFactory.Maven2RepositoryLayout}
 *
 * <p>This layout attempts to make URLs that look like: {groupId}/{artifact}/{version}/{file} for
 * easy translation into HRN format within the wagon provider.
 */
public class HereRepositoryLayout implements RepositoryLayout {

  private static final char PATH_SEPARATOR = '/';
  private static final char ARTIFACT_SEPARATOR = '-';
  private static final char EXTENSION_SEPARATOR = '.';
  private static final int LOCATION_CAPACITY = 128;

  public URI getLocation(Artifact artifact, boolean upload) {
    StringBuilder path = new StringBuilder(LOCATION_CAPACITY);

    // build segments
    path.append(artifact.getGroupId()).append(PATH_SEPARATOR);
    path.append(artifact.getArtifactId()).append(PATH_SEPARATOR);
    path.append(artifact.getBaseVersion()).append(PATH_SEPARATOR);

    path.append(artifact.getArtifactId()).append(ARTIFACT_SEPARATOR).append(artifact.getVersion());
    if (isNotEmpty(artifact.getClassifier())) {
      path.append(ARTIFACT_SEPARATOR).append(artifact.getClassifier());
    }

    if (isNotEmpty(artifact.getExtension())) {
      path.append(EXTENSION_SEPARATOR).append(artifact.getExtension());
    }

    return toUri(path.toString());
  }

  public URI getLocation(Metadata metadata, boolean upload) {
    if (metadata.getGroupId().isEmpty() || metadata.getArtifactId().isEmpty()) {
      throw new IllegalArgumentException("Invalid path for HERE layout: " + metadata);
    }

    StringBuilder path = new StringBuilder(LOCATION_CAPACITY);
    path.append(metadata.getGroupId()).append(PATH_SEPARATOR);
    path.append(metadata.getArtifactId()).append(PATH_SEPARATOR);
    path.append(defaultIfEmpty(metadata.getVersion(), "NONE")).append(PATH_SEPARATOR);
    path.append(metadata.getType());

    return toUri(path.toString());
  }

  public List<Checksum> getChecksums(Artifact artifact, boolean upload, URI location) {
    return getChecksums(location);
  }

  public List<Checksum> getChecksums(Metadata metadata, boolean upload, URI location) {
    return getChecksums(location);
  }

  private URI toUri(String path) {
    try {
      return new URI(null, null, path, null);
    } catch (URISyntaxException e) {
      throw new IllegalStateException(e);
    }
  }

  private List<Checksum> getChecksums(URI location) {
    return Arrays.asList(
        Checksum.forLocation(location, "SHA-1"), Checksum.forLocation(location, "MD5"));
  }
}
