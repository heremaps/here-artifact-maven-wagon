/*
 * Copyright (C) 2018-2026 HERE Europe B.V.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ArtifactWagonCredentialsLoadingTest {

  private static final String CREDENTIALS_FILE_PROPERTY = "hereCredentialsFile";

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private String originalUserHome;
  private String originalCredentialsFile;

  @Before
  public void setUp() throws IOException {
    // environment overrides cannot be unset from a test, so skip when they are present
    assumeTrue(System.getenv("HERE_CREDENTIALS_FILE") == null);
    assumeTrue(System.getenv("HERE_CREDENTIALS_STRING") == null);

    originalUserHome = System.getProperty("user.home");
    originalCredentialsFile = System.getProperty(CREDENTIALS_FILE_PROPERTY);

    File userHome = temporaryFolder.newFolder("home");
    File defaultCredentials = new File(userHome, ".here/credentials.properties");
    write(
        defaultCredentials,
        "here.client.id=default-client-id\n"
            + "here.user.id=default-user-id\n"
            + "here.access.key.id=default-access-key-id\n"
            + "here.access.key.secret=default-access-key-secret\n"
            + "here.token.endpoint.url=https://default.example.com/oauth2/token\n");
    System.setProperty("user.home", userHome.getAbsolutePath());
  }

  @After
  public void tearDown() {
    restore("user.home", originalUserHome);
    restore(CREDENTIALS_FILE_PROPERTY, originalCredentialsFile);
  }

  @Test
  public void testExplicitCredentialsFileIsNotOverriddenByDefaultFile() throws Exception {
    File jwkCredentials = temporaryFolder.newFile("credentials-jwk.properties");
    write(
        jwkCredentials,
        "here.auth.method=private_key_jwt\n"
            + "here.client.id=jwk-client-id\n"
            + "here.private.key=/path/to/private-key.pem\n"
            + "here.token.endpoint.url=https://jwk.example.com/oauth2/token\n");
    System.setProperty(CREDENTIALS_FILE_PROPERTY, jwkCredentials.getAbsolutePath());

    Properties properties = new ArtifactWagon().loadHereProperties();

    assertEquals("jwk-client-id", properties.getProperty("here.client.id"));
    assertEquals(
        "https://jwk.example.com/oauth2/token", properties.getProperty("here.token.endpoint.url"));
    assertNull(properties.getProperty("here.access.key.id"));
    assertNull(properties.getProperty("here.user.id"));
  }

  @Test
  public void testDefaultFileUsedWhenNoCredentialsProvided() throws Exception {
    System.clearProperty(CREDENTIALS_FILE_PROPERTY);

    Properties properties = new ArtifactWagon().loadHereProperties();

    assertEquals("default-client-id", properties.getProperty("here.client.id"));
  }

  @Test
  public void testDefaultFileUsedWhenExplicitFileIsMissing() throws Exception {
    System.setProperty(
        CREDENTIALS_FILE_PROPERTY, new File(temporaryFolder.getRoot(), "missing").getAbsolutePath());

    Properties properties = new ArtifactWagon().loadHereProperties();

    assertEquals("default-client-id", properties.getProperty("here.client.id"));
  }

  private static void write(File file, String content) throws IOException {
    file.getParentFile().mkdirs();
    Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
  }

  private static void restore(String key, String value) {
    if (value == null) {
      System.clearProperty(key);
    } else {
      System.setProperty(key, value);
    }
  }
}
