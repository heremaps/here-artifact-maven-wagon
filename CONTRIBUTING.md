# Artifact Service

## Introduction

`Artifact Wagon` is Maven project with standard directory layout:
- src/main/java
- src/main/resources
- src/test/java

## Tests

For testing purpose *Junit* and *Mockito* are used for unit-tests.
Modules contain unit tests under path `/src/test/java`.
`maven-surefire-plugin` is used for running unit tests and generating reports for tests. To run unit tests use next commands:

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
```
- the package name should start with `com.here.platform`
- the folder structure should reflect the package name
- `*` for `imports` should not be used in the Java-classes. Each import should be declared explicitly.
- it is recommended to use [Google Java Format](https://github.com/google/google-java-format) without any single change to it.
For this purpose configure your IDE with the plugin and enable it.
When enabled, it will replace the normal `Reformat Code` action, which can be triggered from the `Code` menu or with the `Ctrl-Alt-L` (by default) keyboard shortcut.