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

import com.hpe.caf.boilerplate.webcaller.ApiClient;
import com.hpe.caf.boilerplate.webcaller.ApiException;
import com.hpe.caf.boilerplate.webcaller.api.BoilerplateApi;
import com.hpe.caf.boilerplate.webcaller.model.BoilerplateExpression;
import com.hpe.caf.boilerplate.webcaller.model.Tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Responsible for creating boilerplate expressions and tags.
 */
public class BoilerplateCreator {
    public BoilerplateCreator(){}

    /**
     * Reads expression and tags input file, creates these and outputs the created object IDs to a file.
     * Any existing expressions/tags under the projectId configured in the file that have matching names to the input data
     * will be removed before creation.
     */
    public void run(){
        run(true);
    }

    /**
     * Reads expression and tags input file, creates these and outputs the created object IDs to a file.
     * Any existing expressions/tags under the projectId configured in the file that have matching names to the input data
     * will be removed before creation based on the value provided for the {@code overwriteExisting} parameter.
     * @param overwriteExisting Whether existing expressions/tags should be removed if they match the names of the read in
     *                          expressions/tags.
     */
    public void run(boolean overwriteExisting){
        CreationInput input = CreationInputReader.getInput();
        System.out.println("");
        CreationResult cResult = createExpressionsAndTags(input.getProjectId(),
                input.getExpressions(),
                input.getTags(),
                overwriteExisting);
        System.out.println("");
        //output created tag and expression ids (along with project id)
        CreationResultWriter.outputResult(cResult);
        System.out.println("");
    }

    /**
     * Creates the provided expressions and tags under the specified projectId. If expressions/tags exist under the projectId
     * that match the provided names they will be removed before new expressions/tags are created.
     * @param projectId ProjectId to create with.
     * @param expressions Boilerplate Expressions to create.
     * @param tags Tags to create.
     * @return Represents the result of creating the expressions and tags including assigned IDs.
     * @throws RuntimeException If an error occurs creating expressions/tags.
     */
    public CreationResult createExpressionsAndTags(String projectId, Collection<CreationExpression> expressions,
                                                   Collection<Tag> tags) throws RuntimeException{
        return createExpressionsAndTags(projectId, expressions, tags, true);
    }

    /**
     * Creates the provided expressions and tags under the specified projectId.
     * @param projectId ProjectId to create with.
     * @param expressions Boilerplate Expressions to create.
     * @param tags Tags to create.
     * @param overwriteExisting Whether existing expressions/tags should be removed if they match the names of the given
     *                          expressions/tags.
     * @return Represents the result of creating the expressions and tags including assigned IDs.
     * @throws RuntimeException If an error occurs creating expressions/tags.
     *
     */
    public CreationResult createExpressionsAndTags(String projectId, Collection<CreationExpression> expressions,
                        Collection<Tag> tags, boolean overwriteExisting) throws RuntimeException {
        ApiClient apiClient = new ApiClient();
        apiClient.setApiKey(projectId);
        apiClient.setBasePath(System.getProperty(Constants.Properties.BOILERPLATE_URL));
        BoilerplateApi boilerplateApi = new BoilerplateApi(apiClient);

        if(overwriteExisting){
            try {
                BoilerplateRemover.removeMatchingExpressionsAndTags(boilerplateApi,
                        expressions.stream().map(ex -> ex.getName()).collect(Collectors.toList()),
                        tags.stream().map(ta -> ta.getName()).collect(Collectors.toList()));
            }
            catch(ApiException e){
                throw new RuntimeException(e);
            }
        }

        Collection<CreationExpression> createdExps = createBoilerplateExpressions(boilerplateApi, expressions, projectId);
        //update tags with the id's of created expressions
        updateTags(createdExps, tags);

        Collection<Tag> createdTags = createTags(boilerplateApi, tags, projectId);

        return new CreationResult(projectId, createdExps, createdTags);
    }



    private void updateTags(Collection<CreationExpression> createdExps, Collection<Tag> tags){
        //keeping a hashmap of already found mappings
        HashMap<Long, Long> idMappings = new HashMap<>();
        for(Tag tag: tags){
            List<Long> oldExpIds = tag.getBoilerplateExpressions();
            //replace temp expression ids with id's returned when expressions were created
            List<Long> newExpIds = new ArrayList<>();
            for(Long oldExpId : oldExpIds) {
                Long updatedId = idMappings.get(oldExpId);
                //if the expression id was already added to the mapping of temp id's to id's from creation then use it
                if(updatedId != null) {
                    newExpIds.add(updatedId);
                    continue;
                }
                //scan created exp ids for matching temp id
                CreationExpression matchedExp = createdExps.stream().filter(e -> e.getTempId() == oldExpId).findFirst().orElse(null);
                if(matchedExp==null){
                    throw new RuntimeException("Error trying to map tempId of boilerplate expression to the id it was given when created. Boilerplate Temp ID " +
                    oldExpId +" Tag Name: " + tag.getName());
                }
                newExpIds.add(matchedExp.getId());
            }
            tag.setBoilerplateExpressions(newExpIds);
        }
    }

    private Collection<CreationExpression> createBoilerplateExpressions(BoilerplateApi boilerplateApi,
                                             Collection<CreationExpression> expressions, String projectId){
        Collection<CreationExpression> createdExps = new ArrayList<>();
        if(!expressions.isEmpty()){
            System.out.println("================================================================================");
            System.out.println("                    Creating boilerplate expressions");
            System.out.println("================================================================================");
        }
        for(CreationExpression exp : expressions) {
            exp.setProjectId(projectId);
            try {
                //expression creator needs a BoilerplateExpression object NOT an upcasted CreationExpression as the json
                //writer will still see the property 'tempId' and try to serialize it (but we can't mark it as ignore as we need it to be read
                //in from the initial JSON)
                BoilerplateExpression createdExpression = boilerplateApi.createExpression(exp.getAsBoilerplateExpression());
                System.out.println("Created Boilerplate Expression, ID: "+ createdExpression.getId() + ", Name: "+createdExpression.getName());
                exp.setId(createdExpression.getId());
                createdExps.add(exp);
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        }
        return createdExps;
    }

    private Collection<Tag> createTags(BoilerplateApi boilerplateApi, Collection<Tag> tags, String projectId){
        Collection<Tag> createdTags = new ArrayList<>();
        if(!tags.isEmpty()){
            System.out.println("================================================================================");
            System.out.println("                            Creating tags");
            System.out.println("================================================================================");
        }
        for(Tag tag : tags){
            tag.setProjectId(projectId);
            try {
                Tag createdTag = boilerplateApi.createTag(tag);
                System.out.println("Created Tag, ID: "+createdTag.getId() + ", Name: "+createdTag.getName());
                createdTags.add(createdTag);
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        }
        return createdTags;
    }
}
