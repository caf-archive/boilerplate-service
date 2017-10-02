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

import com.hpe.caf.api.worker.TaskMessage;
import com.hpe.caf.worker.boilerplateshared.BoilerplateWorkerConstants;
import com.hpe.caf.worker.boilerplateshared.response.BoilerplateWorkerResponse;
import com.hpe.caf.worker.testing.TestConfiguration;
import com.hpe.caf.worker.testing.TestItem;
import com.hpe.caf.worker.testing.WorkerServices;
import com.hpe.caf.worker.testing.configuration.ValidationSettings;
import com.hpe.caf.worker.testing.validation.PropertyValidatingProcessor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Michael.McAlynn on 12/01/2016.
 */
public class BoilerplateResultValidationProcessor extends PropertyValidatingProcessor<BoilerplateWorkerResponse,
        BoilerplateTestInput, BoilerplateTestExpectation> {

    public BoilerplateResultValidationProcessor(TestConfiguration<?, BoilerplateWorkerResponse, BoilerplateTestInput,
            BoilerplateTestExpectation> testConfiguration, WorkerServices workerServices) {
        super(testConfiguration, workerServices, ValidationSettings.configure().referencedDataProperties()
                .arrayReferencedDataProperties("data", BoilerplateWorkerConstants.PRIMARY_CONTENT,
                        BoilerplateWorkerConstants.SECONDARY_CONTENT, BoilerplateWorkerConstants.TERTIARY_CONTENT, BoilerplateWorkerConstants.EXTRACTED_SIGNATURES).build());
    }

    @Override
    protected boolean isCompleted(TestItem<BoilerplateTestInput, BoilerplateTestExpectation> testItem, TaskMessage message, BoilerplateWorkerResponse boilerplateWorkerResponse) {
        return true;
    }

    @Override
    protected Map<String, Object> getExpectationMap(TestItem<BoilerplateTestInput, BoilerplateTestExpectation> testItem, TaskMessage message, BoilerplateWorkerResponse boilerplateWorkerResponse) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("taskResults", testItem.getExpectedOutputData().getTaskResults());
        return map;
    }

    @Override
    protected Object getValidatedObject(TestItem<BoilerplateTestInput, BoilerplateTestExpectation> testItem, TaskMessage message, BoilerplateWorkerResponse boilerplateWorkerResponse) {
        return boilerplateWorkerResponse;
    }
}
