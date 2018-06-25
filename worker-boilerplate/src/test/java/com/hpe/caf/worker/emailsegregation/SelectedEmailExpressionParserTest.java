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
package com.hpe.caf.worker.emailsegregation;

import com.hpe.caf.worker.boilerplate.emailsegregation.EmailExpressionParserException;
import com.hpe.caf.worker.boilerplate.emailsegregation.SelectedEmailExpressionParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for the SelectedEmail expression parser
 */
public class SelectedEmailExpressionParserTest {

    List<String> emails = new ArrayList<>();
    public static String email1 = "email1";
    public static String email2 = "email2";
    public static String email3 = "email3";
    public static String email4 = "email4";
    public static String email5 = "email5";


    @Before
    public void setup() {
        emails.add(email1);
        emails.add(email2);
        emails.add(email3);
        emails.add(email4);
        emails.add(email5);
    }

    @Test
    public void testExpressionParse_0ToLast() throws EmailExpressionParserException {
        SelectedEmailExpressionParser expressionParser = new SelectedEmailExpressionParser();
        String concatenatedEmail = expressionParser.executeExpression("0..LAST", emails);
        Assert.assertEquals("Concatenated string should match", getExpectedString(email1, email2, email3, email4, email5), concatenatedEmail);
    }

    @Test
    public void testExpressionParse_JustLast() throws EmailExpressionParserException {
        SelectedEmailExpressionParser expressionParser = new SelectedEmailExpressionParser();
        String concatenatedEmail = expressionParser.executeExpression("LAST", emails);
        Assert.assertEquals("Concatenated string should match", getExpectedString(email5), concatenatedEmail);
    }

    @Test
    public void testExpressionParse_JustIndex() throws EmailExpressionParserException {
        SelectedEmailExpressionParser expressionParser = new SelectedEmailExpressionParser();
        String concatenatedEmail = expressionParser.executeExpression("3", emails);
        Assert.assertEquals("Concatenated string should match", getExpectedString(email4), concatenatedEmail);
        //Now check passing the last index instead of LAST also works.
        expressionParser = new SelectedEmailExpressionParser();
        concatenatedEmail = expressionParser.executeExpression("4", emails);
        Assert.assertEquals("Concatenated string should match", getExpectedString(email5), concatenatedEmail);
        //Finally check using an out of bounds index, select the last email.
        concatenatedEmail = expressionParser.executeExpression("100", emails);
        Assert.assertEquals("Concatenated string should match", getExpectedString(email5), concatenatedEmail);
    }

    @Test
    public void testExpressionParse_RangeIsTheSame() throws EmailExpressionParserException {
        SelectedEmailExpressionParser expressionParser = new SelectedEmailExpressionParser();
        String concatenatedEmail = expressionParser.executeExpression("1..1", emails);
        Assert.assertEquals("Concatenated string should match", getExpectedString(email2), concatenatedEmail);
    }

    @Test
    public void testExpressionParse_WithSubtraction() throws EmailExpressionParserException {
        SelectedEmailExpressionParser expressionParser = new SelectedEmailExpressionParser();
        String concatenatedEmail = expressionParser.executeExpression("0..LAST-2", emails);
        Assert.assertEquals("Concatenated string should match", getExpectedString(email1, email2, email3), concatenatedEmail);
        //Now check if subtraction works when the expressions creates a negative index. (Should resolve to 0)
        concatenatedEmail = expressionParser.executeExpression("0..LAST-10", emails);
        Assert.assertEquals("Concatenated string should match", getExpectedString(email1), concatenatedEmail);
        //Now check without using LAST
        concatenatedEmail = expressionParser.executeExpression("1..3-1", emails);
        Assert.assertEquals("Concatenated string should match", getExpectedString(email2, email3), concatenatedEmail);
        //Finally check without using subtraction on both index
        concatenatedEmail = expressionParser.executeExpression("1-1..3-1", emails);
        Assert.assertEquals("Concatenated string should match", getExpectedString(email1, email2, email3), concatenatedEmail);
    }

    @Test
    public void testExpressionParse_Exceptions(){
        //Test expression indexes in wrong order throws
        SelectedEmailExpressionParser expressionParser = new SelectedEmailExpressionParser();
        Boolean exceptionThrown = false;
        try {
            expressionParser.executeExpression("LAST..2", emails);
        } catch (EmailExpressionParserException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue("EmailExpressionParserException should have been thrown", exceptionThrown);
        //Now test supplying more two indexes throws
        exceptionThrown = false;
        try {
            expressionParser.executeExpression("2..3..6", emails);
        } catch (EmailExpressionParserException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue("EmailExpressionParserException should have been thrown", exceptionThrown);
    }

    private String getExpectedString(String... strings) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String currentString : strings) {
            stringBuilder.append(currentString);
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
