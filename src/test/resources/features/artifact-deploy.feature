Feature: Maven Clean Deploy on Sample Project

  Background:
    Given the sample project exists at "src/test/resources/test_project"
    And valid HERE credentials are configured

  Scenario: Successfully run mvn clean deploy with sample project
    When running "mvn clean deploy" on the sample project
    Then the build should complete successfully
    And the exit code should be 0
    And the deployment should succeed without failures
