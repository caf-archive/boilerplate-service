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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hpe.caf.boilerplate.webcaller.ApiClient;
import com.hpe.caf.boilerplate.webcaller.ApiException;
import com.hpe.caf.boilerplate.webcaller.api.BoilerplateApi;
import com.hpe.caf.boilerplate.webcaller.model.BoilerplateExpression;
import com.hpe.caf.boilerplate.webcaller.model.Tag;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by gibsodom on 06/01/2016.
 */

public class BoilerplateWebServiceIT {
    private String projectId;
    private String connectionString;
    //    private IntegrationTestHelper helper;
    ApiClient client = new ApiClient();

    BoilerplateApi api;

    @Before
    public void setup() {
        projectId = UUID.randomUUID().toString();
        connectionString = System.getenv("webserviceurl");
//        connectionString = "http://127.0.0.1:17000";
//        helper = new IntegrationTestHelper(connectionString,projectId);
        client.setApiKey(projectId);
        client.setBasePath(connectionString);
        api = new BoilerplateApi(client);
    }

    @Test
    public void testCreateExpression() throws IOException, ApiException {
        BoilerplateExpression expression = new BoilerplateExpression();
        expression.setName("Expression 1");
        expression.setDescription("Description of 1");
        expression.setReplacementText("Replacement");
        expression.setExpression("*");
        BoilerplateExpression createdExpression = api.createExpression(expression);
        Assert.assertNotNull(createdExpression);
        Assert.assertEquals("Project Id should match", projectId, createdExpression.getProjectId());
        compareExpressions(expression, createdExpression);
    }

    @Test
    public void testCreateTag() throws IOException, ApiException {
        Tag tag = new Tag();
        tag.setName("Tag 1");
        tag.setDescription("Description of 1");
        tag.setDefaultReplacementText("Replacement");
        tag.setBoilerplateExpressions(new ArrayList<>());
        Tag createdTag = api.createTag(tag);
        Assert.assertNotNull(createdTag);
        compareTags(tag, createdTag);
    }

    @Test
    public void testGetExpression() throws ApiException {
        BoilerplateExpression expression = new BoilerplateExpression();
        expression.setName("Expression 1");
        expression.setDescription("Description of 1");
        expression.setReplacementText("Replacement");
        expression.setExpression("*");
        BoilerplateExpression createdExpression = api.createExpression(expression);
        Assert.assertNotNull(createdExpression);
        BoilerplateExpression retrievedExpression = api.getExpression(createdExpression.getId());
        compareExpressions(createdExpression, retrievedExpression);
    }

    @Test
    public void testGetTag() throws ApiException {
        Tag tag = new Tag();
        tag.setName("Tag 1");
        tag.setDescription("Description of 1");
        tag.setDefaultReplacementText("Replacement");
        tag.setBoilerplateExpressions(new ArrayList<>());
        Tag createdTag = api.createTag(tag);
        Assert.assertNotNull(createdTag);
        Tag retrievedTag = api.getTag(createdTag.getId());
        compareTags(createdTag, retrievedTag);
    }

    @Test
    public void testGetExpressionByTag() throws ApiException {
        BoilerplateExpression expression = new BoilerplateExpression();
        expression.setName("Expression 1");
        expression.setDescription("Description of 1");
        expression.setReplacementText("Replacement");
        expression.setExpression("*");
        BoilerplateExpression createdExpression = api.createExpression(expression);
        Assert.assertNotNull(createdExpression);

        Tag tag = new Tag();
        tag.setName("Tag 1");
        tag.setDescription("Description of 1");
        tag.setDefaultReplacementText("Replacement");
        tag.setBoilerplateExpressions(new ArrayList<>());
        tag.getBoilerplateExpressions().add(createdExpression.getId());
        Tag createdTag = api.createTag(tag);
        Assert.assertNotNull(createdTag);
        List<BoilerplateExpression> retrievedExpressions = api.getExpressionsByTagPaged(createdTag.getId(), 1, 10);
        Assert.assertEquals("Should have only retrieved 1 Expression", 1, retrievedExpressions.size());
        BoilerplateExpression retrievedExpression = retrievedExpressions.stream().findFirst().get();
        Assert.assertNotNull(retrievedExpression);
        compareExpressions(createdExpression, retrievedExpression);
    }

    @Test
    public void testGetTags() throws ApiException {
        Tag tag = new Tag();
        tag.setName("Tag 1");
        tag.setDescription("Description of 1");
        tag.setDefaultReplacementText("Replacement");
        tag.setBoilerplateExpressions(new ArrayList<>());
        Tag createdTag1 = api.createTag(tag);
        Assert.assertNotNull(createdTag1);

        Tag tag2 = new Tag();
        tag2.setName("Tag 2");
        tag2.setDescription("Description of 2");
        tag2.setDefaultReplacementText("Replacement");
        tag2.setBoilerplateExpressions(new ArrayList<>());
        Tag createdTag2 = api.createTag(tag2);
        Assert.assertNotNull(createdTag2);
        List<Tag> retrievedTags = api.getTags(1, 10);
        Assert.assertEquals("Should have retrieved 2 tags", 2, retrievedTags.size());
        compareTags(createdTag1, retrievedTags.stream().filter(e -> e.getId() == createdTag1.getId()).findFirst().get());
        compareTags(createdTag2, retrievedTags.stream().filter(e -> e.getId() == createdTag2.getId()).findFirst().get());
    }

