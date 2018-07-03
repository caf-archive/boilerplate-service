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

import com.google.common.cache.LoadingCache;
import com.google.common.collect.Multimap;
import com.hpe.caf.api.Codec;
import com.hpe.caf.api.worker.*;
import com.hpe.caf.boilerplate.webcaller.ApiException;
import com.hpe.caf.boilerplate.webcaller.model.BoilerplateExpression;
import com.hpe.caf.boilerplate.webcaller.model.Tag;
import com.hpe.caf.util.ref.DataSource;
import com.hpe.caf.util.ref.DataSourceException;
import com.hpe.caf.util.ref.ReferencedData;
import com.hpe.caf.worker.AbstractWorker;
import com.hpe.caf.worker.boilerplate.emailsegregation.EmailSegregation;
import com.hpe.caf.worker.boilerplate.emailsegregation.EmailSignatureExtraction;
import com.hpe.caf.worker.boilerplate.expressionprocessing.BoilerplateExpressionProcessor;
import com.hpe.caf.worker.boilerplate.expressionprocessing.Processor;
import com.hpe.caf.worker.boilerplate.expressionprocessing.RegexProcessor;
import com.hpe.caf.worker.boilerplateshared.*;
import com.hpe.caf.worker.boilerplateshared.response.BoilerplateWorkerResponse;
import com.sun.jersey.api.client.ClientHandlerException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * Created by Jason.Quinn on 05/01/2016.
 */
public class BoilerplateWorker extends AbstractWorker<BoilerplateWorkerTask, BoilerplateWorkerResponse> {
    private String baseUrl;
    private Processor processor;
    private DataStore dataStore;
    private int resultSizeLimit;
    private String defaultReplacementText;

    private ExecutorService jepThreadPool;

    private LoadingCache<Pair<String, Long>, Tag> tagCache;
    private LoadingCache<Pair<String, Long>, BoilerplateExpression> expressionCache;
    private LoadingCache<Pair<String, Long>, List<BoilerplateExpression>> tagExpressionCache;

    private List<BoilerplateExpression> expressions;
    private List<BoilerplateExpression> tags;

    private static Logger logger = LoggerFactory.getLogger(BoilerplateWorker.class);

    public BoilerplateWorker(BoilerplateWorkerTask task, DataStore dataStore, String resultQueue, String baseUrl,
                             Codec codec, int resultSizeLimit, String defaultReplacementText,
                             ExecutorService jepThreadPool, LoadingCache<Pair<String, Long>, BoilerplateExpression> expressionCache,
                             LoadingCache<Pair<String, Long>, Tag> tagCache,
                             LoadingCache<Pair<String, Long>, List<BoilerplateExpression>> tagExpressionCache) throws InvalidTaskException, TaskRejectedException {
        super(task, resultQueue, codec);
        this.dataStore = dataStore;
        this.resultSizeLimit = resultSizeLimit;
        processor = new RegexProcessor();

        this.baseUrl = baseUrl;

        this.defaultReplacementText = defaultReplacementText;

        this.jepThreadPool = jepThreadPool;

        this.tagCache = tagCache;

        this.expressionCache = expressionCache;

        this.tagExpressionCache = tagExpressionCache;

        //  Validate boilerplate expression Ids.
        this.expressions = validateExpressions(getTask().getExpressions(), task.getTenantId());

        //  Validate boilerplate tags.
        this.tags = validateTags(getTask().getExpressions(), task.getTenantId());

        //  Validate source data.
        validateSourceData(task.getSourceData());
    }

    @Override
    public WorkerResponse doWork() throws InterruptedException, TaskRejectedException {

        BoilerplateWorkerResponse workerResponse = new BoilerplateWorkerResponse();
        SelectedItems selectedItems = getTask().getExpressions();
        if (selectedItems instanceof SelectedEmail) {
            workerResponse = executeEmailExpressions((SelectedEmail) selectedItems, jepThreadPool, dataStore, getTask().getDataStorePartialReference(), resultSizeLimit);
        } else if (selectedItems instanceof SelectedEmailSignature) {
            workerResponse = extractSignatures((SelectedEmailSignature) selectedItems, jepThreadPool, dataStore, getTask().getDataStorePartialReference(), resultSizeLimit);
        } else {
            workerResponse = executeExpressions(selectedItems);
        }

        return createSuccessResult(workerResponse);
    }


    @Override
    public String getWorkerIdentifier() {
        return BoilerplateWorkerConstants.WORKER_NAME;
    }

    @Override
    public int getWorkerApiVersion() {
        return BoilerplateWorkerConstants.WORKER_API_VERSION;
    }

    private BoilerplateWorkerResponse executeExpressions(SelectedItems selectedItems) throws TaskRejectedException {
        BoilerplateExpressionProcessor expressionProcessor = new BoilerplateExpressionProcessor(getTask(), processor, dataStore,
                getCodec(), defaultReplacementText, resultSizeLimit);

        List<BoilerplateExpression> boilerplateExpressions = null;
        if (selectedItems instanceof SelectedExpressions) {
            //  Use validated expressions.
            boilerplateExpressions = expressions;
        } else if (selectedItems instanceof SelectedTag) {
            //  Use validated tags.
            boilerplateExpressions = tags;
        }
        return expressionProcessor.processExpressions(boilerplateExpressions);
    }


