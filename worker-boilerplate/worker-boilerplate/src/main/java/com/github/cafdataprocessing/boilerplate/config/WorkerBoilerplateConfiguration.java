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
package com.github.cafdataprocessing.boilerplate.config;

/**
 *
 * @author mcgreeva
 */
public class WorkerBoilerplateConfiguration
{
    private String baseUrl;

    private String cacheExpireTimePeriod;

    private int resultSizeThreshold;

    public WorkerBoilerplateConfiguration()
    {
    }

    /**
     * @return the baseUrl
     */
    public String getBaseUrl()
    {
        return baseUrl;
    }

    /**
     * @param baseUrl the baseUrl to set
     */
    public void setBaseUrl(final String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    /**
     * @return the cacheExpireTimePeriod
     */
    public String getCacheExpireTimePeriod()
    {
        return cacheExpireTimePeriod;
    }

    /**
     * @param cacheExpireTimePeriod the cacheExpireTimePeriod to set
     */
    public void setCacheExpireTimePeriod(final String cacheExpireTimePeriod)
    {
        this.cacheExpireTimePeriod = cacheExpireTimePeriod;
    }

    /**
     * @return the resultSizeThreshold
     */
    public int getResultSizeThreshold()
    {
        return resultSizeThreshold;
    }

    /**
     * @param resultSizeThreshold the resultSizeThreshold to set
     */
    public void setResultSizeThreshold(final int resultSizeThreshold)
    {
        this.resultSizeThreshold = resultSizeThreshold;
    }
}
