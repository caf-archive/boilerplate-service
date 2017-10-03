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
package com.hpe.caf.boilerplate.web;

import com.hpe.caf.boilerplate.api.BoilerplateApi;
import com.hpe.caf.boilerplate.api.Tag;
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
public class TagCurrentETagRetriever implements ETagRetriever<Tag> {
    public static final Logger logger = LoggerFactory.getLogger(BoilerplateExpressionCurrentETagRetriever.class);
    private final BoilerplateApi boilerplateApi;

    @Autowired
    public TagCurrentETagRetriever(BoilerplateApi boilerplateApi){
        this.boilerplateApi = boilerplateApi;
    }

    public Class getETagGeneratingType(){
        return Tag.class;
    }

    public String getExpectedETag(HttpServletRequest request){
        Integer tagId = ParametersHelper.getIntegerParameterFromRequest(request, Constants.MethodParameters.Shared.TAG_ID);
        if(tagId==null){
            return null;
        }
        // generate eTag representing current state of Tag
        Tag currentTag = null;
        try{
            currentTag = boilerplateApi.getTag(tagId);
        }
        catch(ItemNotFoundException e){
            logger.debug("Exception occurred calling getExpression when creating eTag to compare against value sent from client.", e);
            return null;
        }
        if(currentTag==null){
            return null;
        }
        return Integer.toString(currentTag.hashCode());
    }
}
