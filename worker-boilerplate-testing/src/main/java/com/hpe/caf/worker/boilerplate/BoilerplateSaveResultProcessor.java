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

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.hpe.caf.api.worker.DataStoreSource;
import com.hpe.caf.api.worker.TaskMessage;
import com.hpe.caf.util.ref.DataSource;
import com.hpe.caf.util.ref.DataSourceException;
import com.hpe.caf.util.ref.ReferencedData;
import com.hpe.caf.worker.boilerplateshared.BoilerplateWorkerConstants;
import com.hpe.caf.worker.boilerplateshared.BoilerplateWorkerTask;
import com.hpe.caf.worker.boilerplateshared.response.BoilerplateResult;
import com.hpe.caf.worker.boilerplateshared.response.BoilerplateWorkerResponse;
import com.hpe.caf.worker.boilerplateshared.response.SignatureExtractStatus;
import com.hpe.caf.worker.testing.ContentFileTestExpectation;
import com.hpe.caf.worker.testing.TestConfiguration;
import com.hpe.caf.worker.testing.TestItem;
import com.hpe.caf.worker.testing.WorkerServices;
import com.hpe.caf.worker.testing.data.ContentComparisonType;
import com.hpe.caf.worker.testing.preparation.PreparationResultProcessor;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Michael.McAlynn on 13/01/2016.
 */
