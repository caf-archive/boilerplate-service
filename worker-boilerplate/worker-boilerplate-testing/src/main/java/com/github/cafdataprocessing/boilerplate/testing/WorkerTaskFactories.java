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

import com.hpe.caf.util.ref.ReferencedData;
import com.hpe.caf.worker.document.*;
import com.hpe.caf.worker.testing.FileInputWorkerTaskFactory;
import com.hpe.caf.worker.testing.TestConfiguration;
import com.hpe.caf.worker.testing.TestItem;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class WorkerTaskFactories
    extends FileInputWorkerTaskFactory<DocumentWorkerTask, WorkerTestInput, DocumentWorkerTestExpectation>
{
    private final static String DATASTORE_CONTAINER_ID_NAME = "datastore.container.id";
    private final static String TEST_CONTAINER_ID = "TEST_CONTAINER";

    public WorkerTaskFactories(final TestConfiguration configuration) throws Exception
    {
        super(configuration);
    }

    @Override
    public String getWorkerName()
    {
        return DocumentWorkerConstants.WORKER_NAME;
    }

    @Override
    public int getApiVersion()
    {
        return DocumentWorkerConstants.WORKER_API_VER;
    }

    @Override
    protected DocumentWorkerTask createTask(TestItem<WorkerTestInput, DocumentWorkerTestExpectation> testItem,
                                            ReferencedData sourceData)
    {
        final DocumentWorkerTask task = testItem.getInputData().getTask();
        setPerTenantValues(task);
        setPerDocumentValues(sourceData, task);
        return task;
    }

    private void setPerTenantValues(final DocumentWorkerTask task)
    {
        String datastoreContainerId = System.getProperty(DATASTORE_CONTAINER_ID_NAME,
                                                         System.getenv(DATASTORE_CONTAINER_ID_NAME) != null
                                                         ? System.getenv(DATASTORE_CONTAINER_ID_NAME)
                                                         : TEST_CONTAINER_ID);
        task.customData.put(WorkerConstants.CustomData.OUTPUT_PARTIAL_REFERENCE, datastoreContainerId);
        task.customData.put(WorkerConstants.CustomData.RETURN_MATCHES, "true");
        task.customData.put(WorkerConstants.CustomData.REDACT_TYPE, "DO_NOTHING");
        task.customData.put(WorkerConstants.CustomData.TENNANT_ID, "1");
        task.customData.put(WorkerConstants.CustomData.FIELDS, "[\"CONTENT\"]");
        task.customData.put(WorkerConstants.CustomData.EXPRESSION_IDS, "[\"1\", \"2\", \"3\", \"4\", \"5\", \"6\", \"7\"]");
        task.customData.put(WorkerConstants.CustomData.TAG_ID, null);
    }

    private static void setPerDocumentValues(final ReferencedData sourceData,
                                             final DocumentWorkerTask task)
    {
        if(sourceData.getData() != null){
        task.fields.put(WorkerConstants.CustomData.CONTENT, createDataList(sourceData.getData()));
        } else {
            task.fields.put(WorkerConstants.CustomData.CONTENT, createDataList(sourceData.getReference()));
        }
    }

    private static List<DocumentWorkerFieldValue> createDataList(final String data)
    {
        final List<DocumentWorkerFieldValue> documentWorkerFieldValueList = new ArrayList<>();
        documentWorkerFieldValueList.add(createData(data));
        return documentWorkerFieldValueList;
    }
    private static List<DocumentWorkerFieldValue> createDataList(final byte[] data)
    {
        final List<DocumentWorkerFieldValue> documentWorkerFieldValueList = new ArrayList<>();
        documentWorkerFieldValueList.add(createData(data));
        return documentWorkerFieldValueList;
    }

    private static DocumentWorkerFieldValue createData(final String data)
    {
        final DocumentWorkerFieldValue documentWorkerFieldValue = new DocumentWorkerFieldValue();
        documentWorkerFieldValue.data = data;
        documentWorkerFieldValue.encoding = DocumentWorkerFieldEncoding.storage_ref;
        return documentWorkerFieldValue;
    }
    private static DocumentWorkerFieldValue createData(final byte[] data)
    {
        final DocumentWorkerFieldValue documentWorkerFieldValue = new DocumentWorkerFieldValue();
        documentWorkerFieldValue.data = Base64.getEncoder().encodeToString(data);
        documentWorkerFieldValue.encoding = DocumentWorkerFieldEncoding.base64;
        return documentWorkerFieldValue;
    }
}
