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

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cucumber hooks for managing test lifecycle.
 * These hooks run before and after each scenario.
 */
public class Hooks {

  private static final Logger LOG = LoggerFactory.getLogger(Hooks.class);

  private final TestContext testContext;

  public Hooks() {
    this.testContext = new TestContext();
  }

  public Hooks(TestContext testContext) {
    this.testContext = testContext;
  }

  @Before
  public void beforeScenario(Scenario scenario) {
    LOG.info("Starting scenario: {}", scenario.getName());
    LOG.info("Testing with Java version: {}", System.getProperty("java.version"));
  }

  @After
  public void afterScenario(Scenario scenario) {
    LOG.info("Finished scenario: {} - Status: {}", scenario.getName(), scenario.getStatus());
    cleanupTempFiles();
  }

  private void cleanupTempFiles() {
    LOG.debug("Cleaning up temporary test files");
  }

}

