/*
 * Copyright (C) 2015-2024 HERE Europe B.V.
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
package com.here.platform.artifact.maven.wagon.util;

/**
 * Helper class covering common String operations.
 */
public class StringUtils {

    /**
     * Checks whether the input string is null or empty.
     *
     * @param string the string to check
     * @return {@code true} if the string is null or empty, {@code false} otherwise.
     */
    public static boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }

    /**
     * Returns the passed string value of the default value if the former string is null or empty.
     *
     * @param string       the string to check
     * @param defaultValue default string value
     * @return the passed string value or default
     */
    public static String defaultIfEmpty(String string, String defaultValue) {
        return isEmpty(string) ? defaultValue : string;
    }
}