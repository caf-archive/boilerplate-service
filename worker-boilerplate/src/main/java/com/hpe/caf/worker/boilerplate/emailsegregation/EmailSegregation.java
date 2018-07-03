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
package com.hpe.caf.worker.boilerplate.emailsegregation;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hpe.caf.api.worker.DataStore;
import com.hpe.caf.api.worker.DataStoreException;
import com.hpe.caf.api.worker.DataStoreSource;
import com.hpe.caf.api.worker.TaskFailedException;
import com.hpe.caf.util.ref.ReferencedData;
import com.hpe.caf.worker.boilerplate.DataStoreUtil;
import com.hpe.caf.worker.boilerplateshared.BoilerplateWorkerConstants;
import com.hpe.caf.worker.boilerplateshared.response.BoilerplateResult;
import com.hpe.caf.worker.emailsegregation.ContentSegregation;
import java.nio.charset.StandardCharsets;
import jep.JepException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 */
public class EmailSegregation {

    private ExecutorService jepThreadPool;
    private int resultSizeLimit;
    private DataStore dataStore;
    private String dataStoreReference;

    public EmailSegregation(ExecutorService jepThreadPool, DataStore dataStore, String dataStoreReference, int resultSizeLimit) {
        this.jepThreadPool = jepThreadPool;
        this.resultSizeLimit = resultSizeLimit;
        this.dataStore = dataStore;
        this.dataStoreReference = dataStoreReference;
    }

    /**
     * Retrieves the key content fields as defined by the supplied expressions.
     *
     * @param email               The email to retrieve key content from.
     * @param primaryExpression   The expression to define primary content.
     * @param secondaryExpression The expression to define secondary content.
     * @param tertiaryExpression  The expression to define tertiary content.
     * @return A BoilerplateResult with the primary, secondary and tertiary content added to the GroupedMatches Multi-map.
     */
    public BoilerplateResult retrieveKeyContent(String email, String primaryExpression, String secondaryExpression, String tertiaryExpression) {
        Multimap<String, ReferencedData> resultMap = HashMultimap.create();
        BoilerplateResult result = new BoilerplateResult();
        try {
            //send the email to the mailgun/talon library to mark the start of each individual message
            // and split the email into individual messages
            List<String> separatedEmails = getSeparatedEmails(email);

            String selectedContent = separateKeyContent(separatedEmails, primaryExpression);

            DataStoreUtil dataStoreUtil = new DataStoreUtil(dataStore,dataStoreReference,resultSizeLimit);

            byte[] contentBytes;
            if (selectedContent != null) {
                contentBytes = selectedContent.getBytes(StandardCharsets.UTF_8);
                resultMap.put(BoilerplateWorkerConstants.PRIMARY_CONTENT, dataStoreUtil.wrapOrStoreData(contentBytes));
            }

            selectedContent = separateKeyContent(separatedEmails, secondaryExpression);
            if (selectedContent != null) {
                contentBytes = selectedContent.getBytes(StandardCharsets.UTF_8);
                resultMap.put(BoilerplateWorkerConstants.SECONDARY_CONTENT, dataStoreUtil.wrapOrStoreData(contentBytes));
            }

            selectedContent = separateKeyContent(separatedEmails, tertiaryExpression);
            if (selectedContent != null) {
                contentBytes = selectedContent.getBytes(StandardCharsets.UTF_8);
                resultMap.put(BoilerplateWorkerConstants.TERTIARY_CONTENT, dataStoreUtil.wrapOrStoreData(contentBytes));
            }

        } catch (JepException e) {
            throw new TaskFailedException("Call to external Python Library failed.", e);
        } catch (EmailExpressionParserException e) {
            throw new TaskFailedException("Invalid SelectedEmail expression", e);
        } catch (InterruptedException | ExecutionException e) {
            throw new TaskFailedException("Python thread encountered an error.", e);
        } catch (DataStoreException e) {
            throw new TaskFailedException("Failed to store data.", e);
        }

        result.setGroupedMatches(resultMap);
        return result;
    }

    /**
     * Separates the given email into the individual messages.
     *
     * @param emailChain The email to separate.
     * @return A list of the individual messages.
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public List<String> getSeparatedEmails(String emailChain) throws ExecutionException, InterruptedException {
        List<Integer> indexes = getMessageIndexes(emailChain);
        return separatedIndividualMessages(emailChain, indexes);
    }

    /**
     * Separates out the content defined by the given expression from the supplied separatedEmails.
     *
     * @param separatedEmails The separatedEmails to separate content from.
     * @param expression      The expression to define the content.
     * @return The separated content or null if separatedEmails or expression is null.
     * @throws JepException
     * @throws EmailExpressionParserException
     */
    private String separateKeyContent(List<String> separatedEmails, String expression) throws JepException, EmailExpressionParserException {
        if (separatedEmails == null || expression == null) {
            return null;
        }

        //pass to parser
        SelectedEmailExpressionParser emailExpressionParser = new SelectedEmailExpressionParser();
        return emailExpressionParser.executeExpression(expression, separatedEmails);
    }

    /**
     * Splits the given message into individual messages based on the supplied indexes
     *
     * @param email   The email to be separated.
     * @param indexes The line numbers of the start of each message in the chain.
     * @return A list of the individual messages in the chain.
     */
    private List<String> separatedIndividualMessages(String email, List<Integer> indexes) {
        List<String> separatedEmails = new ArrayList<>();
        String[] emailLines = email.split("\n");
        int totalLines = emailLines.length;
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < totalLines; i++) {
            //If current line is start of new message, add previous message to list and start to build the new one.
            if (indexes.contains(i) && stringBuilder.length() > 0) {
                separatedEmails.add(stringBuilder.toString());
                //reset string builder
                stringBuilder.setLength(0);
            }
            stringBuilder.append(emailLines[i] + "\n");
        }
        //Add last message in builder
        separatedEmails.add(stringBuilder.toString());
        return separatedEmails;
    }

    /**
     * Submits the task to the Jep thread.
     *
     * @param email The email to separate.
     * @return The line numbers of hte start of each message in the chain.
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private List<Integer> getMessageIndexes(String email) throws ExecutionException, InterruptedException {
        Callable<List<Integer>> callPython = () -> ContentSegregation.splitEmail(email);
        Future<List<Integer>> futureResult = jepThreadPool.submit(callPython);
        List<Integer> indexes = futureResult.get();
        return indexes;
    }

}
