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

import com.hpe.caf.worker.boilerplateshared.BoilerplateWorkerConstants;
import com.hpe.caf.worker.boilerplateshared.BoilerplateWorkerTask;
import com.hpe.caf.worker.boilerplateshared.response.BoilerplateWorkerResponse;
import com.hpe.caf.worker.testing.*;
import com.hpe.caf.worker.testing.execution.AbstractTestControllerProvider;

/**
 * Created by Michael.McAlynn on 12/01/2016.
 */
public class BoilerplateTestControllerProvider extends
        AbstractTestControllerProvider<BoilerplateWorkerConfiguration, BoilerplateWorkerTask, BoilerplateWorkerResponse,
                BoilerplateTestInput, BoilerplateTestExpectation> {

    public BoilerplateTestControllerProvider(){
        super(BoilerplateWorkerConstants.WORKER_NAME, BoilerplateWorkerConfiguration::getOutputQueue,
                BoilerplateWorkerConfiguration.class, BoilerplateWorkerTask.class, BoilerplateWorkerResponse.class,
                BoilerplateTestInput.class, BoilerplateTestExpectation.class);
    }

    @Override
    protected TestItemProvider getTestItemProvider(TestConfiguration<BoilerplateWorkerTask, BoilerplateWorkerResponse, BoilerplateTestInput, BoilerplateTestExpectation> configuration) {
        return new SerializedFilesTestItemProvider<>(configuration);
    }

    @Override
    protected WorkerTaskFactory<BoilerplateWorkerTask,
            BoilerplateTestInput, BoilerplateTestExpectation> getTaskFactory(TestConfiguration<BoilerplateWorkerTask,
            BoilerplateWorkerResponse, BoilerplateTestInput, BoilerplateTestExpectation> configuration) throws Exception {
        return new BoilerplateTaskFactory(configuration);
    }

    @Override
    protected ResultProcessor getTestResultProcessor(TestConfiguration<BoilerplateWorkerTask,
            BoilerplateWorkerResponse, BoilerplateTestInput, BoilerplateTestExpectation> configuration, WorkerServices workerServices) {
        return new BoilerplateResultValidationProcessor(configuration, workerServices);
    }

    @Override
    protected TestItemProvider getDataPreparationItemProvider(TestConfiguration configuration) {
        return new BoilerplateResultProvider(configuration);
    }

    @Override
    protected ResultProcessor getDataPreparationResultProcessor(TestConfiguration configuration, WorkerServices workerServices) {
        return new BoilerplateSaveResultProcessor(configuration, workerServices);
    }
}
