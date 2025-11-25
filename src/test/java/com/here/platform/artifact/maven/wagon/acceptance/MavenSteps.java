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

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * Common step definitions shared across multiple feature files.
 */
public class MavenSteps {

  private static final Logger LOG = LoggerFactory.getLogger(MavenSteps.class);

  private final TestContext testContext;

  public MavenSteps() {
    this.testContext = new TestContext();
  }

  @Given("valid HERE credentials are configured")
  public void validHERECredentialsAreConfigured() {
    testContext.configureValidCredentials();
  }

  @Given("the sample project exists at {string}")
  public void theSampleProjectExistsAt(String path) {
    File projectDir = new File(path);

    if (!projectDir.exists()) {
      projectDir = new File(path);
    }

    assertTrue("Sample project should exist at: " + projectDir.getAbsolutePath(),
        projectDir.exists());
    assertTrue("Sample project should have pom.xml",
        new File(projectDir, "pom.xml").exists());

    testContext.setSampleProjectPath(projectDir.getAbsolutePath());
  }

  @When("running {string} on the sample project")
  public void iRunOnTheSampleProject(String command) throws Exception {
    String projectDir = testContext.getSampleProjectPath();
    String mavenCmd = testContext.getMavenCommand();

    String[] cmdArray = (mavenCmd + " " + command.replace("mvn ", "")).split(" ");

    ProcessBuilder processBuilder = new ProcessBuilder(cmdArray);
    processBuilder.directory(new File(projectDir));
    processBuilder.redirectErrorStream(true);

    Process process = processBuilder.start();

    StringBuilder output = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(process.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line)
            .append("\n");
        LOG.info(line);
      }
    }

    int exitCode = process.waitFor();
    String buildOutput = output.toString();

    testContext.setBuildOutput(buildOutput);
    testContext.setExitCode(exitCode);
  }

  @And("the deployment should succeed without failures")
  public void theDeploymentShouldSucceedWithoutFailures() {
    String output = testContext.getBuildOutput();

    assertFalse("Build output should not contain deployment failures",
        output.toLowerCase()
            .contains("deployment failed"));
    assertFalse("Build output should not contain upload failures",
        output.toLowerCase()
            .contains("failed to deploy"));

    assertTrue("Deployment should complete successfully",
        output.contains("BUILD SUCCESS"));
  }

  @And("the target directory should contain the built artifact")
  public void theTargetDirectoryShouldContainTheBuiltArtifact() {
    String projectDir = testContext.getSampleProjectPath();
    Path targetDir = Paths.get(projectDir, "target");

    assertTrue("Target directory should exist", Files.exists(targetDir));

    File[] jarFiles = targetDir.toFile()
        .listFiles((dir, name) -> name.endsWith(".jar"));
    assertNotNull("JAR files should be present", jarFiles);
    assertTrue("At least one JAR file should be present in target directory",
        jarFiles.length > 0);
  }

  @Then("the exit code should be {int}")
  public void theExitCodeShouldBe(int expectedCode) {
    int actualCode = testContext.getExitCode();
    assertEquals("Exit code should be " + expectedCode, expectedCode, actualCode);
  }

  @Then("the build should complete successfully")
  public void theBuildShouldCompleteSuccessfully() {
    String output = testContext.getBuildOutput();
    assertTrue("Build output should contain BUILD SUCCESS",
        output.contains("BUILD SUCCESS"));
    assertFalse("Build output should not contain BUILD FAILURE",
        output.contains("BUILD FAILURE"));
  }

}

