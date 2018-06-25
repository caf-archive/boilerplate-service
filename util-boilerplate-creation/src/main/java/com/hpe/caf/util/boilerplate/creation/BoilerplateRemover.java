/*
 * Copyright 2015-2018 Micro Focus or one of its affiliates.
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
package com.hpe.caf.util.boilerplate.creation;

import com.hpe.caf.boilerplate.webcaller.ApiException;
import com.hpe.caf.boilerplate.webcaller.api.BoilerplateApi;
import com.hpe.caf.boilerplate.webcaller.model.BoilerplateExpression;
import com.hpe.caf.boilerplate.webcaller.model.Tag;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Responsible for deleting boilerplate expressions and tags that match the names of provided data.
 */
public class BoilerplateRemover {
    private static final Logger LOGGER = LoggerFactory.getLogger(BoilerplateRemover.class);

    /**
     * Removes expressions/tags that are retrievable using the BoilerplateApi that match the names of the provided expressions/tags.
     * @param boilerplateApi API to use in retrieval and deletion of expressions/tags.
     * @param expressionNamesToRemove Names of expressions. Any existing expressions that match these names will be removed.
     * @param tagNamesToRemove Names of tags. Any existing tags that match these names will be removed.
     * @throws ApiException If error occurs trying to communicate with boilerplate API.
     */
    public static void removeMatchingExpressionsAndTags(BoilerplateApi boilerplateApi, Collection<String> expressionNamesToRemove,
                                                  Collection<String> tagNamesToRemove) throws ApiException {
        removeMatchingTags(boilerplateApi, tagNamesToRemove);
        removeMatchingExpressions(boilerplateApi, expressionNamesToRemove);
    }

    /**
     * Removes expressions that are retrievable using the BoilerplateApi that match the names of the provided expressions.
     * @param boilerplateApi API to use in retrieval and deletion of expressions/tags.
     * @param expressionNamesToRemove Names of expressions. Any existing expressions that match these names will be removed.
     * @throws ApiException If error occurs trying to communicate with boilerplate API.
     */
    public static void removeMatchingExpressions(BoilerplateApi boilerplateApi, Collection<String> expressionNamesToRemove)
            throws ApiException {
        int pageNum = 1;
        final int pageSize = 100;
        List<BoilerplateExpression> existingExpressions = new ArrayList<>();
        while(true){
            List<BoilerplateExpression> currentRetrievedExpressions = boilerplateApi.getExpressions(pageNum, pageSize);
            existingExpressions.addAll(currentRetrievedExpressions);
            if(currentRetrievedExpressions.size()!=pageSize){
                break;
            }
            pageNum++;
        }
        //delete all expressions that have a name matching the provided expression names
        for(BoilerplateExpression existingExpression: existingExpressions){
            if(!expressionNamesToRemove.contains(existingExpression.getName())){
                continue;
            }
            long expressionIdToDelete = existingExpression.getId();
            String existingExpressionName = existingExpression.getName();
            //check if this expression is in use by any tags
            List<Tag> tagsUsingExpression = boilerplateApi.getTagsByExpression(expressionIdToDelete);
            if(!tagsUsingExpression.isEmpty()){
                LOGGER.warn("An expression with the name: '"+existingExpressionName+ "' is currently being used by the following tags: "+
                        StringUtils.join(tagsUsingExpression, ", ")+
                        ". Due to this the existing expression will not be removed. Expression ID: "
                        +expressionIdToDelete);
                continue;
            }
            LOGGER.debug("Deleting expression with ID: "+expressionIdToDelete+" and name: "+existingExpressionName);
            boilerplateApi.deleteExpression(expressionIdToDelete);
            LOGGER.debug("Deleted expression with ID: "+expressionIdToDelete);
        }
    }

    /**
     * Removes tags that are retrievable using the BoilerplateApi that match the names of the provided tags.
     * @param boilerplateApi API to use in retrieval and deletion of tags.
     * @param tagNamesToRemove Names of tags. Any existing tags that match these names will be removed.
     * @throws ApiException If error occurs trying to communicate with boilerplate API.
     */
    public static void removeMatchingTags(BoilerplateApi boilerplateApi, Collection<String> tagNamesToRemove) throws ApiException{
        int pageNum = 1;
        final int pageSize = 100;
        List<Tag> existingTags = new ArrayList<>();
        //retrieve all existing tags
        while(true) {
            List<Tag> currentRetrievedTags = boilerplateApi.getTags(pageNum, pageSize);
            existingTags.addAll(currentRetrievedTags);
            if(currentRetrievedTags.size()!=pageSize){
                break;
            }
            pageNum++;
        }
        //delete all tags that have a name matching the provided tag names
        for(Tag existingTag: existingTags){
            String existingTagName = existingTag.getName();
            if(!tagNamesToRemove.contains(existingTagName)){
                continue;
            }
            long tagIdToDelete = existingTag.getId();
            LOGGER.debug("Deleting tag with ID: "+tagIdToDelete+ " and name: "+existingTagName);
            boilerplateApi.deleteTag(tagIdToDelete);
            LOGGER.debug("Deleted tag with ID: "+tagIdToDelete);
        }
    }
}
