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
package com.hpe.caf.worker.boilerplate;

import com.hpe.caf.api.worker.WorkerConfiguration;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Created by Jason.Quinn on 04/01/2016.
 */
public class BoilerplateWorkerConfiguration extends WorkerConfiguration {

    @NotNull
    @Size(min = 1)
    private String outputQueue;

    @Min(1)
    @Max(20)
    private int threads;

    @NotNull
    private String baseUrl;

    private String defaultReplacementText;

    @Min(1)
    private int resultSizeLimit = 1000;

    private String cacheExpireTimePeriod = "PT5M";

    public String getOutputQueue() {
        return outputQueue;
    }

    public void setOutputQueue(final String outputQueue) {
        this.outputQueue = outputQueue;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(final int threads) {
        this.threads = threads;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int getResultSizeLimit() {
        return resultSizeLimit;
    }

    public void setResultSizeLimit(int resultSizeLimit) {
        this.resultSizeLimit = resultSizeLimit;
    }

    public String getDefaultReplacementText() {
        return defaultReplacementText == null ? "<boilerplate content>" : defaultReplacementText;
    }

    public void setDefaultReplacementText(String defaultReplacementText) {
        this.defaultReplacementText = defaultReplacementText;
    }

    public String getCacheExpireTimePeriod() {
        return cacheExpireTimePeriod;
    }

    public void setCacheExpireTimePeriod(String cacheExpireTimePeriod) {
        this.cacheExpireTimePeriod = cacheExpireTimePeriod;
    }
}
