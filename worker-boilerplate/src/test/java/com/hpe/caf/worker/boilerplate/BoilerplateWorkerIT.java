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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hpe.caf.api.CodecException;
import com.hpe.caf.api.worker.*;
import com.hpe.caf.boilerplate.webcaller.ApiClient;
import com.hpe.caf.boilerplate.webcaller.ApiException;
import com.hpe.caf.boilerplate.webcaller.api.BoilerplateApi;
import com.hpe.caf.boilerplate.webcaller.model.BoilerplateExpression;
import com.hpe.caf.boilerplate.webcaller.model.Tag;
import com.hpe.caf.codec.JsonCodec;
import com.hpe.caf.util.ref.DataSourceException;
import com.hpe.caf.util.ref.ReferencedData;
import com.hpe.caf.worker.boilerplateshared.BoilerplateWorkerTask;
import com.hpe.caf.worker.boilerplateshared.RedactionType;
import com.hpe.caf.worker.boilerplateshared.SelectedExpressions;
import com.hpe.caf.worker.boilerplateshared.SelectedTag;
import com.hpe.caf.worker.boilerplateshared.response.BoilerplateMatch;
import com.hpe.caf.worker.boilerplateshared.response.BoilerplateResult;
import com.hpe.caf.worker.boilerplateshared.response.BoilerplateWorkerResponse;
import com.sun.jersey.api.client.ClientHandlerException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by Jason.Quinn on 18/01/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class BoilerplateWorkerIT {
    private UUID projectId;
    private String connectionString;
    private BoilerplateApi boilerplateApi;
    JsonCodec codec = new JsonCodec();
    private static LoadingCache<Pair<String, Long>, BoilerplateExpression> expressionCache;
    private static LoadingCache<Pair<String, Long>, List<BoilerplateExpression>> tagExpressionCache;
    private static LoadingCache<Pair<String, Long>, Tag> tagCache;
    @Mock
    private DataStore dataStore;

    @Before
    public void setup() {
        projectId = UUID.randomUUID();
        connectionString = System.getenv("webserviceurl");
        dataStore = new TestDataStore();

        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(connectionString);
        apiClient.setApiKey(projectId.toString());
        boilerplateApi = new BoilerplateApi(apiClient);

        CacheLoader<Pair<String, Long>, BoilerplateExpression> expressionCacheLoader = new CacheLoader<Pair<String, Long>, BoilerplateExpression>() {
            @Override
            public BoilerplateExpression load(Pair<String, Long> projectIdExpressionIdPair) throws Exception {
                return boilerplateApi.getExpression(projectIdExpressionIdPair.getRight());
            }
        };
        Long cacheExpireTime = 60L;

        expressionCache = CacheBuilder.newBuilder().concurrencyLevel(1)
                .expireAfterAccess(cacheExpireTime, TimeUnit.SECONDS).build(expressionCacheLoader);

        CacheLoader<Pair<String, Long>, Tag> tagCacheLoader = new CacheLoader<Pair<String, Long>, Tag>() {
            @Override
            public Tag load(Pair<String, Long> projectIdTagIdPair) throws ApiException {
                return boilerplateApi.getTag(projectIdTagIdPair.getRight());
            }
        };

        tagCache = CacheBuilder.newBuilder().concurrencyLevel(1)
                .expireAfterAccess(cacheExpireTime, TimeUnit.SECONDS).build(tagCacheLoader);

        CacheLoader<Pair<String, Long>, List<BoilerplateExpression>> tagExpressionCacheLoader = new CacheLoader<Pair<String, Long>, List<BoilerplateExpression>>() {
            @Override
            public List<BoilerplateExpression> load(Pair<String, Long> projectIdTagIdPair) throws Exception {
                return boilerplateApi.getExpressionsByTagPaged(projectIdTagIdPair.getRight(), null, null);
            }
        };

        tagExpressionCache = CacheBuilder.newBuilder().concurrencyLevel(1)
                .expireAfterAccess(cacheExpireTime, TimeUnit.SECONDS).build(tagExpressionCacheLoader);
    }

    @Test
    public void testMatch() throws TaskRejectedException, InterruptedException, InvalidTaskException, DataStoreException, CodecException, IOException, DataSourceException {
        List<BoilerplateExpression> expressions = createBoilerplateExpressions();
        BoilerplateWorkerTask task = new BoilerplateWorkerTask();
        task.setTenantId(projectId.toString());
        SelectedExpressions selectedExpressions = new SelectedExpressions();
        selectedExpressions.setExpressionIds(expressions.stream().map(BoilerplateExpression::getId).collect(Collectors.toList()));
        task.setExpressions(selectedExpressions);

        Multimap<String, ReferencedData> sourceData = HashMultimap.create();
        ReferencedData referencedData = saveItem("test1 ÑNontese");
        sourceData.put("Content", referencedData);

        task.setSourceData(sourceData);
        task.setRedactionType(RedactionType.DO_NOTHING);

        BoilerplateWorker boilerplateWorker = createBoilerplateWorker(task);

        WorkerResponse workerResponse = boilerplateWorker.doWork();

        Assert.assertEquals(TaskStatus.RESULT_SUCCESS, workerResponse.getTaskStatus());

        BoilerplateWorkerResponse deserialise = codec.deserialise(workerResponse.getData(), BoilerplateWorkerResponse.class);
        Map<String, BoilerplateResult> taskResults = deserialise.getTaskResults();

        Collection<BoilerplateMatch> boilerplateMatches = taskResults.get("Content").getMatches();
        Assert.assertEquals(2, boilerplateMatches.size());

        Assert.assertEquals("Data should be null", null, taskResults.get("Content").getData());
    }

    @Test
    public void testReplace() throws TaskRejectedException, InterruptedException, InvalidTaskException, DataStoreException, CodecException, IOException, DataSourceException {
        replaceTest(null, "<boilerplate content>");
    }

    @Test
    public void testReplacementExpression() throws TaskRejectedException, InterruptedException, InvalidTaskException, DataStoreException, CodecException, DataSourceException, IOException {
        replaceTest("replaced", "replaced");
    }

    private void replaceTest(String expressionRedactText, String expectedRedactText) throws DataStoreException, InvalidTaskException, TaskRejectedException, InterruptedException, CodecException, DataSourceException, IOException {
        List<BoilerplateExpression> expressions = createBoilerplateExpressions(expressionRedactText);
        BoilerplateWorkerTask task = new BoilerplateWorkerTask();
        task.setTenantId(projectId.toString());
        SelectedExpressions selectedExpressions = new SelectedExpressions();
        selectedExpressions.setExpressionIds(expressions.stream().map(BoilerplateExpression::getId).collect(Collectors.toList()));
        task.setExpressions(selectedExpressions);

        Multimap<String, ReferencedData> sourceData = HashMultimap.create();
        sourceData.put("Content", saveItem("test1 ÑNontese"));

        task.setSourceData(sourceData);
        task.setRedactionType(RedactionType.REPLACE);

        BoilerplateWorker boilerplateWorker = createBoilerplateWorker(task);

        WorkerResponse workerResponse = boilerplateWorker.doWork();

        Assert.assertEquals(TaskStatus.RESULT_SUCCESS, workerResponse.getTaskStatus());

        BoilerplateWorkerResponse deserialise = codec.deserialise(workerResponse.getData(), BoilerplateWorkerResponse.class);
        Map<String, BoilerplateResult> taskResults = deserialise.getTaskResults();

        Collection<BoilerplateMatch> boilerplateMatches = taskResults.get("Content").getMatches();
        //test1 will be changes so only tes will match
        Assert.assertEquals(1, boilerplateMatches.size());

        ReferencedData content = taskResults.get("Content").getData().stream().findAny().get();

        DataStoreSource dataStoreSource = new DataStoreSource(dataStore, codec);
        InputStream acquired = content.acquire(dataStoreSource);

        String redactedString = IOUtils.toString(acquired, StandardCharsets.UTF_8);

        Assert.assertEquals(expectedRedactText + "t1 ÑNon" + expectedRedactText + "e", redactedString);
    }

    @Test
    public void tagReplaceTest() throws DataStoreException, InvalidTaskException, TaskRejectedException, InterruptedException, CodecException, DataSourceException, IOException {
        List<BoilerplateExpression> expressions = createBoilerplateExpressions();
        String tagReplacementText = "tag replacement";
        Tag tag = createTag(expressions, tagReplacementText);
        BoilerplateWorkerTask task = new BoilerplateWorkerTask();
        task.setTenantId(projectId.toString());
        SelectedTag selectedTag = new SelectedTag();
        selectedTag.setTagId(tag.getId());
        task.setExpressions(selectedTag);

        Multimap<String, ReferencedData> sourceData = HashMultimap.create();
        sourceData.put("Content", saveItem("test1 ÑNontese"));

        task.setSourceData(sourceData);
        task.setRedactionType(RedactionType.REPLACE);

        BoilerplateWorker boilerplateWorker = createBoilerplateWorker(task);

        WorkerResponse workerResponse = boilerplateWorker.doWork();

        Assert.assertEquals(TaskStatus.RESULT_SUCCESS, workerResponse.getTaskStatus());

        BoilerplateWorkerResponse deserialise = codec.deserialise(workerResponse.getData(), BoilerplateWorkerResponse.class);
        Map<String, BoilerplateResult> taskResults = deserialise.getTaskResults();

        Collection<BoilerplateMatch> boilerplateMatches = taskResults.get("Content").getMatches();
        //test1 will be changes so only tes will match
        Assert.assertEquals(1, boilerplateMatches.size());

        ReferencedData content = taskResults.get("Content").getData().stream().findAny().get();

        DataStoreSource dataStoreSource = new DataStoreSource(dataStore, codec);
        InputStream acquired = content.acquire(dataStoreSource);

        String redactedString = IOUtils.toString(acquired, StandardCharsets.UTF_8);

        Assert.assertEquals(tagReplacementText + "t1 ÑNon" + tagReplacementText + "e", redactedString);
    }

    @Test
    public void testExpressionCacheErrorHandling() throws InvalidTaskException, DataStoreException, TaskRejectedException, InterruptedException {
        //Create new expression cache that throws errors
        LoadingCache<Pair<String, Long>, BoilerplateExpression> errorCache = CacheBuilder.newBuilder().expireAfterAccess(2L, TimeUnit.MINUTES)
                .build(new CacheLoader<Pair<String, Long>, BoilerplateExpression>() {
                    @Override
                    public BoilerplateExpression load(Pair<String, Long> stringLongPair) throws ApiException {
                        throw new ApiException("error message", 404, null, null);
                    }
                });

        BoilerplateWorkerTask task = new BoilerplateWorkerTask();
        task.setTenantId(projectId.toString());
        SelectedExpressions selectedExpressions = new SelectedExpressions();
        selectedExpressions.setExpressionIds(Arrays.asList(1l, 2l));
        task.setExpressions(selectedExpressions);

        Multimap<String, ReferencedData> sourceData = HashMultimap.create();
        sourceData.put("Content", saveItem("test1 ÑNontese"));

        task.setSourceData(sourceData);
        task.setRedactionType(RedactionType.REPLACE);

        BoilerplateWorker worker = null;

        //  Test expression cache error handling.
        InvalidTaskException exception = null;
        try {
            worker = new BoilerplateWorker(task, dataStore, "results", connectionString, new JsonCodec(), 1000, "<boilerplate content>", null,
                    errorCache, tagCache, tagExpressionCache);
        } catch (InvalidTaskException ite) {
            exception = ite;
        }
        Assert.assertNotNull("Exception should have been thrown", exception);
        Assert.assertEquals("Cause of exception should be an ApiException", ApiException.class, exception.getCause().getClass());
        Assert.assertEquals("Error code should be 404", 404L, ((ApiException) exception.getCause()).getCode());

        errorCache = CacheBuilder.newBuilder().build(new CacheLoader<Pair<String, Long>, BoilerplateExpression>() {
            @Override
            public BoilerplateExpression load(Pair<String, Long> stringLongPair) throws Exception {
                throw new ApiException("error message", 500, null, null);
            }
        });

        TaskRejectedException taskRejectedException = null;
        try {
            worker = new BoilerplateWorker(task, dataStore, "results", connectionString, new JsonCodec(), 1000, "<boilerplate content>", null,
                    errorCache, tagCache, tagExpressionCache);
        } catch (TaskRejectedException e) {
            taskRejectedException = e;
        }
        Assert.assertNotNull("Exception should have been thrown", taskRejectedException);
        Assert.assertEquals("Cause of exception should be an ApiException", ApiException.class, taskRejectedException.getCause().getClass());
        Assert.assertEquals("Error code should be 500", 500L, ((ApiException) taskRejectedException.getCause()).getCode());

        errorCache = CacheBuilder.newBuilder().build(new CacheLoader<Pair<String, Long>, BoilerplateExpression>() {
            @Override
            public BoilerplateExpression load(Pair<String, Long> stringLongPair) throws Exception {
                throw new Exception("error message");
            }
        });

        taskRejectedException = null;
        try {
            worker = new BoilerplateWorker(task, dataStore, "results", connectionString, new JsonCodec(), 1000, "<boilerplate content>", null,
                    errorCache, tagCache, tagExpressionCache);
        } catch (TaskRejectedException e) {
            taskRejectedException = e;
        }
        Assert.assertNotNull("Exception should have been thrown", taskRejectedException);

    }

    @Test
    public void testTagCacheErrorHandling() throws InvalidTaskException, DataStoreException, TaskRejectedException, InterruptedException {
        //Create new tag cache that throws errors
        LoadingCache<Pair<String, Long>, Tag> errorCache = CacheBuilder.newBuilder().expireAfterAccess(2L, TimeUnit.MINUTES)
                .build(new CacheLoader<Pair<String, Long>, Tag>() {
                    @Override
                    public Tag load(Pair<String, Long> stringLongPair) throws ApiException {
                        throw new ApiException("error message", 404, null, null);
                    }
                });

        BoilerplateWorkerTask task = new BoilerplateWorkerTask();
        task.setTenantId(projectId.toString());

        String tagReplacementText = "tag replacement";
        Tag tag = new Tag();
        tag.setName("Tag");
        tag.setDefaultReplacementText(tagReplacementText);
        tag.setBoilerplateExpressions(Arrays.asList(1l, 2l));
        tag.setProjectId(projectId.toString());
        task.setTenantId(projectId.toString());
        SelectedTag selectedTag = new SelectedTag();
        selectedTag.setTagId(1l);
        task.setExpressions(selectedTag);

        Multimap<String, ReferencedData> sourceData = HashMultimap.create();
        sourceData.put("Content", saveItem("test1 ÑNontese"));

        task.setSourceData(sourceData);
        task.setRedactionType(RedactionType.REPLACE);

        BoilerplateWorker worker = null;

        //  Test expression cache error handling.
        InvalidTaskException exception = null;
        try {
            worker = new BoilerplateWorker(task, dataStore, "results", connectionString, new JsonCodec(), 1000, "<boilerplate content>", null,
                    expressionCache, errorCache, tagExpressionCache);
        } catch (InvalidTaskException ite) {
            exception = ite;
        }
        Assert.assertNotNull("Exception should have been thrown", exception);
        Assert.assertEquals("Cause of exception should be an ApiException", ApiException.class, exception.getCause().getClass());
        Assert.assertEquals("Error code should be 404", 404L, ((ApiException) exception.getCause()).getCode());

        errorCache = CacheBuilder.newBuilder().build(new CacheLoader<Pair<String, Long>, Tag>() {
            @Override
            public Tag load(Pair<String, Long> stringLongPair) throws Exception {
                throw new ApiException("error message", 500, null, null);
            }
        });

        TaskRejectedException taskRejectedException = null;
        try {
            worker = new BoilerplateWorker(task, dataStore, "results", connectionString, new JsonCodec(), 1000, "<boilerplate content>", null,
                    expressionCache, errorCache, tagExpressionCache);
        } catch (TaskRejectedException e) {
            taskRejectedException = e;
        }
        Assert.assertNotNull("Exception should have been thrown", taskRejectedException);
        Assert.assertEquals("Cause of exception should be an ApiException", ApiException.class, taskRejectedException.getCause().getClass());
        Assert.assertEquals("Error code should be 500", 500L, ((ApiException) taskRejectedException.getCause()).getCode());

        errorCache = CacheBuilder.newBuilder().build(new CacheLoader<Pair<String, Long>, Tag>() {
            @Override
            public Tag load(Pair<String, Long> stringLongPair) throws Exception {
                throw new Exception("error message");
            }
        });

        taskRejectedException = null;
        try {
            worker = new BoilerplateWorker(task, dataStore, "results", connectionString, new JsonCodec(), 1000, "<boilerplate content>", null,
                    expressionCache, errorCache, tagExpressionCache);
        } catch (TaskRejectedException e) {
            taskRejectedException = e;
        }
        Assert.assertNotNull("Exception should have been thrown", taskRejectedException);
    }

    @Test
    public void testInvalidSourceData_NullReference() throws TaskRejectedException {
        List<BoilerplateExpression> expressions = createBoilerplateExpressions();
        BoilerplateWorkerTask task = new BoilerplateWorkerTask();
        task.setTenantId(projectId.toString());
        SelectedExpressions selectedExpressions = new SelectedExpressions();
        selectedExpressions.setExpressionIds(expressions.stream().map(BoilerplateExpression::getId).collect(Collectors.toList()));
        task.setExpressions(selectedExpressions);

        Multimap<String, ReferencedData> sourceData = HashMultimap.create();
        sourceData.put("Content", ReferencedData.getReferencedData(null));

        task.setSourceData(sourceData);
        task.setRedactionType(RedactionType.DO_NOTHING);

        BoilerplateWorker boilerplateWorker = null;
        InvalidTaskException nullReferenceException = null;
        try {
            boilerplateWorker = createBoilerplateWorker(task);
        } catch (InvalidTaskException e) {
            nullReferenceException = e;
        }

        Assert.assertNotNull("InvalidTaskException should have been thrown because reference data is null", nullReferenceException);
    }

    @Test
    public void testInvalidSourceData_NullData() throws TaskRejectedException {
        List<BoilerplateExpression> expressions = createBoilerplateExpressions();
        BoilerplateWorkerTask task = new BoilerplateWorkerTask();
        task.setTenantId(projectId.toString());
        SelectedExpressions selectedExpressions = new SelectedExpressions();
        selectedExpressions.setExpressionIds(expressions.stream().map(BoilerplateExpression::getId).collect(Collectors.toList()));
        task.setExpressions(selectedExpressions);

        Multimap<String, ReferencedData> sourceData = HashMultimap.create();
        sourceData.put("Content", ReferencedData.getWrappedData(null));

        task.setSourceData(sourceData);
        task.setRedactionType(RedactionType.DO_NOTHING);

        BoilerplateWorker boilerplateWorker = null;
        InvalidTaskException nullDataException = null;
        try {
            boilerplateWorker = createBoilerplateWorker(task);
        } catch (InvalidTaskException e) {
            nullDataException = e;
        }

        Assert.assertNotNull("InvalidTaskException should have been thrown because reference data is null", nullDataException);
    }

    @Test
    public void testInvalidSourceData() throws DataStoreException, InvalidTaskException, InterruptedException, TaskRejectedException {
        List<BoilerplateExpression> expressions = createBoilerplateExpressions();
        BoilerplateWorkerTask task = new BoilerplateWorkerTask();
        task.setTenantId(projectId.toString());
        SelectedExpressions selectedExpressions = new SelectedExpressions();
        selectedExpressions.setExpressionIds(expressions.stream().map(BoilerplateExpression::getId).collect(Collectors.toList()));
        task.setExpressions(selectedExpressions);

        Multimap<String, ReferencedData> sourceData = HashMultimap.create();
        sourceData.put("Content", ReferencedData.getReferencedData("Invalid"));

        task.setSourceData(sourceData);
        task.setRedactionType(RedactionType.REPLACE);

        BoilerplateWorker boilerplateWorker = createBoilerplateWorker(task);

        TaskRejectedException exception = null;
        try {
            WorkerResponse workerResponse = boilerplateWorker.doWork();
        } catch (TaskRejectedException e) {
            exception = e;
        }
        Assert.assertNotNull("TaskRejectedException should have been thrown because invalid storage reference was supplied", exception);
    }

    private List<BoilerplateExpression> createBoilerplateExpressions() {
        return createBoilerplateExpressions(null);
    }

    private List<BoilerplateExpression> createBoilerplateExpressions(String replacementText) {
        List<BoilerplateExpression> expressions = new ArrayList<>();
        {
            BoilerplateExpression boilerplateExpression = new BoilerplateExpression();
            boilerplateExpression.setName("1");
            boilerplateExpression.setReplacementText(replacementText);
            boilerplateExpression.setExpression("te\\w*[2345]");
            expressions.add(create(boilerplateExpression));
        }
        {
            BoilerplateExpression boilerplateExpression = new BoilerplateExpression();
            boilerplateExpression.setName("2");
            boilerplateExpression.setReplacementText(replacementText);
            boilerplateExpression.setExpression("tes");
            expressions.add(create(boilerplateExpression));
        }
        {
            BoilerplateExpression boilerplateExpression = new BoilerplateExpression();
            boilerplateExpression.setName("3");
            boilerplateExpression.setReplacementText(replacementText);
            boilerplateExpression.setExpression("test1");
            expressions.add(create(boilerplateExpression));
        }
        return expressions;
    }

    private Tag createTag(List<BoilerplateExpression> expressions, String replacementText) {
        Tag tag = new Tag();
        tag.setName("Tag");
        tag.setDefaultReplacementText(replacementText);
        tag.setBoilerplateExpressions(expressions.stream().map(BoilerplateExpression::getId).collect(Collectors.toList()));
        tag.setProjectId(projectId.toString());
        return create(tag);
    }

    private ReferencedData saveItem(String string) throws DataStoreException {
        String uuid = UUID.randomUUID().toString();
        String stored = dataStore.store(new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)), uuid);
        return ReferencedData.getReferencedData(stored);
    }

    private BoilerplateWorker createBoilerplateWorker(BoilerplateWorkerTask task) throws InvalidTaskException, TaskRejectedException {
        return new BoilerplateWorker(task, dataStore, "results", connectionString, new JsonCodec(), 1000, "<boilerplate content>", null,
                expressionCache, tagCache, tagExpressionCache);
    }

    private Tag create(Tag tag) {
        try {
            return boilerplateApi.createTag(tag);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }

    private BoilerplateExpression create(BoilerplateExpression expression) {
        try {
            return boilerplateApi.createExpression(expression);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }

}
