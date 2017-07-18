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
package com.github.cafdataprocessing.boilerplate;

import com.github.cafdataprocessing.boilerplate.config.WorkerBoilerplateConfiguration;
import com.github.cafdataprocessing.boilerplate.emailsegregation.EmailSegregation;
import com.github.cafdataprocessing.boilerplate.emailsegregation.EmailSignatureExtraction;
import com.github.cafdataprocessing.boilerplate.expressionprocessing.BoilerplateExpressionProcessor;
import com.github.cafdataprocessing.boilerplate.expressionprocessing.Processor;
import com.github.cafdataprocessing.boilerplate.expressionprocessing.RegexProcessor;
import com.github.cafdataprocessing.boilerplate.result.BoilerplateWorkerResponse;
import com.github.cafdataprocessing.boilerplate.result.ResultTranslator;
import com.github.cafdataprocessing.boilerplate.source.RuntimeInvalidTaskException;
import com.github.cafdataprocessing.boilerplate.source.RuntimeTaskRejectedException;
import com.github.cafdataprocessing.boilerplate.utils.CreateCaches;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Multimap;
import com.hpe.caf.api.Codec;
import com.hpe.caf.api.ConfigurationException;
import com.hpe.caf.api.ConfigurationSource;
import com.hpe.caf.api.worker.DataStore;
import com.hpe.caf.api.worker.DataStoreSource;
import com.hpe.caf.api.worker.InvalidTaskException;
import com.hpe.caf.api.worker.TaskRejectedException;
import com.hpe.caf.boilerplate.webcaller.ApiException;
import org.apache.commons.lang3.tuple.Pair;
import com.hpe.caf.boilerplate.webcaller.model.BoilerplateExpression;
import com.hpe.caf.boilerplate.webcaller.model.Tag;
import com.hpe.caf.util.ref.DataSource;
import com.hpe.caf.util.ref.DataSourceException;
import com.hpe.caf.util.ref.ReferencedData;
import com.github.cafdataprocessing.boilerplate.task.BoilerplateWorkerTask;
import com.github.cafdataprocessing.boilerplate.task.BoilerplateWorkerTaskFactory;
import com.github.cafdataprocessing.boilerplate.utils.SelectedEmail;
import com.github.cafdataprocessing.boilerplate.utils.SelectedEmailSignature;
import com.github.cafdataprocessing.boilerplate.utils.SelectedExpressions;
import com.github.cafdataprocessing.boilerplate.utils.SelectedItems;
import com.github.cafdataprocessing.boilerplate.utils.SelectedTag;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.hpe.caf.boilerplate.webcaller.ApiClient;
import com.hpe.caf.boilerplate.webcaller.api.BoilerplateApi;
import com.hpe.caf.worker.document.config.DocumentWorkerConfiguration;
import com.hpe.caf.worker.document.exceptions.DocumentWorkerTransientException;
import com.hpe.caf.worker.document.extensibility.DocumentWorker;
import com.hpe.caf.worker.document.model.Application;
import com.hpe.caf.worker.document.model.Document;
import com.hpe.caf.worker.document.model.HealthMonitor;
import com.sun.jersey.api.client.ClientHandlerException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.joda.time.Period;

/**
 * This is an example implementation of the DocumentWorker interface.
 * <p>
 * Implementing the DocumentWorker interface provides an easy way to efficiently integrate into the Data Processing pipeline. Documents
 * passing through the pipeline can be routed to the worker and enriched from an external source such as a database.
 * <p>
 * The example implementation simply does a lookup from an internal in-memory map.
 * <p>
 * If it would be more efficient to process multiple documents together then the BulkDocumentWorker interface can be implemented instead
 * of the DocumentWorker interface.
 */
public class WorkerBoilerplate implements DocumentWorker
{
    private String defaultReplacementText;
    private LoadingCache<org.apache.commons.lang3.tuple.Pair<String, Long>, Tag> tagCache;
    private LoadingCache<org.apache.commons.lang3.tuple.Pair<String, Long>, BoilerplateExpression> expressionCache;
    private LoadingCache<org.apache.commons.lang3.tuple.Pair<String, Long>, List<BoilerplateExpression>> tagExpressionCache;

    private List<BoilerplateExpression> expressions;
    private List<BoilerplateExpression> tags;

    private final DataStore dataStore;
    private final Codec codec;
    private final WorkerBoilerplateConfiguration config;
    private final DocumentWorkerConfiguration doucmentConfiguration;

    public WorkerBoilerplate(final Application application) throws ConfigurationException
    {
        this.dataStore = application.getService(DataStore.class);
        this.codec = application.getService(Codec.class);
        this.config = application.getService(ConfigurationSource.class)
            .getConfiguration(WorkerBoilerplateConfiguration.class);
        this.doucmentConfiguration = application.getService(ConfigurationSource.class)
            .getConfiguration(DocumentWorkerConfiguration.class);
    }

    /**
     * This method provides an opportunity for the worker to report if it has any problems which would prevent it processing documents
     * correctly. If the worker is healthy then it should simply return without calling the health monitor.
     *
     * @param healthMonitor used to report the health of the application
     */
    @Override
    public void checkHealth(final HealthMonitor healthMonitor)
    {
    }

