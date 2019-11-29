/*
 * Copyright 2017-2020 Micro Focus or one of its affiliates.
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
package com.hpe.caf.worker.boilerplate.expressionprocessing;

import com.google.common.collect.Multimap;
import com.hpe.caf.api.Codec;
import com.hpe.caf.api.worker.DataStore;
import com.hpe.caf.api.worker.DataStoreException;
import com.hpe.caf.api.worker.DataStoreSource;
import com.hpe.caf.api.worker.TaskRejectedException;
import com.hpe.caf.boilerplate.webcaller.model.BoilerplateExpression;
import com.hpe.caf.util.ref.DataSourceException;
import com.hpe.caf.util.ref.ReferencedData;
import com.hpe.caf.worker.boilerplateshared.BoilerplateWorkerTask;
import com.hpe.caf.worker.boilerplateshared.response.BoilerplateResult;
import com.hpe.caf.worker.boilerplateshared.response.BoilerplateWorkerResponse;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class that handles the processing of boilerplate expressions.
 */
public class BoilerplateExpressionProcessor {

    private static Logger logger = LoggerFactory.getLogger(BoilerplateExpressionProcessor.class);
    private BoilerplateWorkerTask task;
    private Processor processor;
    private DataStore dataStore;
    private String defaultReplacementText;
    private Codec codec;
    private Integer resultSizeLimit;

    public BoilerplateExpressionProcessor(BoilerplateWorkerTask task, Processor processor, DataStore dataStore,
                                          Codec codec, String defaultReplacementText, Integer resultSizeLimit) {
        this.task = task;
        this.processor = processor;
        this.dataStore = dataStore;
        this.codec = codec;
        this.defaultReplacementText = defaultReplacementText;
        this.resultSizeLimit = resultSizeLimit;
    }

    public BoilerplateWorkerResponse processExpressions(List<BoilerplateExpression> expressions) throws TaskRejectedException {

        BoilerplateWorkerResponse workerResponse = new BoilerplateWorkerResponse();
        Multimap<String, ReferencedData> sourceData = task.getSourceData();
        for (Map.Entry<String, ReferencedData> referencedDataEntry : sourceData.entries()) {
            InputStream stream = null;
            try {
                stream = referencedDataEntry.getValue().acquire(new DataStoreSource(dataStore, codec));

                if (stream ==  null) {
                    //  Not sure of the exact cause of data acquire failure.
                    throw new TaskRejectedException("Failed to acquire data.");
                }

                ProcessorResult processorResult = processor.match(stream, expressions, task.getRedactionType(), defaultReplacementText, task.getReturnMatches());
                BoilerplateResult boilerplateResult = new BoilerplateResult();
                boilerplateResult.setMatches(processorResult.getMatches());
                if (processorResult.getInputStream() != null) {
                    if (boilerplateResult.getData() == null) {
                        boilerplateResult.setData(new ArrayList<>());
                    }
                    if (processorResult.getInputStream().equals(stream)) {
                        boilerplateResult.getData().add(referencedDataEntry.getValue());
                    } else {
                        //Save the stream to the store
                        try {
                            ReferencedData referencedData = storeOrSerialize(processorResult.getInputStream(), task.getDataStorePartialReference(), dataStore, processorResult.getInputStream().available(), resultSizeLimit);
                            boilerplateResult.getData().add(referencedData);
                        } catch (IOException | DataStoreException e) {
                            throw new RuntimeException(e);
                        } finally {
                            try {
                                processorResult.getInputStream().close();
                            } catch (IOException e) {
                                logger.warn("Could not close stream", e);
                            }
                        }
                    }
                }
                workerResponse.getTaskResults().put(referencedDataEntry.getKey(), boilerplateResult);
            } catch (DataSourceException e) {
                //  Not sure of the exact cause of content retrieval failure.
                throw new TaskRejectedException("Failed to retrieve content from storage.", e);
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        logger.warn("Could not close stream", e);
                    }
                }
            }
        }

        return workerResponse;
    }

    /*
      Util method to check whether to store output in Datastore or just serialize
  */
    private ReferencedData storeOrSerialize(InputStream stream, String dataStoreRef, DataStore store, int dataSize, int resultSizeLimit) throws IOException, DataStoreException {
        if (dataSize < resultSizeLimit) {
            return ReferencedData.getWrappedData(IOUtils.toByteArray(stream));
        } else {
            return ReferencedData.getReferencedData(store.store(stream, dataStoreRef));
        }
    }
}
