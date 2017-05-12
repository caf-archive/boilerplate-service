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
package com.hpe.caf.boilerplate.web;

import com.hpe.caf.boilerplate.api.BoilerplateApi;
import com.hpe.caf.boilerplate.api.BoilerplateExpression;
import com.hpe.caf.boilerplate.api.exceptions.ItemNotFoundException;
import com.hpe.caf.boilerplate.web.setup.etags.ETagRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Michael.McAlynn on 14/12/2015.
 */
@Component
public class BoilerplateExpressionCurrentETagRetriever implements ETagRetriever<BoilerplateExpression> {
    public static final Logger logger = LoggerFactory.getLogger(BoilerplateExpressionCurrentETagRetriever.class);
    private final BoilerplateApi boilerplateApi;

    @Autowired
    public BoilerplateExpressionCurrentETagRetriever(BoilerplateApi boilerplateApi){
        this.boilerplateApi = boilerplateApi;
    }

    public Class getETagGeneratingType(){
        return BoilerplateExpression.class;
    }

    public String getExpectedETag(HttpServletRequest request){
        Integer expressionId = ParametersHelper.getIntegerParameterFromRequest(request, Constants.MethodParameters.Shared.EXPRESSION_ID);
        if(expressionId==null){
            return null;
        }
        // generate eTag representing current state of BoilerplateExpression
        BoilerplateExpression currentExpression = null;
        try{
            currentExpression = boilerplateApi.getExpression(expressionId);
        }
        catch(ItemNotFoundException e){
            logger.debug("Exception occurred calling getExpression when creating eTag to compare against value sent from client.", e);
            return null;
        }
        if(currentExpression==null){
            return null;
        }
        return Integer.toString(currentExpression.hashCode());
    }
}
