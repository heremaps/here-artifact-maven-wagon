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

package com.here.platform.artifact.maven.wagon.acceptance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.util.Properties;

/**
 * Shared test context for Cucumber step definitions.
 * This class maintains state between different step definitions.
 */
public class TestContext {

  private static final Logger LOG = LoggerFactory.getLogger(TestContext.class);

  private String sampleProjectPath;

  private String buildOutput;

  private int exitCode;

  private Properties credentials;

  public void setSampleProjectPath(String path) {
    this.sampleProjectPath = path;
  }

  public String getSampleProjectPath() {
    return sampleProjectPath;
  }

  public String getMavenCommand() {
    String mavenHome = System.getenv("M2_HOME");
    if (mavenHome != null && !mavenHome.isEmpty()) {
      return mavenHome + "/bin/mvn";
    }

    return "mvn";
  }

  public void setBuildOutput(String output) {
    this.buildOutput = output;
  }

  public String getBuildOutput() {
    return buildOutput;
  }

  public void setExitCode(int code) {
    this.exitCode = code;
  }

  public int getExitCode() {
    return exitCode;
  }

  public void configureValidCredentials() {
    this.credentials = new Properties();

    String credentialsString = System.getenv("HERE_CREDENTIALS_STRING");
    String credentialsFile = System.getenv("HERE_CREDENTIALS_FILE");

    if (credentialsString != null && !credentialsString.isEmpty()) {
      try {
        credentials.load(new StringReader(credentialsString));
      } catch (Exception e) {
        LOG.error("Failed to load credentials from HERE_CREDENTIALS_STRING: {}", e.getMessage());
      }
    } else if (credentialsFile != null && !credentialsFile.isEmpty()) {
      try (FileInputStream fis = new FileInputStream(credentialsFile)) {
        credentials.load(fis);
      } catch (Exception e) {
        LOG.error("Failed to load credentials from {}: {}", credentialsFile, e.getMessage());
      }
    } else {
      String homeDir = System.getProperty("user.home");
      File defaultCredsFile = new File(homeDir, ".here/credentials.properties");
      if (defaultCredsFile.exists()) {
        try (FileInputStream fis = new FileInputStream(defaultCredsFile)) {
          credentials.load(fis);
        } catch (Exception e) {
          LOG.error("Failed to load credentials from default location: {}", e.getMessage());
        }
      }
    }
  }

}

