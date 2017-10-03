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
package com.hpe.caf.util.boilerplate.creation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hpe.caf.boilerplate.webcaller.ApiClient;
import com.hpe.caf.boilerplate.webcaller.ApiException;
import com.hpe.caf.boilerplate.webcaller.api.BoilerplateApi;
import com.hpe.caf.boilerplate.webcaller.model.BoilerplateExpression;
import com.hpe.caf.boilerplate.webcaller.model.Tag;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Integration tests for boilerplate creation
 */
public class BoilerplateCreatorIT {
    private static final String cafBoilerplateApiUrl;

    static {
        cafBoilerplateApiUrl = System.getProperty(Constants.Properties.BOILERPLATE_URL);
    }

    @Test(description = "Checks that expressions and tags are created with expected values.")
    public void createExpressionsAndTagsTest() throws ApiException {
        String testProjectId = UUID.randomUUID().toString();
        Collection<CreationExpression> testExpressions = buildCreationExpressions(5, testProjectId);
        Collection<Tag> testTags = buildTags(3, testProjectId, testExpressions.stream()
                .map(ex -> ex.getTempId()).limit(3).collect(Collectors.toList()));
        testTags.addAll(buildTags(2, testProjectId, testExpressions.stream()
                .map(ex -> ex.getTempId()).skip(3).limit(2)
                .collect(Collectors.toList())));

        BoilerplateCreator bpCreator = new BoilerplateCreator();
        bpCreator.createExpressionsAndTags(testProjectId, testExpressions, testTags);
        BoilerplateApi boilerplateApi = getBoilerplateApi(testProjectId);
        List<BoilerplateExpression> createdExpressions = boilerplateApi.getExpressions(1, 10);
        verifyCreatedExpressions(testExpressions, createdExpressions);

        List<Tag> createdTags = boilerplateApi.getTags(1, 10);
        verifyCreatedTags(testTags, createdTags);
    }

    @Test(description = "Creates some expressions and tags then calls create again with the expectation that the first set of " +
            "expressions and tags will be removed.")
    public void removeExistingExpressionsAndTagsTest() throws ApiException, IOException {
        String testProjectId = UUID.randomUUID().toString();
        ObjectMapper mapper = new ObjectMapper();

        Collection<CreationExpression> testExpressions = buildCreationExpressions(5, testProjectId);
        Collection<Tag> testTags = buildTags(3, testProjectId, testExpressions.stream()
                .map(ex -> ex.getTempId()).limit(3).collect(Collectors.toList()));
        testTags.addAll(buildTags(2, testProjectId, testExpressions.stream()
                .map(ex -> ex.getTempId()).skip(3).limit(2)
                .collect(Collectors.toList())));

        //creating a copy of the test tags as they will be modified during creation to have the actual IDs of created expressions
        Collection<Tag> originalTestTags = mapper.readValue(mapper.writeValueAsBytes(testTags),
                new TypeReference<List<Tag>>(){});

        //create first set of expressions and tags
        BoilerplateCreator bpCreator = new BoilerplateCreator();
        bpCreator.createExpressionsAndTags(testProjectId, testExpressions, testTags);
        BoilerplateApi boilerplateApi = getBoilerplateApi(testProjectId);
        List<BoilerplateExpression> firstCreatedExpressions = boilerplateApi.getExpressions(1, 10);
        verifyCreatedExpressions(testExpressions, firstCreatedExpressions);

        List<Tag> firstCreatedTags = boilerplateApi.getTags(1, 10);
        verifyCreatedTags(testTags, firstCreatedTags);

        //record the IDs of those expressions and tags
        List<Long> firstExpressionsIds = firstCreatedExpressions.stream().map(ex -> ex.getId()).collect(Collectors.toList());
        List<Long> firstTagsIds = firstCreatedTags.stream().map(ta -> ta.getId()).collect(Collectors.toList());

        //create second set of expressions and tags, expecting first set to be removed
        bpCreator.createExpressionsAndTags(testProjectId, testExpressions, originalTestTags);

        List<BoilerplateExpression> secondCreatedExpressions = boilerplateApi.getExpressions(1, 100);
        //check that none of the expressions returned match the IDs of the first set
        List<Long> curentExpressionIds = secondCreatedExpressions.stream()
                .map(ex -> ex.getId()).collect(Collectors.toList());
        for(Long firstExpressionId: firstExpressionsIds){
            Assert.assertTrue(!curentExpressionIds.contains(firstExpressionId), "ID from first set of created expressions " +
                    "should no longer be present.");
        }

        verifyCreatedExpressions(testExpressions, secondCreatedExpressions);

        List<Tag> secondCreatedTags = boilerplateApi.getTags(1, 100);
        List<Long> curentTagIds = secondCreatedTags.stream()
                .map(ta -> ta.getId()).collect(Collectors.toList());
        for(Long firstTagId: firstTagsIds){
            Assert.assertTrue(!curentTagIds.contains(firstTagId), "ID from first set of created tags " +
                    "should no longer be present.");
        }
        verifyCreatedTags(originalTestTags, secondCreatedTags);
    }

