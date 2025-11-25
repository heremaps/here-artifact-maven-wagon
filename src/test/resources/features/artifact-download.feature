Feature: Maven Clean Install on Sample Project

  Background:
    Given the sample project exists at "src/test/resources/test_project"

  Scenario: Successfully run mvn clean install with sample project
    When running "mvn clean install" on the sample project
    Then the build should complete successfully
    And the exit code should be 0
    And the target directory should contain the built artifact
