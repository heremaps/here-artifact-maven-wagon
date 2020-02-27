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

import java.net.URI;
import java.util.List;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.metadata.DefaultMetadata;
import org.eclipse.aether.metadata.Metadata;
import org.eclipse.aether.spi.connector.layout.RepositoryLayout;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HereRepositoryLayoutTest {

  private HereRepositoryLayout layout;

  @Before
  public void setup() {
    this.layout = new HereRepositoryLayout();
  }

  @Test
  public void testGetFileJar() {
    assertEquals(
        "com.example/test-schema/1.0/test-schema-1.0.jar",
        layout
            .getLocation(toArtifact("com.example", "test-schema", "1.0", "jar"), false)
            .getPath());
  }

  @Test
  public void testPutFileJar() {
    assertEquals(
        "com.example/test-schema/1.0/test-schema-1.0.jar",
        layout.getLocation(toArtifact("com.example", "test-schema", "1.0", "jar"), true).getPath());
  }

  @Test
  public void testGetFileNoExtension() {
    assertEquals(
        "com.example/test-schema/1.0/test-schema-1.0",
        layout.getLocation(toArtifact("com.example", "test-schema", "1.0", null), false).getPath());
  }

  @Test
  public void testPutFileNoExtension() {
    assertEquals(
        "com.example/test-schema/1.0/test-schema-1.0",
        layout.getLocation(toArtifact("com.example", "test-schema", "1.0", null), true).getPath());
  }

  @Test
  public void testGetMetadata() {
    assertEquals(
        "com.example/test-schema/1.0/metadata.xml",
        layout
            .getLocation(toMetadata("com.example", "test-schema", "1.0", "metadata.xml"), true)
            .getPath());
  }

  @Test
  public void testGetMetadataNoVersion() {
    assertEquals(
        "com.example/test-schema/NONE/metadata.xml",
        layout
            .getLocation(toMetadata("com.example", "test-schema", null, "metadata.xml"), true)
            .getPath());
  }

  @Test
  public void testChecksums() {
    Artifact artifact = toArtifact("com.example", "test-schema", "1.0", "xml");
    URI location = layout.getLocation(artifact, false);

    List<RepositoryLayout.Checksum> checksums = layout.getChecksums(artifact, false, location);
    for (RepositoryLayout.Checksum checksum : checksums) {
      String extension = checksum.getAlgorithm().replace("-", "").toLowerCase();
      assertEquals(location.getPath() + "." + extension, checksum.getLocation().toString());
    }
  }

  private Metadata toMetadata(String groupId, String artifactId, String version, String file) {
    return new DefaultMetadata(groupId, artifactId, version, file, Metadata.Nature.RELEASE);
  }

  private Artifact toArtifact(String groupId, String artifactId, String version, String extension) {
    return new DefaultArtifact(groupId, artifactId, extension, version);
  }
}
