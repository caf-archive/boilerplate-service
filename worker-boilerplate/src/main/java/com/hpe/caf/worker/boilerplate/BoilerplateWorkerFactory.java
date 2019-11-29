/*
 * Copyright 2017-2020 Micro Focus or one of its affiliates.
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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.hpe.caf.api.Codec;
import com.hpe.caf.api.ConfigurationSource;
import com.hpe.caf.api.HealthResult;
import com.hpe.caf.api.worker.*;
import com.hpe.caf.boilerplate.webcaller.ApiClient;
import com.hpe.caf.boilerplate.webcaller.ApiException;
import com.hpe.caf.boilerplate.webcaller.api.BoilerplateApi;
import com.hpe.caf.boilerplate.webcaller.model.BoilerplateExpression;
import com.hpe.caf.boilerplate.webcaller.model.Tag;
import com.hpe.caf.worker.AbstractWorkerFactory;
import com.hpe.caf.worker.boilerplateshared.BoilerplateWorkerConstants;
import com.hpe.caf.worker.boilerplateshared.BoilerplateWorkerTask;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.Period;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Jason.Quinn on 04/01/2016.
 */
public class BoilerplateWorkerFactory extends AbstractWorkerFactory<BoilerplateWorkerConfiguration, BoilerplateWorkerTask> {

    private ExecutorService jepThreadPool;

    //Cache of Boilerplate Expressions keyed off Tenant Id and Expression Id
    private static LoadingCache<Pair<String, Long>, BoilerplateExpression> expressionCache;
    //Cache of Boilerplate Tags keyed off Tenant Id and Tag Id
    private static LoadingCache<Pair<String, Long>, Tag> tagCache;
    //cache of Boilerplate Expressions keyed off Tenant Id and Tag Id. This cache is requires due to limitations with the LoadingCache
    //interface and to avoid writing our own implementation.
    private static LoadingCache<Pair<String, Long>, List<BoilerplateExpression>> tagExpressionCache;

    public BoilerplateWorkerFactory(ConfigurationSource configSource, DataStore dataStore, Codec codec, Class<BoilerplateWorkerConfiguration> configurationClass, Class<BoilerplateWorkerTask> taskClass) throws WorkerException {
        super(configSource, dataStore, codec, configurationClass, taskClass);
        jepThreadPool = Executors.newSingleThreadExecutor();
        createCaches();
    }

    @Override
    protected String getWorkerName() {
        return BoilerplateWorkerConstants.WORKER_NAME;
    }

    @Override
    protected int getWorkerApiVersion() {
        return BoilerplateWorkerConstants.WORKER_API_VERSION;
    }

    @Override
    protected Worker createWorker(BoilerplateWorkerTask boilerplateWorkerTask) throws TaskRejectedException, InvalidTaskException {
        return new BoilerplateWorker(boilerplateWorkerTask, getDataStore(), getConfiguration().getOutputQueue(),
                getConfiguration().getBaseUrl(), getCodec(), getConfiguration().getResultSizeLimit(),
                getConfiguration().getDefaultReplacementText(), jepThreadPool,
                expressionCache, tagCache, tagExpressionCache);
    }

    @Override
    public String getInvalidTaskQueue() {
        return getConfiguration().getOutputQueue();
    }

    @Override
    public int getWorkerThreads() {
        return getConfiguration().getThreads();
    }

    @Override
    public HealthResult healthCheck() {
        return HealthResult.RESULT_HEALTHY;
    }

    @Override
    public void shutdown() {
        jepThreadPool.shutdown();
    }

    /**
     * Method to initialise the boilerplate expression and tag caches.
     * <p/>
     * Both caches are keyed off a Pair of the Tenant Id to object Id
     */
    private final void createCaches() {
        CacheLoader<Pair<String, Long>, BoilerplateExpression> expressionCacheLoader = new CacheLoader<Pair<String, Long>, BoilerplateExpression>() {
            @Override
            public BoilerplateExpression load(Pair<String, Long> projectIdExpressionIdPair) throws Exception {
                ApiClient apiClient = new ApiClient();
                apiClient.setApiKey(projectIdExpressionIdPair.getLeft());
                apiClient.setBasePath(getConfiguration().getBaseUrl());
                BoilerplateApi boilerplateApi = new BoilerplateApi(apiClient);
                return boilerplateApi.getExpression(projectIdExpressionIdPair.getRight());
            }
        };
        Long cacheExpireTime = new Period(getConfiguration().getCacheExpireTimePeriod()).toStandardDuration().getStandardSeconds();

        expressionCache = CacheBuilder.newBuilder().concurrencyLevel(getWorkerThreads())
                .expireAfterAccess(cacheExpireTime, TimeUnit.SECONDS).build(expressionCacheLoader);

        CacheLoader<Pair<String, Long>, Tag> tagCacheLoader = new CacheLoader<Pair<String, Long>, Tag>() {
            @Override
            public Tag load(Pair<String, Long> projectIdTagIdPair) throws ApiException {
                ApiClient apiClient = new ApiClient();
                apiClient.setApiKey(projectIdTagIdPair.getLeft());
                apiClient.setBasePath(getConfiguration().getBaseUrl());
                BoilerplateApi boilerplateApi = new BoilerplateApi(apiClient);
                return boilerplateApi.getTag(projectIdTagIdPair.getRight());
            }
        };

        tagCache = CacheBuilder.newBuilder().concurrencyLevel(getWorkerThreads())
                .expireAfterAccess(cacheExpireTime, TimeUnit.SECONDS).build(tagCacheLoader);

        CacheLoader<Pair<String, Long>, List<BoilerplateExpression>> tagExpressionCacheLoader = new CacheLoader<Pair<String, Long>, List<BoilerplateExpression>>() {
            @Override
            public List<BoilerplateExpression> load(Pair<String, Long> projectIdTagIdPair) throws Exception {
                ApiClient apiClient = new ApiClient();
                apiClient.setApiKey(projectIdTagIdPair.getLeft());
                apiClient.setBasePath(getConfiguration().getBaseUrl());
                BoilerplateApi boilerplateApi = new BoilerplateApi(apiClient);
                return boilerplateApi.getExpressionsByTagPaged(projectIdTagIdPair.getRight(), null, null);
            }
        };

        tagExpressionCache = CacheBuilder.newBuilder().concurrencyLevel(getWorkerThreads())
                .expireAfterAccess(cacheExpireTime, TimeUnit.SECONDS).build(tagExpressionCacheLoader);

    }


}
