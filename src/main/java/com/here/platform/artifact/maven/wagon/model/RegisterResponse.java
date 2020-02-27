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
package com.here.platform.artifact.maven.wagon.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RegisterResponse {

  private final String groupId;
  private final String artifactId;
  private final String hrnPrefix;
  private final String groupHrnPrefix;
  private final boolean created;

  @JsonCreator
  public RegisterResponse(
      @JsonProperty("groupId") String groupId,
      @JsonProperty("artifactId") String artifactId,
      @JsonProperty("hrnPrefix") String hrnPrefix,
      @JsonProperty("groupHrnPrefix") String groupHrnPrefix,
      @JsonProperty("created") boolean created) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.hrnPrefix = hrnPrefix;
    this.groupHrnPrefix = groupHrnPrefix;
    this.created = created;
  }

  public String getGroupId() {
    return groupId;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public String getHrnPrefix() {
    return hrnPrefix;
  }

  public String getGroupHrnPrefix() {
    return groupHrnPrefix;
  }

  public boolean isCreated() {
    return created;
  }
}
