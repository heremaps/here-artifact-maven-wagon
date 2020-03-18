# Contributing Guide

## Introduction

`Artifact Wagon` is Maven Java project with standard directory layout:
- src/main/java
- src/main/resources
- src/test/java

## Build

The project uses [Maven](https://maven.apache.org/) build system and build instructions are stored 
in [pom.xml](./pom.xml). Please [download](https://maven.apache.org/download.cgi) and 
[install](https://maven.apache.org/install.html) Maven before running any instruction below.

In order to compile and package the source code run:
```bash
mvn package
```

The build jar can be found in `target` folder.

## Tests

[Junit](https://junit.org) and [Mockito](https://site.mockito.org/) are used for unit-tests.

Modules contain unit tests under path `/src/test/java`. `maven-surefire-plugin` is used for running unit 
tests and generating reports.

To run unit tests use next commands:

- `mvn test` to run all tests
- `mvn -Dtest=ClassTest test` to run separate test-class
- `mvn -Dtest=ClassTest#methodTest test` to run separate method of the test-class
- `mvn -Dtest="com/here/your_package/**" test` to run separate package

Reports can be found under `target/surefire-reports` folder.

## Code Standards

Code styles conventions :

- the Java class should have **Copyright Notice**:
```text
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
```
- the package name should start with `com.here.platform`
- the folder structure should reflect the package name
- `*` for `imports` should not be used in the Java-classes. Each import should be declared explicitly.
- it is recommended to use [Google Java Format](https://github.com/google/google-java-format) without any single change to it.
For this purpose configure your IDE with the plugin and enable it.
When enabled, it will replace the normal `Reformat Code` action, which can be triggered from the `Code` menu or with the `Ctrl-Alt-L` (by default) keyboard shortcut.

# Commit Signing

As part of filing a pull request we ask you to sign off the
[Developer Certificate of Origin](https://developercertificate.org/) (DCO) in each commit.
Any Pull Request with commits that are not signed off will be reject by the
[DCO check](https://probot.github.io/apps/dco/).

A DCO is lightweight way for a contributor to confirm that you wrote or otherwise have the right
to submit code or documentation to a project. Simply add `Signed-off-by` as shown in the example below
to indicate that you agree with the DCO.

An example signed commit message:

```
    README.md: Fix minor spelling mistake

    Signed-off-by: John Doe <john.doe@example.com>
```

Git has the `-s` flag that can sign a commit for you, see example below:

`$ git commit -s -m 'README.md: Fix minor spelling mistake'`

# Travis CI
All opened pull request are being tested by Travis CI before they can be merged to the target branch.
After the new code is pushed to `master` Travis will run the test suite again, build the artifacts and release them
to Maven Central repository. The job will automatically increase Artifact Wagon patch version during this process.
If you don't want your changes to trigger the release - you can add the `[skip release]` flag to your commit message,
e.g., `git commit -s -m "[skip release] Fixed proxy configuration"`. We recommend doing so in cases when you update
CI scripts or documentation like README.md and CONTRIBUTING.md.
