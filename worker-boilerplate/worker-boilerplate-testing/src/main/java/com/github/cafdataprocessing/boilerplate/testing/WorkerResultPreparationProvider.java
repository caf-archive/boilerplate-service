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

import com.hpe.caf.worker.document.DocumentWorkerResult;
import com.hpe.caf.worker.document.DocumentWorkerTask;
import com.hpe.caf.worker.document.DocumentWorkerTestExpectation;
import com.hpe.caf.worker.testing.TestConfiguration;
import com.hpe.caf.worker.testing.TestItem;
import com.hpe.caf.worker.testing.preparation.PreparationItemProvider;

import java.nio.file.Path;
import java.util.HashMap;

public class WorkerResultPreparationProvider
        extends PreparationItemProvider<DocumentWorkerTask,
                                        DocumentWorkerResult,
                                        WorkerTestInput,
                                        DocumentWorkerTestExpectation>
{
    public WorkerResultPreparationProvider(final TestConfiguration<DocumentWorkerTask,
                                                                              DocumentWorkerResult,
                                                                              WorkerTestInput,
                                                                              DocumentWorkerTestExpectation> configuration)
    {
        super(configuration);
    }

    @Override
    protected TestItem createTestItem(Path inputFile, Path expectedFile) throws Exception {
        final TestItem<WorkerTestInput, DocumentWorkerTestExpectation> item = super.createTestItem(inputFile, expectedFile);
        final DocumentWorkerTask templateTask = getTaskTemplate();
        item.getInputData().setTask(templateTask == null ? createTask() : templateTask);
        item.setCompleted(false);
        return item;
    }

    private DocumentWorkerTask createTask() {
        final DocumentWorkerTask task = new DocumentWorkerTask();
        task.fields = new HashMap<>();
        task.customData = new HashMap<>();
        return task;
    }
}