    /**
     * Processes a single document.
     * <p>
     * This example implementation sets the values of the 'UNIQUE_ID' field based on the values of the 'REFERENCE' field. The references
     * are looked up in an internal in-memory map, and if any of them are present then the corresponding unique ids are set.
     * <p>
     * Obviously a real implementation would likely query a central database rather than having an in-memory map, and it would also
     * operate in bulk rather than a single document at a time as presented here.
     *
     * @param document the document to be processed. Fields can be added or removed from the document.
     * @throws InterruptedException if any thread has interrupted the current thread
     * @throws DocumentWorkerTransientException if the document could not be processed
     */
    @Override
    public void processDocument(final Document document) throws InterruptedException, DocumentWorkerTransientException
    {
        BoilerplateWorkerResponse workerResponse = new BoilerplateWorkerResponse();
        ExecutorService jepThreadPool = Executors.newSingleThreadExecutor();
        final BoilerplateWorkerTask task = BoilerplateWorkerTaskFactory.getTask(document);
        final SelectedItems selectedItems = task.getExpressions();
        final Processor processor = new RegexProcessor();

        try {
            validateSourceData(task.getSourceData());
            createCaches();
            this.expressions = validateExpressions(task.getExpressions(), task.getTenantId());
            this.tags = validateTags(task.getExpressions(), task.getTenantId());
            final int resultSizeThreshold = config.getResultSizeThreshold();
            if (selectedItems instanceof SelectedEmail) {
                workerResponse = executeEmailExpressions((SelectedEmail) selectedItems, jepThreadPool, dataStore, resultSizeThreshold,
                                                         codec, task);
            } else if (selectedItems instanceof SelectedEmailSignature) {
                workerResponse = extractSignatures((SelectedEmailSignature) selectedItems, jepThreadPool, dataStore, resultSizeThreshold,
                                                   codec, task);
            } else {
                workerResponse = executeExpressions(selectedItems, task, codec, dataStore, resultSizeThreshold, processor);
            }
        }catch (InvalidTaskException ex) {
            document.addFailure(BoilerplateWorkerConstants.FailureConstants.INVALID_TASK, "An error occured during processing: " + ex);
        } catch (TaskRejectedException ex) {
            document.addFailure(BoilerplateWorkerConstants.FailureConstants.TASK_REJECTED_EXCEPTION, "An error occured during processing: " + ex);
        }
        ResultTranslator.translate(document, workerResponse);

    }