public class BoilerplateSaveResultProcessor extends PreparationResultProcessor<BoilerplateWorkerTask,
        BoilerplateWorkerResponse, BoilerplateTestInput, BoilerplateTestExpectation> {
    private TestConfiguration configuration;
    private WorkerServices workerServices;

    protected BoilerplateSaveResultProcessor(TestConfiguration<BoilerplateWorkerTask, BoilerplateWorkerResponse,
            BoilerplateTestInput, BoilerplateTestExpectation> configuration, WorkerServices workerServices) {
        super(configuration, workerServices.getCodec());
        this.configuration = configuration;
        this.workerServices = workerServices;
    }

    @Override
    protected byte[] getOutputContent(BoilerplateWorkerResponse boilerplateWorkerResponse, TaskMessage message,
                                      TestItem<BoilerplateTestInput, BoilerplateTestExpectation> testItem) throws Exception {
        testItem.getExpectedOutputData().setTaskResults(convertMap(boilerplateWorkerResponse.getTaskResults(), testItem));
        return super.getOutputContent(boilerplateWorkerResponse, message, testItem);
    }

    private Map<String, BoilerplateResultForTest> convertMap(Map<String, BoilerplateResult> map,
                                                             TestItem<BoilerplateTestInput, BoilerplateTestExpectation> testItem) {
        Map<String, BoilerplateResultForTest> updatedMap = new HashMap<>();
        map.forEach((k, v) -> {
            try {
                updatedMap.put(k, convertResult(v, testItem));
            } catch (DataSourceException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return updatedMap;
    }

    private BoilerplateResultForTest convertResult(BoilerplateResult result,
                                                   TestItem<BoilerplateTestInput, BoilerplateTestExpectation> testItem)
            throws DataSourceException, IOException {
        BoilerplateResultForTest resultForTest = new BoilerplateResultForTest();
        resultForTest.setMatches(result.getMatches());

        //convert data
        Collection<ReferencedData> resultData = result.getData();
        if(resultData != null) {
            convertReferencedData(resultData, resultForTest, testItem);
        }
        //convert groupedMatches
        Multimap<String, ReferencedData> groupedMatches = result.getGroupedMatches();
        if(groupedMatches != null){
            convertGroupedMatches(groupedMatches,resultForTest,testItem);
        }
        // convert signatureExtractStatus
        SignatureExtractStatus signatureExtractStatus = result.getSignatureExtractStatus();
        if(signatureExtractStatus != null) {
            resultForTest.setSignatureExtractStatus(signatureExtractStatus);
        }
        return resultForTest;
    }

    private void convertReferencedData(Collection<ReferencedData> resultData, BoilerplateResultForTest resultForTest,
                                       TestItem<BoilerplateTestInput, BoilerplateTestExpectation> testItem) throws DataSourceException, IOException {
        Collection testResultData = resultForTest.getData();
        if(testResultData== null){
            testResultData = new ArrayList<>();
            resultForTest.setData(testResultData);
        }
        for(ReferencedData data : resultData) {
            ContentFileTestExpectation expectation = new ContentFileTestExpectation();
            expectation.setComparisonType(ContentComparisonType.BINARY);
            expectation.setExpectedSimilarityPercentage(100);
            Path savePath = Paths.get(configuration.getTestDataFolder(), testItem.getTag() + ".content");
            DataSource dataSource = new DataStoreSource(workerServices.getDataStore(), workerServices.getCodec());
            String content = IOUtils.toString(data.acquire(dataSource));
            Files.write(savePath, content.getBytes(), StandardOpenOption.CREATE);
            expectation.setExpectedContentFile(savePath.toString());
            testResultData.add(expectation);
        }
    }

    private void convertGroupedMatches(Multimap<String, ReferencedData> groupedMatches, BoilerplateResultForTest resultForTest,
                                       TestItem<BoilerplateTestInput, BoilerplateTestExpectation> testItem) {
        if(resultForTest.getGroupedMatches() == null){
            resultForTest.setGroupedMatches(LinkedListMultimap.create());
        }
        groupedMatches.entries().stream().filter(entry -> entry.getKey().equalsIgnoreCase(BoilerplateWorkerConstants.PRIMARY_CONTENT) ||
                entry.getKey().equalsIgnoreCase(BoilerplateWorkerConstants.SECONDARY_CONTENT) ||
                entry.getKey().equalsIgnoreCase(BoilerplateWorkerConstants.TERTIARY_CONTENT)).forEach(entry -> {
            try {
                ContentFileTestExpectation expectation = new ContentFileTestExpectation();
                expectation.setComparisonType(ContentComparisonType.BINARY);
                expectation.setExpectedSimilarityPercentage(100);
                Path savePath = Paths.get(configuration.getTestDataFolder(), testItem.getTag() + getFileExtension(entry.getKey()));
                DataSource dataSource = new DataStoreSource(workerServices.getDataStore(), workerServices.getCodec());
                String content = IOUtils.toString(entry.getValue().acquire(dataSource));
                Files.write(savePath, content.getBytes(), StandardOpenOption.CREATE);
                expectation.setExpectedContentFile(savePath.toString());
                resultForTest.getGroupedMatches().put(entry.getKey(),expectation);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (DataSourceException e) {
                e.printStackTrace();
            }
        });
        final Integer[] numOfSignatures = {0};
        groupedMatches.entries().stream().filter(entry ->
                entry.getKey().equalsIgnoreCase(BoilerplateWorkerConstants.EXTRACTED_SIGNATURES)).forEach(entry ->{
            try{
                numOfSignatures[0]++;
                ContentFileTestExpectation expectation = new ContentFileTestExpectation();
                expectation.setComparisonType(ContentComparisonType.BINARY);
                expectation.setExpectedSimilarityPercentage(100);
                String[] split = testItem.getTag().split("/");
                String testCaseName;
                if(split.length > 1){
                    testCaseName = split[split.length-1];
                } else {
                    testCaseName = testItem.getTag();
                }
                Path savePath = Paths.get(configuration.getTestDataFolder(), testItem.getTag()+"-signatures", testCaseName + getFileExtension(entry.getKey())+numOfSignatures[0]);
                DataSource dataSource = new DataStoreSource(workerServices.getDataStore(), workerServices.getCodec());
                String content = IOUtils.toString(entry.getValue().acquire(dataSource));
                Files.createDirectories(savePath.getParent());
                Files.write(savePath, content.getBytes(), StandardOpenOption.CREATE);
                expectation.setExpectedContentFile(savePath.toString());
                resultForTest.getGroupedMatches().put(entry.getKey(),expectation);
            } catch (DataSourceException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private String getFileExtension(String key){
        if(key.equalsIgnoreCase(BoilerplateWorkerConstants.PRIMARY_CONTENT)){
            return ".primary";
        }
        if(key.equalsIgnoreCase(BoilerplateWorkerConstants.SECONDARY_CONTENT)){
            return ".secondary";
        }
        if(key.equalsIgnoreCase(BoilerplateWorkerConstants.TERTIARY_CONTENT)){
            return ".tertiary";
        }
        if(key.equalsIgnoreCase(BoilerplateWorkerConstants.EXTRACTED_SIGNATURES))
        {
            return ".signature";
        }
        return ".content";
    }
}
