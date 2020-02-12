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
package com.here.platform.artifact.maven.wagon;

import java.util.Objects;

public class Artifact {

  private final String groupId;
  private final String artifactId;
  private final String version;
  private final String file;

  public Artifact(String groupId, String artifactId, String version, String file) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
    this.file = file;
  }

  public String getGroupId() {
    return groupId;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public String getVersion() {
    return version;
  }

  public String getFile() {
    return file;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Artifact artifact = (Artifact) o;
    return Objects.equals(groupId, artifact.groupId)
        && Objects.equals(artifactId, artifact.artifactId)
        && Objects.equals(version, artifact.version)
        && Objects.equals(file, artifact.file);
  }

  @Override
  public int hashCode() {
    return Objects.hash(groupId, artifactId, version, file);
  }

  @Override
  public String toString() {
    return String.format("%s:%s:%s:%s", groupId, artifactId, version, file);
  }
}
