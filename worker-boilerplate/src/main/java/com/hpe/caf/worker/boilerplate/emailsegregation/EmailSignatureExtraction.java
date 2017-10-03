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
package com.hpe.caf.worker.boilerplate.emailsegregation;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.hpe.caf.api.Codec;
import com.hpe.caf.api.worker.DataStore;
import com.hpe.caf.api.worker.DataStoreException;
import com.hpe.caf.api.worker.DataStoreSource;
import com.hpe.caf.api.worker.TaskFailedException;
import com.hpe.caf.util.ref.DataSource;
import com.hpe.caf.util.ref.DataSourceException;
import com.hpe.caf.util.ref.ReferencedData;
import com.hpe.caf.worker.boilerplate.DataStoreUtil;
import com.hpe.caf.worker.boilerplate.StringContentConcatenate;
import com.hpe.caf.worker.boilerplateshared.BoilerplateWorkerConstants;
import com.hpe.caf.worker.boilerplateshared.BoilerplateWorkerTask;
import com.hpe.caf.worker.boilerplateshared.RedactionType;
import com.hpe.caf.worker.boilerplateshared.SelectedEmailSignature;
import com.hpe.caf.worker.boilerplateshared.response.BoilerplateResult;
import com.hpe.caf.worker.boilerplateshared.response.BoilerplateWorkerResponse;
import com.hpe.caf.worker.boilerplateshared.response.SignatureExtractStatus;
import com.hpe.caf.worker.emailsegregation.ContentSegregation;
import com.hpe.caf.worker.emailsegregation.EmailStructure;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Class to separate the signature from an email.
 */
public class EmailSignatureExtraction {

    private BoilerplateWorkerTask task;
    private Codec codec;
    private ExecutorService jepThreadPool;
    private DataStore dataStore;
    private String dataStoreReference;
    private int resultSizeLimit;

    /**
     * Constructor
     *
     * @param task               The BoilerplateWorkerTask to carry out.
     * @param codec              The implementation of the Codec.
     * @param jepThreadPool      The Thread Pool that the Jep Python interpreter runs on.
     * @param dataStore          The DataStore.
     * @param dataStoreReference The container id of the DataStore.
     * @param resultSizeLimit    The size limit in byte to serialize the result data instead of store in the DataStore.
     */
    public EmailSignatureExtraction(BoilerplateWorkerTask task, Codec codec, ExecutorService jepThreadPool,
                                    DataStore dataStore, String dataStoreReference, int resultSizeLimit) {
        this.task = task;
        this.codec = codec;
        this.jepThreadPool = jepThreadPool;
        this.dataStore = dataStore;
        this.dataStoreReference = dataStoreReference;
        this.resultSizeLimit = resultSizeLimit;
    }

    private static Logger logger = LoggerFactory.getLogger(EmailSignatureExtraction.class);

    /**
     * Extracts the signatures from the content of each field in the task.
     *
     * @param emailSegregation The email content segregation library.
     * @param signature        The SelectedEmailSignature that specifies the sender of the email.
     * @return The BoilerplateWorkerResponse with the email signatures extracted and if RedactionType.REMOVE
     * was set on the task the content with the signatures removed.
     */
    public BoilerplateWorkerResponse extractSignatures(EmailSegregation emailSegregation, SelectedEmailSignature signature) {
        Multimap<String, ReferencedData> sourceData = task.getSourceData();
        DataSource dataSource = new DataStoreSource(dataStore, codec);
        BoilerplateWorkerResponse boilerplateWorkerResponse = new BoilerplateWorkerResponse();
        boilerplateWorkerResponse.setTaskResults(new HashMap<>());
        for (Map.Entry<String, ReferencedData> referencedDataEntry : sourceData.entries()) {
            try {
                String content = IOUtils.toString(referencedDataEntry.getValue().acquire(dataSource));
                boilerplateWorkerResponse.getTaskResults().put(referencedDataEntry.getKey(), extractSignatureForField(content, signature, emailSegregation));
            } catch (DataSourceException e) {
                throw new TaskFailedException("Failed to retrieve content from storage.", e);
            } catch (IOException e) {
                throw new TaskFailedException("Failed to read input stream from storage.", e);
            } catch (InterruptedException | ExecutionException e) {
                throw new TaskFailedException("Jep Python thread encountered an error.", e);
            } catch (DataStoreException e) {
                throw new TaskFailedException("Failed to store content to storage.", e);
            }
        }

        return boilerplateWorkerResponse;
    }

