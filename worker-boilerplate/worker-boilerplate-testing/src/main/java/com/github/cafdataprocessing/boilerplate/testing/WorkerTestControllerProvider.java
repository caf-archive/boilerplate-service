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
package com.github.cafdataprocessing.boilerplate.testing;

import com.hpe.caf.worker.document.*;
import com.hpe.caf.worker.document.config.DocumentWorkerConfiguration;
import com.hpe.caf.worker.testing.*;
import com.hpe.caf.worker.testing.execution.AbstractTestControllerProvider;

public class WorkerTestControllerProvider
        extends AbstractTestControllerProvider<DocumentWorkerConfiguration,
                                               DocumentWorkerTask,
                                               DocumentWorkerResult,
                                               WorkerTestInput,
                                               DocumentWorkerTestExpectation>
{

    public WorkerTestControllerProvider() {
        super(WorkerConstants.WORKER_NAME,
                DocumentWorkerConfiguration::getOutputQueue,
                DocumentWorkerConfiguration.class,
                DocumentWorkerTask.class,
                DocumentWorkerResult.class,
                WorkerTestInput.class,
                DocumentWorkerTestExpectation.class);
    }


    @Override
    protected WorkerTaskFactory<DocumentWorkerTask, WorkerTestInput, DocumentWorkerTestExpectation>
    getTaskFactory(TestConfiguration<DocumentWorkerTask,
                                     DocumentWorkerResult,
                                     WorkerTestInput,
                                     DocumentWorkerTestExpectation> configuration) throws Exception
    {
        return new WorkerTaskFactories(configuration);
    }


    @Override
    protected ResultProcessor getTestResultProcessor(TestConfiguration<DocumentWorkerTask,
                                                                       DocumentWorkerResult,
                                                                       WorkerTestInput,
                                                                       DocumentWorkerTestExpectation> configuration,
                                                     WorkerServices workerServices)
    {
        return new DocumentWorkerResultValidationProcessor(configuration, workerServices);
    }


    @Override
    protected TestItemProvider getDataPreparationItemProvider(TestConfiguration<DocumentWorkerTask,
                                                                                DocumentWorkerResult,
                                                                                WorkerTestInput,
                                                                                DocumentWorkerTestExpectation> configuration)
    {
        return new WorkerResultPreparationProvider(configuration);
    }


    @Override
    protected ResultProcessor getDataPreparationResultProcessor(TestConfiguration<DocumentWorkerTask,
                                                                                  DocumentWorkerResult,
                                                                                  WorkerTestInput,
                                                                                  DocumentWorkerTestExpectation> configuration,
                                                                WorkerServices workerServices)
    {
        return new DocumentWorkerSaveResultProcessor(configuration, workerServices);
    }
}