    private BoilerplateWorkerResponse executeExpressions(final SelectedItems selectedItems, final BoilerplateWorkerTask task,
                                                         final Codec codec, final DataStore dataStore, final int resultSizeLimit,
                                                         final Processor processor)
        throws TaskRejectedException
    {
        BoilerplateExpressionProcessor expressionProcessor = new BoilerplateExpressionProcessor(task, processor, dataStore,
                                                                                                codec, defaultReplacementText,
                                                                                                resultSizeLimit);

        List<BoilerplateExpression> boilerplateExpressions = null;
        System.out.println("selectedItems instanceof SelectedExpressions: ");
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
     * @param selectedEmail Rules defining what the primary, secondary and tertiary content of the email is.
     * @param jepThreadPool A single thread pool that the Jep/Python code executes on.
     * @param dataStore The storage service data store.
     * @param datastoreReference The reference Id to use in the storage service.
     * @param resultSizeLimit The size limit in bytes that will be encoded into base64. Content over this limit will be saved to the data
     * store
     * @return A BoilerplateWorkerResponse with the separated key content.
     */
    private BoilerplateWorkerResponse executeEmailExpressions(final SelectedEmail selectedEmail, final ExecutorService jepThreadPool,
                                                              final DataStore dataStore, final int resultSizeLimit, final Codec codec,
                                                              final BoilerplateWorkerTask task)
        throws TaskRejectedException
    {
        EmailSegregation emailSegregation = new EmailSegregation(jepThreadPool, dataStore, task.getDataStorePartialReference(), resultSizeLimit);
        Multimap<String, ReferencedData> sourceData = task.getSourceData();
        DataSource dataSource = new DataStoreSource(dataStore, codec);
        BoilerplateWorkerResponse boilerplateWorkerResponse = new BoilerplateWorkerResponse();
        boilerplateWorkerResponse.setTaskResults(new HashMap<>());
        for (Map.Entry<String, ReferencedData> referencedDataEntry : sourceData.entries()) {
            try {
                String content = IOUtils.toString(referencedDataEntry.getValue().acquire(dataSource));
                boilerplateWorkerResponse.getTaskResults().put(referencedDataEntry.getKey(),
                                                               emailSegregation.retrieveKeyContent(content, selectedEmail.primaryContent,
                                                                                                   selectedEmail.secondaryContent,
                                                                                                   selectedEmail.tertiaryContent));
            } catch (DataSourceException e) {
                throw new TaskRejectedException("Failed to retrieve content from storage", e);
            } catch (IOException e) {
                throw new TaskRejectedException("Failed to read input stream from storage", e);
            }
        }

        return boilerplateWorkerResponse;
    }

    private BoilerplateWorkerResponse extractSignatures(final SelectedEmailSignature selectedSignature, final ExecutorService jepThreadPool,
                                                        final DataStore dataStore, final int resultSizeLimit,
                                                        final Codec codec, final BoilerplateWorkerTask task)
    {
        EmailSegregation emailSegregation = new EmailSegregation(jepThreadPool, dataStore, task.getDataStorePartialReference(), resultSizeLimit);
        return new EmailSignatureExtraction(task, codec, jepThreadPool, dataStore, task.getDataStorePartialReference(), resultSizeLimit)
            .extractSignatures(emailSegregation, selectedSignature);
    }

    private List<BoilerplateExpression> validateExpressions(SelectedItems selectedItems, String tenantId)
        throws InvalidTaskException, TaskRejectedException
    {
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

    private List<BoilerplateExpression> validateTags(SelectedItems selectedItems, String tenantId)
        throws InvalidTaskException, TaskRejectedException
    {
        List<BoilerplateExpression> expressions = null;

        if (selectedItems instanceof SelectedTag) {
            SelectedTag selectedTag = (SelectedTag) selectedItems;
            try {
                Tag tag = tagCache.get(Pair.of(tenantId, selectedTag.getTagId()));
                if (tag.getDefaultReplacementText() != null) {
                    defaultReplacementText = tag.getDefaultReplacementText();
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

    private static void validateSourceData(Multimap<String, ReferencedData> sourceData) throws InvalidTaskException
    {
        for (Map.Entry<String, ReferencedData> referencedDataEntry : sourceData.entries()) {
            String reference = referencedDataEntry.getValue().getReference();
            byte[] data = referencedDataEntry.getValue().getData();
            if ((reference == null || reference.isEmpty()) && (data == null)) {
                throw new InvalidTaskException("Task data reference is null or empty.");
            }
        }
    }
    
        /**
     * Method to initialise the boilerplate expression and tag caches.
     *
     * Both caches are keyed off a Pair of the Tenant Id to object Id
     *
     * @param expressionCache
     * @param tagCache
     * @param tagExpressionCache
     * @param config
     * @param threads
     */
    private final void createCaches()
    { int threads = doucmentConfiguration.getThreads();
        CacheLoader<Pair<String, Long>, BoilerplateExpression> expressionCacheLoader = new CacheLoader<Pair<String, Long>, BoilerplateExpression>()
        {
            @Override
            public BoilerplateExpression load(Pair<String, Long> projectIdExpressionIdPair) throws Exception
            {
                ApiClient apiClient = new ApiClient();
                apiClient.setApiKey(projectIdExpressionIdPair.getLeft());
                apiClient.setBasePath(config.getBaseUrl());
                BoilerplateApi boilerplateApi = new BoilerplateApi(apiClient);
                return boilerplateApi.getExpression(projectIdExpressionIdPair.getRight());
            }
        };
        Long cacheExpireTime = new Period(config.getCacheExpireTimePeriod()).toStandardDuration().getStandardSeconds();

        expressionCache = CacheBuilder.newBuilder().concurrencyLevel(threads)
            .expireAfterAccess(cacheExpireTime, TimeUnit.SECONDS).build(expressionCacheLoader);

        CacheLoader<Pair<String, Long>, Tag> tagCacheLoader = new CacheLoader<Pair<String, Long>, Tag>()
        {
            @Override
            public Tag load(Pair<String, Long> projectIdTagIdPair) throws ApiException
            {
                ApiClient apiClient = new ApiClient();
                apiClient.setApiKey(projectIdTagIdPair.getLeft());
                apiClient.setBasePath(config.getBaseUrl());
                BoilerplateApi boilerplateApi = new BoilerplateApi(apiClient);
                return boilerplateApi.getTag(projectIdTagIdPair.getRight());
            }
        };

        tagCache = CacheBuilder.newBuilder().concurrencyLevel(threads)
            .expireAfterAccess(cacheExpireTime, TimeUnit.SECONDS).build(tagCacheLoader);

        CacheLoader<Pair<String, Long>, List<BoilerplateExpression>> tagExpressionCacheLoader = new CacheLoader<Pair<String, Long>, List<BoilerplateExpression>>()
        {
            @Override
            public List<BoilerplateExpression> load(Pair<String, Long> projectIdTagIdPair) throws Exception
            {
                ApiClient apiClient = new ApiClient();
                apiClient.setApiKey(projectIdTagIdPair.getLeft());
                apiClient.setBasePath(config.getBaseUrl());
                BoilerplateApi boilerplateApi = new BoilerplateApi(apiClient);
                return boilerplateApi.getExpressionsByTagPaged(projectIdTagIdPair.getRight(), null, null);
            }
        };

        tagExpressionCache = CacheBuilder.newBuilder().concurrencyLevel(threads)
            .expireAfterAccess(cacheExpireTime, TimeUnit.SECONDS).build(tagExpressionCacheLoader);

    }
}