    private void verifyCreatedTags(Collection<Tag> expectedTags, List<Tag> createdTags){
        Assert.assertEquals(
                createdTags.size(),
                expectedTags.size(),
                "Number of created tags should be the same as the expected number.");
        for(Tag expectedTag: expectedTags){
            Optional<Tag> tagOptional = createdTags.stream().filter(ta -> ta.getName().equals(expectedTag.getName())).findFirst();
            Assert.assertTrue(tagOptional.isPresent(), "Should have a created tag matching expected name.");
            Tag createdTag = tagOptional.get();
            verifyCreatedTag(expectedTag, createdTag);
        }
    }

    private void verifyCreatedTag(Tag expectedTag, Tag createdTag){
        Assert.assertEquals(createdTag.getDescription(), expectedTag.getDescription(),
                "Description of created tag should be as expected.");
        Assert.assertEquals(createdTag.getDefaultReplacementText(), expectedTag.getDefaultReplacementText(),
                "Default replacement text on created tag should be as expected.");
        List<Long> createdTagExpressionIds = createdTag.getBoilerplateExpressions();
        List<Long> expectedTagExpressionIds = expectedTag.getBoilerplateExpressions();
        Assert.assertEquals(createdTagExpressionIds.size(), expectedTagExpressionIds.size(),
                "Expression IDs on created tag should be expected size.");
        for(Long createdTagExpressionId: createdTagExpressionIds){
            Assert.assertTrue(expectedTagExpressionIds.contains(createdTagExpressionId),
                    "");
        }

    }

    private void verifyCreatedExpressions(Collection<CreationExpression> expectedExpressions,
                                          List<BoilerplateExpression> createdExpressions){
        Assert.assertEquals(
                createdExpressions.size(),
                expectedExpressions.size(),
                "Number of created expressions should be the same as the expected number.");
        for(CreationExpression expectedExpression: expectedExpressions){
            Optional<BoilerplateExpression> expressionOptional = createdExpressions.stream()
                    .filter(ex -> ex.getName().equals(expectedExpression.getName())).findFirst();
            Assert.assertTrue(expressionOptional.isPresent(), "Should have a created expression matching expected name.");
            BoilerplateExpression createdExpression = expressionOptional.get();
            verifyCreatedExpression(expectedExpression, createdExpression);
        }
    }

    private void verifyCreatedExpression(CreationExpression expectedExpression, BoilerplateExpression createdExpression){
        Assert.assertEquals(createdExpression.getDescription(), expectedExpression.getDescription(),
                "Description of created expression should be as expected.");
        Assert.assertEquals(createdExpression.getExpression(), expectedExpression.getExpression(),
                "Expression on created expression should be as expected.");
        Assert.assertEquals(createdExpression.getReplacementText(), expectedExpression.getReplacementText(),
                "Replacement text on created expression should be as expected.");
    }

    private BoilerplateApi getBoilerplateApi(String projectId){
        ApiClient apiClient = new ApiClient();
        apiClient.setApiKey(projectId);
        apiClient.setBasePath(cafBoilerplateApiUrl);
        return new BoilerplateApi(apiClient);
    }

    private Collection<CreationExpression> buildCreationExpressions(int numToCreate, String projectId){
        Collection<CreationExpression> expressions = new ArrayList<>();
        for(long expressionTempId = 0; expressionTempId < numToCreate; expressionTempId++) {
            CreationExpression expression = new CreationExpression();
            expression.setTempId(expressionTempId);
            expression.setName("Name_"+UUID.randomUUID().toString());
            expression.setDescription("Desc_"+UUID.randomUUID().toString());
            expression.setProjectId(projectId);
            expression.setExpression("Expression_"+UUID.randomUUID().toString());
            expression.setReplacementText("Replacement_text_"+UUID.randomUUID().toString());
            expressions.add(expression);
        }
        return expressions;
    }

    private Collection<Tag> buildTags(int numToCreate, String projectId, List<Long> expressionsToUse){
        Collection<Tag> tags = new ArrayList<>();
        for(long tagIndex = 0; tagIndex < numToCreate; tagIndex++){
            Tag tag = new Tag();
            tag.setDescription("Desc_"+UUID.randomUUID().toString());
            tag.setName("Name_"+UUID.randomUUID().toString());
            tag.setProjectId(projectId);
            tag.setBoilerplateExpressions(expressionsToUse);
            tags.add(tag);
        }
        return tags;
    }
}