    /**
     * Performs email key content segregation.
     *
     * @param selectedEmail      Rules defining what the primary, secondary and tertiary content of the email is.
     * @param jepThreadPool      A single thread pool that the Jep/Python code executes on.
     * @param dataStore          The storage service data store.
     * @param datastoreReference The reference Id to use in the storage service.
     * @param resultSizeLimit    The size limit in bytes that will be encoded into base64.
     *                           Content over this limit will be saved to the data store
     * @return A BoilerplateWorkerResponse with the separated key content.
     */
    private BoilerplateWorkerResponse executeEmailExpressions(SelectedEmail selectedEmail, ExecutorService jepThreadPool, DataStore dataStore, String datastoreReference, int resultSizeLimit) throws TaskRejectedException {
        EmailSegregation emailSegregation = new EmailSegregation(jepThreadPool, dataStore, datastoreReference, resultSizeLimit);
        BoilerplateWorkerTask task = getTask();
        Multimap<String, ReferencedData> sourceData = task.getSourceData();
        DataSource dataSource = new DataStoreSource(dataStore, getCodec());
        BoilerplateWorkerResponse boilerplateWorkerResponse = new BoilerplateWorkerResponse();
        boilerplateWorkerResponse.setTaskResults(new HashMap<>());
        for (Map.Entry<String, ReferencedData> referencedDataEntry : sourceData.entries()) {
            try {
                String content = IOUtils.toString(referencedDataEntry.getValue().acquire(dataSource), StandardCharsets.UTF_8);
                boilerplateWorkerResponse.getTaskResults().put(referencedDataEntry.getKey(), emailSegregation.retrieveKeyContent(content, selectedEmail.primaryContent, selectedEmail.secondaryContent, selectedEmail.tertiaryContent));
            } catch (DataSourceException e) {
                throw new TaskRejectedException("Failed to retrieve content from storage", e);
            } catch (IOException e) {
                throw new TaskRejectedException("Failed to read input stream from storage", e);
            }
        }

        return boilerplateWorkerResponse;
    }


    private BoilerplateWorkerResponse extractSignatures(SelectedEmailSignature selectedSignature, ExecutorService jepThreadPool, DataStore dataStore, String datastoreReference, int resultSizeLimit) {
        EmailSegregation emailSegregation = new EmailSegregation(jepThreadPool, dataStore, datastoreReference, resultSizeLimit);
        return new EmailSignatureExtraction(getTask(), getCodec(), jepThreadPool, dataStore, datastoreReference, resultSizeLimit).extractSignatures(emailSegregation, selectedSignature);
    }

    private List<BoilerplateExpression> validateExpressions(SelectedItems selectedItems, String tenantId) throws InvalidTaskException, TaskRejectedException {
        List<BoilerplateExpression> expressions = null;

        if (selectedItems instanceof SelectedExpressions) {
            try {
                expressions = ((SelectedExpressions) selectedItems)
                        .getExpressionIds()
                        .parallelStream()
                        .map(u -> {
                            try {
                                return expressionCache.get(Pair.of(tenantId, u));
                            } catch (ExecutionException e) {
                                //Check if execution exception wraps a boilerplate API exception.
                                if (e.getCause() instanceof ApiException) {
                                    if (((ApiException) e.getCause()).getCode() == 404) {
                                        throw new RuntimeInvalidTaskException(new InvalidTaskException(e.getMessage(), e.getCause()));
                                    }
                                }
                                //  Assume this is a transient error.
                                throw new RuntimeTaskRejectedException(new TaskRejectedException(e.getMessage(), e.getCause()));
                            }
                        })
                        .collect(Collectors.toList());
            } catch (RuntimeInvalidTaskException e) {
                throw e.getInvalidTaskException();
            } catch (RuntimeTaskRejectedException e) {
                throw e.getTaskRejectedException();
            }
        }

        return expressions;
    }

    private List<BoilerplateExpression> validateTags(SelectedItems selectedItems, String tenantId) throws InvalidTaskException, TaskRejectedException {
        List<BoilerplateExpression> expressions = null;

        if (selectedItems instanceof SelectedTag) {
            SelectedTag selectedTag = (SelectedTag) selectedItems;
            try {
                Tag tag = tagCache.get(Pair.of(tenantId, selectedTag.getTagId()));
                if (tag.getDefaultReplacementText() != null) {
                    this.defaultReplacementText = tag.getDefaultReplacementText();
                }
                expressions = tagExpressionCache.get(Pair.of(tenantId, selectedTag.getTagId()));
            } catch (ExecutionException e) {
                //Check if execution exception wraps a runtime or boilerplate API exception.
                if (e.getCause() instanceof RuntimeException) {
                    throw (RuntimeException) e.getCause();
                } else if (e.getCause() instanceof ApiException) {
                    if (((ApiException) e.getCause()).getCode() == 404) {
                        throw new InvalidTaskException(e.getMessage(), e.getCause());
                    }
                }
                //  Assume this is a transient error.
                throw new TaskRejectedException(e.getMessage(), e.getCause());
            } catch (ClientHandlerException e) {
                throw new TaskRejectedException("Transitory error encountered during tag retrieval", e);
            }
        }

        return expressions;
    }


    private static void validateSourceData(Multimap<String, ReferencedData> sourceData) throws InvalidTaskException{
        for (Map.Entry<String, ReferencedData> referencedDataEntry : sourceData.entries()) {
            String reference = referencedDataEntry.getValue().getReference();
            byte[] data = referencedDataEntry.getValue().getData();
            if ((reference == null || reference.isEmpty()) && (data == null)) {
                throw new InvalidTaskException("Task data reference is null or empty.");
            }
        }
    }
}
