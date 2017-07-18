/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development LP.
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
package com.github.cafdataprocessing.boilerplate.emailsegregation;

import com.github.cafdataprocessing.boilerplate.source.StringContentConcatenate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Class that parses the SelectedEmail expressions
 */
public class SelectedEmailExpressionParser {

    /**
     * Concatenates the emails defined by the supplied expression.
     *
     * @param expression      The expression to execute/
     * @param separatedEmails The list of emails in the change to select from.
     * @return The emails selected by the expression concatenated into a single string.
     * @throws EmailExpressionParserException
     */
    public String executeExpression(String expression, List<String> separatedEmails) throws EmailExpressionParserException {
        Set<Integer> indexes = parseExpression(expression, separatedEmails.size() - 1);
        return StringContentConcatenate.concatenateContent(indexes, separatedEmails);
    }

    /**
     * Parses the expression and builds a set of indexes the expression defines.
     *
     * @param expression    The expression to parse.
     * @param numOfMessages The total number of emails in the chain.
     * @return A set of the indexes of the emails the expression defines.
     * @throws EmailExpressionParserException
     */
    private Set<Integer> parseExpression(String expression, int numOfMessages) throws EmailExpressionParserException {
        List<Integer> range = new ArrayList<>();
        parse(expression, range, numOfMessages);

        if (range.size() > 2) {
            throw new EmailExpressionParserException("Expression should not contain more than two range");
        }
        return calculateIndexes(range);
    }

    /**
     * Parses the expression.
     *
     * @param currentExpression The current part of the expression to parse.
     * @param indexes           The current list of indexes.
     * @param numOfMessages     The total number of messages.
     */
    private void parse(String currentExpression, List<Integer> indexes, int numOfMessages) {
        String[] parts = currentExpression.split("\\.\\.");
        if (parts.length == 1) {
            String currentString = parts[0];
            int resolvedIndex;
            if (currentString.contains("-")) {
                parts = currentString.split("-");
                for (int i = 0; i < parts.length; i++) {
                    if (parts[i].equalsIgnoreCase("LAST")) {
                        parts[i] = String.valueOf(numOfMessages);
                    }
                }
                resolvedIndex = Integer.valueOf(parts[0]);
                for (int i = 1; i < parts.length; i++) {
                    resolvedIndex = resolvedIndex - Integer.valueOf(parts[i]);
                }

            } else {
                if (parts[0].equalsIgnoreCase("LAST")) {
                    resolvedIndex = numOfMessages;
                } else {
                    int index = Integer.valueOf(parts[0]);
                    resolvedIndex = index > numOfMessages ? numOfMessages : index;
                }
            }
            indexes.add(resolvedIndex < 0 ? 0 : resolvedIndex);
        } else {
            for (String subString : parts) {
                parse(subString, indexes, numOfMessages);
            }
        }
    }

    /**
     * Creates a set of indexes from the specified range.
     *
     * @param range A two element list containing the fist and last index as defined by the expression.
     * @return A set of every index within the range.
     * @throws EmailExpressionParserException
     */
    private Set<Integer> calculateIndexes(List<Integer> range) throws EmailExpressionParserException {
        Set indexes = new TreeSet<>();
        if (range.size() <= 1) {
            indexes.add(range.get(0));
        } else {
            Integer minRange = range.get(0);
            Integer maxRange = range.get(range.size() - 1) + 1;
            if (minRange > maxRange) {
                throw new EmailExpressionParserException("Maximum index cannot be smaller than the minimum.");
            }
            for (int i = minRange; i < maxRange; i++) {
                indexes.add(i);
            }
        }
        return indexes;
    }

}
