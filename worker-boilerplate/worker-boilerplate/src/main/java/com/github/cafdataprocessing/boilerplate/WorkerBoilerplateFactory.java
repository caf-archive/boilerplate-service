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
package com.github.cafdataprocessing.boilerplate;

import com.hpe.caf.api.ConfigurationException;
import com.hpe.caf.worker.document.extensibility.DocumentWorker;
import com.hpe.caf.worker.document.extensibility.DocumentWorkerFactory;
import com.hpe.caf.worker.document.model.Application;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mcgreeva
 */
public class WorkerBoilerplateFactory implements DocumentWorkerFactory
{
    @Override
    public DocumentWorker createDocumentWorker(final Application application)
    {
        try {
            return new WorkerBoilerplate(application);
        } catch (ConfigurationException ex) {
            Logger.getLogger(WorkerBoilerplateFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
}
