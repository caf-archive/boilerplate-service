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
import com.google.common.collect.Multimap;
import com.hpe.caf.util.ref.ReferencedData;
import com.hpe.caf.worker.boilerplateshared.BoilerplateWorkerConstants;
import com.hpe.caf.worker.boilerplateshared.BoilerplateWorkerTask;
import com.hpe.caf.worker.testing.FileInputWorkerTaskFactory;
import com.hpe.caf.worker.testing.TestConfiguration;
import com.hpe.caf.worker.testing.TestItem;


/**
 * Created by Michael.McAlynn on 12/01/2016.
 */
public class BoilerplateTaskFactory extends FileInputWorkerTaskFactory<BoilerplateWorkerTask,
BoilerplateTestInput, BoilerplateTestExpectation> {

    public BoilerplateTaskFactory(TestConfiguration configuration) throws Exception {
        super(configuration);
    }

    @Override
    protected BoilerplateWorkerTask createTask(TestItem<BoilerplateTestInput, BoilerplateTestExpectation> testItem, ReferencedData sourceData) {
        BoilerplateWorkerTask task = new BoilerplateWorkerTask();

        BoilerplateTestInput inputData = testItem.getInputData();
        task.setSourceData(HashMultimap.create());
        task.getSourceData().put("CONTENT", sourceData);
        Multimap<String, ReferencedData> inputSourceData = inputData.getSourceData();
        if(inputSourceData!=null && !inputSourceData.isEmpty()) {
            task.getSourceData().putAll(inputData.getSourceData());
        }
        task.setExpressions(inputData.getExpressions());
        task.setRedactionType(inputData.getRedactionType());
        task.setReturnMatches(inputData.getReturnMatches());
        task.setTenantId(inputData.getTenantId());
        task.setDataStorePartialReference(getContainerId());
        return task;
    }

    @Override
    public String getWorkerName() {
        return BoilerplateWorkerConstants.WORKER_NAME;
    }

    @Override
    public int getApiVersion() {
        return BoilerplateWorkerConstants.WORKER_API_VERSION;
    }
}
