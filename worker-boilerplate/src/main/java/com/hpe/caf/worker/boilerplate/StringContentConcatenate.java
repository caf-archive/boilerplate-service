/*
 * Copyright 2015-2017 EntIT Software LLC, a Micro Focus company.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hpe.caf.worker.boilerplate;

import java.util.List;
import java.util.Set;

/**
 */
public class StringContentConcatenate {


    /**
     * Concatenates the specified strings into a single string.
     *
     * @param indexes          The indexes of the strings to concatenate.
     * @param separatedContent The complete list of strings.
     * @return The concatenated emails.
     */
    public static String concatenateContent(Set<Integer> indexes, List<String> separatedContent) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Integer currentIndex : indexes) {
            stringBuilder.append(separatedContent.get(currentIndex));
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    /**
     * Concatenates the list of strings into a single string.
     *
     * @param separatedContent The separate strings to concatenate.
     * @return The concatenated string.
     */
    public static String concatenateContent(List<String> separatedContent) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String currentEmail : separatedContent) {
            stringBuilder.append(currentEmail);
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
