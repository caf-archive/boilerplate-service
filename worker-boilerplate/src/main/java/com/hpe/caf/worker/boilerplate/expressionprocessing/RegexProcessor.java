/*
 * Copyright 2015-2018 Micro Focus or one of its affiliates.
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
package com.hpe.caf.worker.boilerplate.expressionprocessing;

import com.google.common.io.FileBackedOutputStream;
import com.hpe.caf.boilerplate.webcaller.model.BoilerplateExpression;
import com.hpe.caf.worker.boilerplateshared.RedactionType;
import com.hpe.caf.worker.boilerplateshared.response.BoilerplateMatch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Jason.Quinn on 08/12/2015.
 */
public class RegexProcessor implements Processor {
    public ProcessorResult match(InputStream stream, List<BoilerplateExpression> expressions, RedactionType redactionType, String defaultReplacementText, boolean setMatches) {
        ProcessorResult processorResult = new ProcessorResult();
        if (redactionType == RedactionType.DO_NOTHING && !setMatches) {
            return processorResult;
        }

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream,StandardCharsets.UTF_8));

        Map<BoilerplateExpression, Pattern> patterns = new LinkedHashMap<>();
        for (BoilerplateExpression expression : expressions) {
            patterns.put(expression, Pattern.compile(expression.getExpression()));
        }

        Set<BoilerplateMatch> matches = new HashSet<>();

        FileBackedOutputStream fileBackedOutputStream = null;
        if (redactionType != RedactionType.DO_NOTHING) {
            fileBackedOutputStream = new FileBackedOutputStream(1024 * 1024);
        }


        int amountToRetrieve = 1000;
        char[] half1;
        char[] half2 = new char[amountToRetrieve];
        int sizeReturned;
        try {
            bufferedReader.read(half2);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        do {
            half1 = half2;
            half2 = new char[amountToRetrieve];
            try {
                sizeReturned = bufferedReader.read(half2);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            //If all remaining data has been read from the stream and the stream wasn't already closed (sizeReturned is -1) when reading for the first buffer
            // remove the additional null characters from the buffer
            if (sizeReturned < amountToRetrieve && sizeReturned > -1) {
                half2 = Arrays.copyOf(half2, sizeReturned);
            }

            //now check for matches
            Map<Integer, Integer> positions = new HashMap<>();
            for (Map.Entry<BoilerplateExpression, Pattern> boilerplatePattern : patterns.entrySet()) {
                BoilerplateExpression boilerplateExpression = boilerplatePattern.getKey();
                Pattern pattern = boilerplatePattern.getValue();

                String mergedString = new String(half1) + new String(half2);
                Matcher matcher = pattern.matcher(mergedString);

                positions.clear();
                while (matcher.find()) {
                    matches.add(new BoilerplateMatch(boilerplateExpression.getId(), matcher.group()));

                    positions.put(matcher.start(), matcher.end());
                }

                if (redactionType != RedactionType.DO_NOTHING && positions.size() != 0) {
                    StringBuilder redactedText1 = new StringBuilder();
                    StringBuilder redactedText2 = new StringBuilder();
                    for (int i = 0; i < mergedString.length(); i++) {
                        StringBuilder builderToUse = i < half1.length
                                ? redactedText1
                                : redactedText2;

                        if (positions.containsKey(i)) {
                            i = positions.get(i) - 1;
                            if (redactionType == RedactionType.REPLACE) {
                                String replacementText = boilerplateExpression.getReplacementText() != null
                                        ? boilerplateExpression.getReplacementText()
                                        : (defaultReplacementText == null
                                        ? "<boilerplate content>"
                                        : defaultReplacementText);
                                builderToUse.append(replacementText);
                            }
                        } else {
                            char character = mergedString.charAt(i);
                            if (character != 0) {
                                builderToUse.append(character);
                            }
                        }
                    }
                    half1 = redactedText1.toString().toCharArray();
                    half2 = redactedText2.toString().toCharArray();
                }
            }
            writeToOutputStream(redactionType, fileBackedOutputStream, half1);

        } while (sizeReturned == amountToRetrieve);
        writeToOutputStream(redactionType, fileBackedOutputStream, half2);

        if (setMatches) {
            processorResult.setMatches(matches);
        }
        if (redactionType != RedactionType.DO_NOTHING) {
            if (matches.size() == 0) {
                processorResult.setInputStream(stream);
            } else {
                assert fileBackedOutputStream != null;
                try {
                    processorResult.setInputStream(fileBackedOutputStream.asByteSource().openBufferedStream());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return processorResult;
    }

    private void writeToOutputStream(RedactionType redactionType, FileBackedOutputStream fileBackedOutputStream, char[] half1) {
        if (redactionType != RedactionType.DO_NOTHING) {
            try {
                assert fileBackedOutputStream != null;
                fileBackedOutputStream.write(new String(half1).getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
