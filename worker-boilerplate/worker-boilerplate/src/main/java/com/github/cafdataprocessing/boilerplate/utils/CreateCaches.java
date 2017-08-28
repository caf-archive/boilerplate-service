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
package com.github.cafdataprocessing.boilerplate.utils;

import com.github.cafdataprocessing.boilerplate.config.WorkerBoilerplateConfiguration;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.hpe.caf.boilerplate.webcaller.ApiClient;
import com.hpe.caf.boilerplate.webcaller.ApiException;
import com.hpe.caf.boilerplate.webcaller.api.BoilerplateApi;
import com.hpe.caf.boilerplate.webcaller.model.BoilerplateExpression;
import com.hpe.caf.boilerplate.webcaller.model.Tag;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.Period;

/**
 *
 * @author mcgreeva
 */
public final class CreateCaches
{
    private CreateCaches()
    {
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
    public static final void createCaches(LoadingCache<Pair<String, Long>, BoilerplateExpression> expressionCache,
                                          LoadingCache<Pair<String, Long>, Tag> tagCache,
                                          LoadingCache<Pair<String, Long>, List<BoilerplateExpression>> tagExpressionCache,
                                          final WorkerBoilerplateConfiguration config, final int threads)
    {
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
