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
package com.hpe.caf.worker.boilerplate;

import com.google.common.base.Stopwatch;
import com.hpe.caf.api.worker.DataStoreException;
import com.hpe.caf.boilerplate.webcaller.model.BoilerplateExpression;
import com.hpe.caf.util.ref.DataSourceException;
import com.hpe.caf.worker.boilerplate.expressionprocessing.Processor;
import com.hpe.caf.worker.boilerplate.expressionprocessing.ProcessorResult;
import com.hpe.caf.worker.boilerplate.expressionprocessing.RegexProcessor;
import com.hpe.caf.worker.boilerplateshared.RedactionType;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by Jason.Quinn on 13/01/2016.
 */
public class RegexProcessorTest {

    private Processor regexProcessor = new RegexProcessor();

    @Test
    public void matchTest() throws IOException, DataSourceException, DataStoreException {

        String is = "test Ñ test1 test2 test3" +
                getRandomCharacters() +
                " test4 test5";

        List<BoilerplateExpression> expressions = getTestBoilerplateExpressions();

        ProcessorResult result = match(is, expressions, RedactionType.DO_NOTHING, true);

        Assert.assertEquals(6, result.getMatches().size());
    }

    @Test
    public void redactionTest() throws IOException, DataSourceException, DataStoreException {

        String is = "test Ñ test1 test2 test3" +
                this.getRandomCharacters() +
                " test4 test5";

        List<BoilerplateExpression> expressions = getTestBoilerplateExpressions();

        ProcessorResult result = match(is, expressions, RedactionType.REMOVE, true);

        Assert.assertEquals(5, result.getMatches().size());
        
        String newString = IOUtils.toString(result.getInputStream(), StandardCharsets.UTF_8);
        Assert.assertEquals("Redacted string not the correct size", is.length()
                - (5 * 4) - (2 * 3)//subtract removed text length
                , newString.length());
        Assert.assertTrue("Start does not match", newString.startsWith("t Ñ t1  "));
        Assert.assertTrue("End does not match", newString.endsWith("  "));
    }

    /**
     * Run this test multiple times as it exposed an issue will NULL characters.
     *
     * @throws DataSourceException
     * @throws IOException
     * @throws DataStoreException
     */
    @Test
    public void testRedactionLoads() throws DataSourceException, IOException, DataStoreException {
        for (int i = 0; i < 100; i++) {
            redactionTest();
        }
    }

    @Test
    public void redactionReplacementTest() throws IOException, DataSourceException, DataStoreException {

        String is = "test Ñ test1 test2 test3" +
                this.getRandomCharacters() +
                " test4 test5";

        List<BoilerplateExpression> expressions = getTestBoilerplateExpressions();

        ProcessorResult result = match(is, expressions, RedactionType.REPLACE, true);

        Assert.assertEquals(5, result.getMatches().size());

        String newString = IOUtils.toString(result.getInputStream(), StandardCharsets.UTF_8);

        Assert.assertEquals("Redacted string not the correct size", is.length()
                - (5 * 4) - (2 * 3)//subtract replaced text length
                + (6 * "<boilerplate content>".length()) //add replacement text length
                , newString.length());
        String start = "<boilerplate content>t Ñ <boilerplate content>t1 <boilerplate content> <boilerplate content> ";
        Assert.assertTrue("Start does not match", newString.startsWith(start));

        Assert.assertTrue("End does not match", newString.endsWith(" <boilerplate content> <boilerplate content>"));
    }

    /**
     * Checking that replacement happens correctly with multiple single character matchs
     *
     * @throws IOException
     * @throws DataSourceException
     * @throws DataStoreException
     */
    @Test
    public void redactionReplacementSingleCharTest() throws IOException, DataSourceException, DataStoreException {

        String is = "aaa";

        List<BoilerplateExpression> expressions = new ArrayList<>();
        {
            BoilerplateExpression expression = new BoilerplateExpression();
            expression.setExpression("a");
            expressions.add(expression);
        }

        ProcessorResult result = match(is, expressions, RedactionType.REPLACE, true);

        Assert.assertEquals(1, result.getMatches().size());

        String newString = IOUtils.toString(result.getInputStream());

        String targetText = "<boilerplate content><boilerplate content><boilerplate content>";
        Assert.assertEquals("Text does not match", targetText, newString);
    }

    /**
     * Gets random characters each separated by a space
     *
     * @return StringBuilder with the random characters
     */
    private StringBuilder getRandomCharacters() {
        return getRandomCharacters(1000000);
    }

    /**
     * Gets random characters separated by a space
     *
     * @param amount Amount of characters to generate
     * @return StringBuilder with the random characters
     */
    private StringBuilder getRandomCharacters(int amount) {
        StringBuilder stringBuilder = new StringBuilder();
        Random r = new Random();
        for (int i = 0; i < amount; i++) {
            stringBuilder.append(" ");
            //Ensure random char is not 0 because that will be removed during redaction
            stringBuilder.append((char) (r.nextInt(Character.MAX_VALUE - 1) + 1));
        }
        return stringBuilder;
    }

    private List<BoilerplateExpression> getTestBoilerplateExpressions() {
        List<BoilerplateExpression> expressions = new ArrayList<>();

        {
            BoilerplateExpression expression = new BoilerplateExpression();
            expression.setId(1L);
            expression.setExpression("te\\w*[2345]");
            expressions.add(expression);
        }
        {
            BoilerplateExpression expression = new BoilerplateExpression();
            expression.setId(2L);
            expression.setExpression("tes");
            expressions.add(expression);
        }
        {
            BoilerplateExpression expression = new BoilerplateExpression();
            expression.setId(3L);
            expression.setExpression("test1");
            expressions.add(expression);
        }
        return expressions;
    }

    private ProcessorResult match(String inputString, List<BoilerplateExpression> expressions, RedactionType redactionType, boolean setMatches) throws IOException, DataSourceException, DataStoreException {
        InputStream is = getInputStream(inputString);
        Stopwatch stopwatch = Stopwatch.createStarted();
        ProcessorResult result = regexProcessor.match(is, expressions, redactionType, "<boilerplate content>", setMatches);
        System.out.println("Time of execution in milliseconds match1:" + stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
        return result;
    }

    private InputStream getInputStream(String text) throws FileNotFoundException, DataSourceException, DataStoreException {
        return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
    }
}