    @Test
    public void testGetTagsByExpression() throws ApiException {
        BoilerplateExpression expression = new BoilerplateExpression();
        expression.setName("Expression 1");
        expression.setDescription("Description of 1");
        expression.setReplacementText("Replacement");
        expression.setExpression("*");
        BoilerplateExpression createdExpression = api.createExpression(expression);
        Assert.assertNotNull(createdExpression);

        Tag tag = new Tag();
        tag.setName("Tag 1");
        tag.setDescription("Description of 1");
        tag.setDefaultReplacementText("Replacement");
        tag.setBoilerplateExpressions(new ArrayList<>());
        tag.getBoilerplateExpressions().add(createdExpression.getId());
        Tag createdTag = api.createTag(tag);
        Assert.assertNotNull(createdTag);

        List<Tag> retrievedTag = api.getTagsByExpression(createdExpression.getId());
        Assert.assertNotNull(retrievedTag);
        Assert.assertEquals("Should have only retrieved 1 tag", 1, retrievedTag.size());
        compareTags(createdTag, retrievedTag.stream().findFirst().get());
    }

    @Test
    public void testUpdateExpression() throws ApiException {
        BoilerplateExpression expression = new BoilerplateExpression();
        expression.setName("Expression 1");
        expression.setDescription("Description of 1");
        expression.setReplacementText("Replacement");
        expression.setExpression("*");
        BoilerplateExpression createdExpression = api.createExpression(expression);
        Assert.assertNotNull(createdExpression);
        compareExpressions(expression, createdExpression);

        expression.setName("Updated Expression 1");

        BoilerplateExpression updatedExpression = api.updateExpression(createdExpression.getId(), expression);
        Assert.assertEquals("Ids should match",createdExpression.getId(),updatedExpression.getId());
        Assert.assertNotEquals("Name should have updated", createdExpression.getName(), updatedExpression.getName());
    }

    @Test
    public void testUpdateTag() throws ApiException {
        Tag tag = new Tag();
        tag.setName("Tag 1");
        tag.setDescription("Description of 1");
        tag.setDefaultReplacementText("Replacement");
        tag.setBoilerplateExpressions(new ArrayList<>());
        Tag createdTag = api.createTag(tag);
        Assert.assertNotNull(createdTag);
        compareTags(tag, createdTag);

        tag.setName("Updated Tag 1");
        Tag updatedTag = api.updateTag(createdTag.getId(), tag);
        Assert.assertEquals("Ids should match", createdTag.getId(), updatedTag.getId());
        Assert.assertNotEquals("Name should have been updated", createdTag.getName(), updatedTag.getName());
    }

    @Test
    public void testDeleteExpression() throws ApiException {
        BoilerplateExpression expression = new BoilerplateExpression();
        expression.setName("Expression 1");
        expression.setDescription("Description of 1");
        expression.setReplacementText("Replacement");
        expression.setExpression("*");
        BoilerplateExpression createdExpression = api.createExpression(expression);
        Assert.assertNotNull(createdExpression);
        compareExpressions(expression, createdExpression);

        api.deleteExpression(createdExpression.getId());

        try {
            api.getExpression(createdExpression.getId());
        }
        catch (ApiException e){
            Assert.assertEquals("Should throw an ItemNotFound exception",404,e.getCode());
        }
    }

    @Test
    public void testDeleteTag() throws ApiException {
        Tag tag = new Tag();
        tag.setName("Tag 1");
        tag.setDescription("Description of 1");
        tag.setDefaultReplacementText("Replacement");
        tag.setBoilerplateExpressions(new ArrayList<>());
        Tag createdTag = api.createTag(tag);
        Assert.assertNotNull(createdTag);
        compareTags(tag, createdTag);

        api.deleteTag(createdTag.getId());
        try {
            api.getTag(createdTag.getId());
        }
        catch (ApiException e){
            Assert.assertEquals("Should throw an ItemNotFound exception",404,e.getCode());
        }
    }

    private void compareExpressions(BoilerplateExpression expected, BoilerplateExpression actual) {
        Assert.assertNotNull("Id should not be null", actual.getId());
        Assert.assertEquals("Name should match", expected.getName(), actual.getName());
        Assert.assertEquals("Description should match", expected.getDescription(), actual.getDescription());
        Assert.assertEquals("Replacement Text should match", expected.getReplacementText(), actual.getReplacementText());
        Assert.assertEquals("Expressions should match", expected.getExpression(), actual.getExpression());
    }

    private void compareTags(Tag expected, Tag actual) {
        Assert.assertNotNull("Id should not be null", actual.getId());
        Assert.assertEquals("Name should match", expected.getName(), actual.getName());
        Assert.assertEquals("Description should match", expected.getDescription(), actual.getDescription());
        Assert.assertEquals("Expression size should match", expected.getBoilerplateExpressions().size(), actual.getBoilerplateExpressions().size());
        if (expected.getBoilerplateExpressions() != null) {
            Assert.assertTrue("Expression Ids should match", expected.getBoilerplateExpressions().containsAll(actual.getBoilerplateExpressions()));
        }
        Assert.assertEquals("Default replacement text should match", expected.getDefaultReplacementText(), actual.getDefaultReplacementText());
    }
}