    /**
     * Extracts the signature of the supplied email.
     *
     * @param email             The email to extract the signature from.
     * @param selectedSignature The SelectedEmailSignature that specified the sender.
     * @param emailSegregation  The EmailSegregation library.
     * @return A BoilerplateResult that contains the extracted signatures for this email
     * and if RedactionType.REMOVE specified the updated content with the signatures removed.
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws DataStoreException
     */
    private BoilerplateResult extractSignatureForField(String email, SelectedEmailSignature selectedSignature, EmailSegregation emailSegregation) throws ExecutionException, InterruptedException, DataStoreException {
        BoilerplateResult boilerplateResult = new BoilerplateResult();
        //Use linkedListMultimap to preserve insertion order.
        boilerplateResult.setGroupedMatches(LinkedListMultimap.create());

        List<String> separatedEmails = emailSegregation.getSeparatedEmails(email);
        for (int currentEmailIndex = 0; currentEmailIndex < separatedEmails.size(); currentEmailIndex++) {
            String currentEmail = separatedEmails.get(currentEmailIndex);
            EmailStructure emailStructure = splitSignatureFromBody(currentEmail);
            if (emailStructure.getSignature() == null) {
                logger.debug("Failed to find signature on first pass, now retrying with machine learning.");
                emailStructure = splitSignatureFromBody_MachineLearning(currentEmail, selectedSignature.sender);
            }
            //If extracted  a signature then add to result
            if (emailStructure.getSignature() != null) {
                boilerplateResult.getGroupedMatches().put(BoilerplateWorkerConstants.EXTRACTED_SIGNATURES, ReferencedData.getWrappedData(emailStructure.getSignature().getBytes()));
            }
            //replace the current message with the email without it's signature.
            separatedEmails.set(currentEmailIndex, emailStructure.getBody());
        }
        if (!boilerplateResult.getGroupedMatches().containsKey(BoilerplateWorkerConstants.EXTRACTED_SIGNATURES)) {
            logger.warn("Failed to detect any signatures for the current message.");
            boilerplateResult.setSignatureExtractStatus(SignatureExtractStatus.NO_SIGNATURES_EXTRACTED);
        } else {
            boilerplateResult.setSignatureExtractStatus(SignatureExtractStatus.SIGNATURES_EXTRACTED);
        }

        if (task.getRedactionType() == RedactionType.REMOVE) {
            addEmailBodyToResult(boilerplateResult, separatedEmails);
        }
        return boilerplateResult;
    }

    /**
     * Submits the email signature extraction task to the Jep thread pool.
     *
     * @param email The Email to extract the signature from.
     * @return An EmailStructure object, containing the email body and string in separate fields.
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private EmailStructure splitSignatureFromBody(String email) throws ExecutionException, InterruptedException {
        Callable<EmailStructure> callPython = () -> ContentSegregation.extractSignature(email);
        Future<EmailStructure> emailStructureFuture = jepThreadPool.submit(callPython);
        return emailStructureFuture.get();
    }

    /**
     * Submits the email signature extraction task to the Jep thread pool. In this instance the request is for the slower
     * but more reliable Machine Learning method. This is usually called if the default failed to find a signature.
     *
     * @param email  The Email to extract the signature from.
     * @param sender The sender's email address to aid in identifying the signature.
     * @return An EmailStructure object, containing the email body and string in separate fields.
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private EmailStructure splitSignatureFromBody_MachineLearning(String email, String sender) throws ExecutionException, InterruptedException {
        Callable<EmailStructure> callPython = () -> ContentSegregation.extractSignature_MachineLearning(email, sender);
        Future<EmailStructure> emailStructureFuture = jepThreadPool.submit(callPython);
        return emailStructureFuture.get();
    }


    /**
     * Concatenates the individual messages, with their signatures, removed in the email chain into a single string
     * and stores or serializes this data before adding to the Boilerplate Result.
     *
     * @param boilerplateResult       The current BoilerplateResult
     * @param emailsWithoutSignatures A List of the email bodies without their signatures.
     * @throws DataStoreException
     */
    private void addEmailBodyToResult(BoilerplateResult boilerplateResult, List<String> emailsWithoutSignatures) throws DataStoreException {
        DataStoreUtil dataStoreUtil = new DataStoreUtil(dataStore, dataStoreReference, resultSizeLimit);
        String concatenatedEmail = StringContentConcatenate.concatenateContent(emailsWithoutSignatures);
        if (boilerplateResult.getData() == null) {
            boilerplateResult.setData(new ArrayList<>());
        }
        boilerplateResult.getData().add(dataStoreUtil.wrapOrStoreData(concatenatedEmail.getBytes()));
    }


}
