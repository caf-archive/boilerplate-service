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
package com.hpe.caf.worker.boilerplate;

import com.google.common.collect.HashMultimap;
import com.hpe.caf.worker.boilerplateshared.BoilerplateWorkerTask;
import com.hpe.caf.worker.boilerplateshared.response.BoilerplateWorkerResponse;
import com.hpe.caf.worker.testing.TestConfiguration;
import com.hpe.caf.worker.testing.TestItem;
import com.hpe.caf.worker.testing.preparation.PreparationItemProvider;
import java.nio.file.Path;

/**
 * Created by Michael.McAlynn on 13/01/2016.
 */
public class BoilerplateResultProvider extends PreparationItemProvider<BoilerplateWorkerTask,
        BoilerplateWorkerResponse, BoilerplateTestInput, BoilerplateTestExpectation> {
    TestConfiguration configuration;

    public BoilerplateResultProvider(TestConfiguration<BoilerplateWorkerTask, BoilerplateWorkerResponse,
            BoilerplateTestInput, BoilerplateTestExpectation> configuration) {
        super(configuration);
        this.configuration = configuration;
    }

    @Override
    protected TestItem createTestItem(Path inputFile, Path expectedFile) throws Exception {
        TestItem<BoilerplateTestInput, BoilerplateTestExpectation> item = super.createTestItem(inputFile, expectedFile);
        BoilerplateWorkerTask task = getTaskTemplate();
        //if no task template available
        if (task == null) {
            task = new BoilerplateWorkerTask();
        }
        //allowing some properties to be set on task template and others not to be (no need to set properties with defined default values)
        if (task.getDataStorePartialReference() == null) {
            task.setDataStorePartialReference(configuration.getDataStoreContainerId());
        }
        if (task.getTenantId() == null) {
            task.setTenantId("DefaultTenantId");
        }
        if (task.getSourceData() == null) {
            task.setSourceData(HashMultimap.create());
        }
        item = updateItem(item, task, inputFile);
        return item;
    }

    private TestItem updateItem(TestItem<BoilerplateTestInput, BoilerplateTestExpectation> item, BoilerplateWorkerTask task, Path inputFile){
        BoilerplateTestInput inputData = item.getInputData();

        inputData.setExpressions(task.getExpressions());
        inputData.setInputFile(inputFile.toString());
        inputData.setRedactionType(task.getRedactionType());
        inputData.setReturnMatches(task.getReturnMatches());
        inputData.setUseDataStore(true);
        inputData.setSourceData(task.getSourceData());
        inputData.setTenantId(task.getTenantId());
        return item;
    }
}
