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
package com.hpe.caf.boilerplate.web;

import com.hpe.caf.boilerplate.api.BoilerplateExpression;
import com.hpe.caf.boilerplate.api.Tag;
import com.hpe.caf.boilerplate.web.exceptions.ErrorResponse;
import com.hpe.caf.boilerplate.web.exceptions.LocalizedException;
import com.hpe.caf.boilerplate.web.exceptions.LocalizedItemNotFoundErrors;
import com.hpe.caf.boilerplate.web.exceptions.LocalizedItemNotFoundException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by Michael.McAlynn on 11/12/2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:mvc-dispatcher-servlet.xml"})
public class BoilerplateApiControllerIT {
    @Autowired
    WebApplicationContext wac;
    @Autowired
    MockHttpSession session;
    @Autowired
    MockHttpServletRequest request;

    IntegrationTestHelper testHelper;

    private final static String BOILERPLATE_CONTROLLER_URL = "/boilerplate";
    private final static String BOILERPLATE_CREATE_URL = BOILERPLATE_CONTROLLER_URL + "/expression/";
    private final static String TAG_CREATE_URL = BOILERPLATE_CONTROLLER_URL + "/tag/";
    private final static String CHECK_HEALTH_URL = BOILERPLATE_CONTROLLER_URL + "/checkhealth";

    @Before
    public void setup() {
        this.testHelper = new IntegrationTestHelper(UUID.randomUUID().toString(), MockMvcBuilders.webAppContextSetup(this.wac).build(),
                this.request, this.session, this.wac, BOILERPLATE_CREATE_URL, TAG_CREATE_URL);
    }

    @Test
    public void checkHealthTest() throws Exception {
        boolean checkResult = testHelper.issueSuccessfulGetAndReturnMvcResult(CHECK_HEALTH_URL + "?project_id=" + testHelper.project_id, boolean.class);
        Assert.assertEquals("Expecting 'true' back from health check.", true, checkResult);
    }

    @Test
    public void createAndGetTag() throws Exception {
        Tag createdTag = testHelper.createTagViaWebSerivce("Test Tag", "Testing Web service", "Redacted", new ArrayList<>());
        //get the tag as it was stored on save
        Tag returnedTagByGet = testHelper.issueSuccessfulGetAndReturnMvcResult(testHelper.constructGetTagRequestUrl(createdTag.getId()), Tag.class);
        //compare the tag returned by create method with the tag returned by the get
        testHelper.compareTags(createdTag, returnedTagByGet);
    }

    @Test
    public void createAndGetExpression() throws Exception {
        BoilerplateExpression createdBoilerplateExpression = testHelper.createExpressionViaWebService("Test expression", "Testing create boilerplate expression",
                "Sample expression", "Sample replacement text.");

        BoilerplateExpression returnedExpressionByGet =
                testHelper.issueSuccessfulGetAndReturnMvcResult(testHelper.constructGetBoilerplateExpressionRequestUrl(createdBoilerplateExpression.getId()),
                        BoilerplateExpression.class);
        testHelper.compareBoilerplateExpressions(createdBoilerplateExpression, returnedExpressionByGet);
    }

    @Test
    public void createAndGetExpressionWithoutReplacementText() throws Exception {
        BoilerplateExpression createdBoilerplateExpression = testHelper.createExpressionViaWebService("Test expression", "Testing create boilerplate expression",
                "Sample expression", null);

        BoilerplateExpression returnedExpressionByGet =
                testHelper.issueSuccessfulGetAndReturnMvcResult(testHelper.constructGetBoilerplateExpressionRequestUrl(createdBoilerplateExpression.getId()),
                        BoilerplateExpression.class);
        testHelper.compareBoilerplateExpressions(createdBoilerplateExpression, returnedExpressionByGet);
    }

    @Test
    public void getNonExistantExpression() throws Exception {
        //request an expression that doesn't exist (randomly generated project id should ensure no existing expressions are picked up)
        ErrorResponse response = testHelper.issueItemNotFoundGetAndReturnMvcResult(testHelper.constructGetBoilerplateExpressionRequestUrl(1L), ErrorResponse.class);
        LocalizedException expectedException = new LocalizedItemNotFoundException(LocalizedItemNotFoundErrors.EXPRESSION_NOT_FOUND);
        testHelper.checkErrorResponse(expectedException, response);
    }

    @Test
    public void getNonExistantTag() throws Exception {
        //request a tag that doesn't exist (randomly generated project id should ensure no existing tags are picked up)
        ErrorResponse response = testHelper.issueItemNotFoundGetAndReturnMvcResult(testHelper.constructGetTagRequestUrl(1L), ErrorResponse.class);
        LocalizedException expectedException = new LocalizedItemNotFoundException(LocalizedItemNotFoundErrors.TAG_NOT_FOUND);
        testHelper.checkErrorResponse(expectedException, response);
    }

    @Test
    public void createTagWithNonExistantExpression() throws Exception {
        ArrayList<Long> boilerplateExpresions = new ArrayList<>();
        boilerplateExpresions.add(1L);
        Tag tagToCreate = testHelper.createTag("Test Tag", "Testing Web service", "Redacted", boilerplateExpresions);
        tagToCreate.setId(-1L);
        String serializedTag = testHelper.serializeItem(tagToCreate);
        ErrorResponse response = testHelper.issueItemNotFoundCreateAndReturnMvcResult(serializedTag, TAG_CREATE_URL, ErrorResponse.class);
        LocalizedException expectedException = new LocalizedItemNotFoundException(LocalizedItemNotFoundErrors.EXPRESSION_NOT_FOUND);
        testHelper.checkErrorResponse(expectedException, response);
    }

    @Test
    public void getExpressionByTag() throws Exception {
        //create expression for use with tag
        BoilerplateExpression createdBoilerplateExpression = testHelper.createExpressionViaWebService("Created expression", "This was created for test.", "aaaa", "test");

        //create tag with expression
        ArrayList<Long> boilerplateExpresions = new ArrayList<>();
        boilerplateExpresions.add(createdBoilerplateExpression.getId());
        Tag createdTag = testHelper.createTagViaWebSerivce("Test Tag", "Testing Web service", "Redacted", boilerplateExpresions);

        //retrieve the expression by requesting all expressions on the tag
        BoilerplateExpression[] returnedExpressions =
                testHelper.issueSuccessfulGetAndReturnMvcResult(testHelper.constructGetExpressionsByTagRequestUrl(createdTag.getId()), BoilerplateExpression[].class);
        Assert.assertEquals("Expecting one boilerplate expression returned.", 1, returnedExpressions.length);
        testHelper.compareBoilerplateExpressions(createdBoilerplateExpression, returnedExpressions[0]);
    }

    @Test
    public void getTagByExpression() throws Exception {
        //create expression for use with tag
        BoilerplateExpression createdBoilerplateExpression = testHelper.createExpressionViaWebService("Created expression", "This was created for test.", "aaaa", "test");

        //create tag with expression
        ArrayList<Long> boilerplateExpresions = new ArrayList<>();
        boilerplateExpresions.add(createdBoilerplateExpression.getId());
        Tag createdTag = testHelper.createTagViaWebSerivce("Test Tag", "Testing Web service", "Redacted", boilerplateExpresions);

        //retrieve the tag by requesting all tags for the created expression
        Tag[] returnedTags =
                testHelper.issueSuccessfulGetAndReturnMvcResult(testHelper.constructGetTagsByExpressionRequestUrl(createdBoilerplateExpression.getId()), Tag[].class);
        Assert.assertEquals("Expecting one tag returned.", 1, returnedTags.length);
        testHelper.compareTags(createdTag, returnedTags[0]);
    }

    @Test
    public void updateTag() throws Exception {
        //create tag
        Tag createdTag = testHelper.createTagViaWebSerivce("First name", "First description", "First replacement text", new ArrayList<>());
        //create expression for use on tag during update
        BoilerplateExpression createdBoilerplateExpression = testHelper.createExpressionViaWebService("Created expression", "This was created for test.", "aaaa", "test");
        ArrayList<Long> boilerplateExpresions = new ArrayList<>();
        boilerplateExpresions.add(createdBoilerplateExpression.getId());
        //update tag properties
        Tag updatedTag = testHelper.updateTagViaWebService(createdTag.getId(), "Second description", "Second description", "Second replacement text", boilerplateExpresions);
        //verify tags are different
        Assert.assertNotEquals("Expecting different names", createdTag.getName(), updatedTag.getName());
        Assert.assertNotEquals("Expecting different descriptions", createdTag.getDescription(), updatedTag.getDescription());
        Assert.assertNotEquals("Expecting different replacement text", createdTag.getDefaultReplacementText(), updatedTag.getDefaultReplacementText());
        Assert.assertNotEquals("Expecting different boilerplate expressions", createdTag.getBoilerplateExpressions().size(),
                updatedTag.getBoilerplateExpressions().size());
    }

    @Test
    public void updateTagWithIfMatchHeader() throws Exception {
        //create tag
        Tag createdTag = testHelper.createTagViaWebSerivce("First name", "First description", "First replacement text", new ArrayList<>());
        String expectedEtag = Integer.toString(createdTag.hashCode());

        //expecting failure due to incorrect etag passed, helper will check status is 412 (precondition failed)
        String serializedTag = testHelper.serializeItem(createdTag);
        testHelper.issuePreconditionFailedUpdate(serializedTag,
                testHelper.constructUpdateTagRequestUrl(createdTag.getId()), expectedEtag + "1341241");
        //pass correct etag, should succeed
        testHelper.issueIfMatchSuccessfulUpdateAndReturnMvcResult(serializedTag,
                testHelper.constructUpdateTagRequestUrl(createdTag.getId()), Tag.class, expectedEtag);
    }

    @Test
    public void updateExpressionWithIfMatchHeader() throws Exception {
        //create tag
        BoilerplateExpression createdExpression = testHelper.createExpressionViaWebService("Created expression", "This was created for test.", "aaaa", "test");
        String expectedEtag = Integer.toString(createdExpression.hashCode());

        //expecting failure due to incorrect etag passed, helper will check status is 412 (precondition failed)
        String serializedItem = testHelper.serializeItem(createdExpression);
        testHelper.issuePreconditionFailedUpdate(serializedItem,
                testHelper.constructUpdateExpressionRequestUrl(createdExpression.getId()), expectedEtag + "1341241");
        //pass correct etag, should succeed
        testHelper.issueIfMatchSuccessfulUpdateAndReturnMvcResult(serializedItem,
                testHelper.constructUpdateExpressionRequestUrl(createdExpression.getId()), BoilerplateExpression.class, expectedEtag);
    }

    @Test
    public void updateExpression() throws Exception {
        BoilerplateExpression createdExpression = testHelper.createExpressionViaWebService("Created expression 1", "This was created for test 1 .", "aaaa 1", "test 1");
        //update boilerplate expression
        BoilerplateExpression updatedExpression = testHelper.updateBoilerplateExpressionViaWebService(createdExpression.getId(), "Created expression 2", "Another description", "A new expression", "Some dummy text");
        Assert.assertNotEquals("Expecting different names", createdExpression.getName(), updatedExpression.getName());
        Assert.assertNotEquals("Expecting different descriptions", createdExpression.getDescription(), updatedExpression.getDescription());
        Assert.assertNotEquals("Expecting different replacement text", createdExpression.getReplacementText(), updatedExpression.getReplacementText());
        Assert.assertNotEquals("Expecting different expression", createdExpression.getExpression(), updatedExpression.getExpression());
    }

    @Test
    public void deleteTag() throws Exception {
        //create tag
        Tag createdTag = testHelper.createTagViaWebSerivce("First name", "First description", "First replacement text", new ArrayList<>());
        //delete the created tag
        Tag deletedTag = testHelper.deleteTagViaWebService(createdTag.getId(), "First name", "First description", "First replacement text", new ArrayList<>());
        //verify this matches the original created tag
        testHelper.compareTags(createdTag, deletedTag);
        //try to get the tag
        ErrorResponse response = testHelper.issueItemNotFoundGetAndReturnMvcResult(testHelper.constructGetTagRequestUrl(1L), ErrorResponse.class);
        LocalizedException expectedException = new LocalizedItemNotFoundException(LocalizedItemNotFoundErrors.TAG_NOT_FOUND);
        testHelper.checkErrorResponse(expectedException, response);
    }

    @Test
    public void deleteExpression() throws Exception {
        //create expression
        BoilerplateExpression createdExpression = testHelper.createExpressionViaWebService("Created expression 1", "This was created for test 1 .", "aaaa 1", "test 1");
        //delete expression
        BoilerplateExpression deletedExpression = testHelper.deleteBoilerplateExpressionViaWebService(createdExpression.getId(), "Created expression 1", "This was created for test 1 .", "aaaa 1", "test 1");
        testHelper.compareBoilerplateExpressions(createdExpression, deletedExpression);
        ErrorResponse response = testHelper.issueItemNotFoundGetAndReturnMvcResult(testHelper.constructGetBoilerplateExpressionRequestUrl(1L), ErrorResponse.class);
        LocalizedException expectedException = new LocalizedItemNotFoundException(LocalizedItemNotFoundErrors.EXPRESSION_NOT_FOUND);
        testHelper.checkErrorResponse(expectedException, response);
    }

    @Test
    public void updateNonExistantExpression() throws Exception {
        BoilerplateExpression expression = testHelper.createBoilerplateExpression("Dummy", "423423", "segseg", "fgegsege");
        String serializedEx = testHelper.serializeItem(expression);
        ErrorResponse response = testHelper.issueItemNotFoundUpdateAndReturnMvcResult(serializedEx, testHelper.constructUpdateExpressionRequestUrl(1L), ErrorResponse.class);
        LocalizedException expectedException = new LocalizedItemNotFoundException(LocalizedItemNotFoundErrors.EXPRESSION_NOT_FOUND);
        testHelper.checkErrorResponse(expectedException, response);
    }

    @Test
    public void updateNonExistantTag() throws Exception {
        Tag tag = testHelper.createTag("First name", "First description", "First replacement text", new ArrayList<>());
        String serializedEx = testHelper.serializeItem(tag);
        ErrorResponse response = testHelper.issueItemNotFoundUpdateAndReturnMvcResult(serializedEx, testHelper.constructUpdateTagRequestUrl(1L), ErrorResponse.class);
        LocalizedException expectedException = new LocalizedItemNotFoundException(LocalizedItemNotFoundErrors.TAG_NOT_FOUND);
        testHelper.checkErrorResponse(expectedException, response);
    }

    @Test
    public void deleteNonExistantTag() throws Exception {
        ErrorResponse response = testHelper.issueItemNotFoundDeleteAndReturnMvcResult(testHelper.constructDeleteTagRequestUrl(1L), ErrorResponse.class);
        LocalizedException expectedException = new LocalizedItemNotFoundException(LocalizedItemNotFoundErrors.TAG_NOT_FOUND);
        testHelper.checkErrorResponse(expectedException, response);
    }

    @Test
    public void deleteNonExistantExpression() throws Exception {
        ErrorResponse response = testHelper.issueItemNotFoundDeleteAndReturnMvcResult(testHelper.constructDeleteExpressionRequestUrl(1L), ErrorResponse.class);
        LocalizedException expectedException = new LocalizedItemNotFoundException(LocalizedItemNotFoundErrors.EXPRESSION_NOT_FOUND);
        testHelper.checkErrorResponse(expectedException, response);
    }

    @Test
    public void deleteExpressionInUseByTag() throws Exception {
        //should be able to delete expression and it will be removed from a tag that was using it
        //create expression
        BoilerplateExpression createdExpression = testHelper.createExpressionViaWebService("Created expression 1", "This was created for test 1 .", "aaaa 1", "test 1");
        ArrayList<Long> boilerplateExpressions = new ArrayList<>();
        boilerplateExpressions.add(createdExpression.getId());
        Tag createdTag = testHelper.createTagViaWebSerivce("Test Tag", "Testing Web service", "Redacted", boilerplateExpressions);
        //get the tag as it was stored on save
        Tag returnedTagByGet = testHelper.issueSuccessfulGetAndReturnMvcResult(testHelper.constructGetTagRequestUrl(createdTag.getId()), Tag.class);
        //compare the tag returned by create method with the tag returned by the get
        testHelper.compareTags(createdTag, returnedTagByGet);

        //delete the expression
        BoilerplateExpression deletedExpression = testHelper.deleteBoilerplateExpressionViaWebService(createdExpression.getId(), "Created expression 1", "This was created for test 1 .", "aaaa 1", "test 1");
        testHelper.compareBoilerplateExpressions(createdExpression, deletedExpression);
        ErrorResponse response = testHelper.issueItemNotFoundGetAndReturnMvcResult(testHelper.constructGetBoilerplateExpressionRequestUrl(1L), ErrorResponse.class);
        LocalizedException expectedException = new LocalizedItemNotFoundException(LocalizedItemNotFoundErrors.EXPRESSION_NOT_FOUND);
        testHelper.checkErrorResponse(expectedException, response);

        Tag returnedTagWithoutBoilerplateExpression = testHelper.issueSuccessfulGetAndReturnMvcResult(testHelper.constructGetTagRequestUrl(createdTag.getId()), Tag.class);
        Assert.assertEquals("Expecting no boilerplate expressions on tag anymore.", 0, returnedTagWithoutBoilerplateExpression.getBoilerplateExpressions().size());
    }

    @Test
    public void getPagedTags() throws Exception {
        //running this multiple times to verify the items come back in expected order every time
        for (int i = 0; i < 30; i++) {
            testHelper.project_id = UUID.randomUUID().toString();
            //create multiple tags
            int numTagsToCreate = 20;
            ArrayList<Tag> tags = new ArrayList<>();
            for (int index = 0; index < numTagsToCreate; index++) {
                Tag createdTag = testHelper.createTagViaWebSerivce("Test Tag", "Testing Web service", "Redacted", new ArrayList<>());
                tags.add(createdTag);
            }
            //retrieve the first 5 tags
            Tag[] returnedTags = testHelper.issueSuccessfulGetAndReturnMvcResult(testHelper.constructGetTagsPagedRequestUrl(1, 5), Tag[].class);
            //expecting 5 tags back
            Assert.assertEquals("Expecting 5 tags returned", 5, returnedTags.length);
            for (int index = 0; index < returnedTags.length; index++) {
                testHelper.compareTags(tags.get(index), returnedTags[index]);
            }

            //retrieve last 4 tags (asking for more than available)
            int expectedFromEnd = 4;
            returnedTags = testHelper.issueSuccessfulGetAndReturnMvcResult(testHelper.constructGetTagsPagedRequestUrl(numTagsToCreate - 3, 5), Tag[].class);
            Assert.assertEquals("Expecting 4 tags returned", expectedFromEnd, returnedTags.length);
            for (int index = 0; index < returnedTags.length; index++) {
                testHelper.compareTags(tags.get(index + (20 - expectedFromEnd)), returnedTags[index]);
            }
        }
    }

    @Test
    public void getPagedExpressions() throws Exception {
        for (int i = 0; i < 30; i++) {
            testHelper.project_id = UUID.randomUUID().toString();
            //create multiple expressions
            int numExpressionsToCreate = 20;
            ArrayList<BoilerplateExpression> expressions = new ArrayList<>();
            for (int index = 0; index < numExpressionsToCreate; index++) {
                BoilerplateExpression createdExpression = testHelper.createExpressionViaWebService("Created expression 1", "This was created for test 1 .", "aaaa 1", "test 1");
                expressions.add(createdExpression);
            }
            //retrieve the first 5 expressions
            BoilerplateExpression[] returnedExpression = testHelper.issueSuccessfulGetAndReturnMvcResult(testHelper.constructGetExpressionsPagedRequestUrl(1, 5), BoilerplateExpression[].class);
            //expecting 5 tags back
            Assert.assertEquals("Expecting 5 expressions returned", 5, returnedExpression.length);
            for (int index = 0; index < returnedExpression.length; index++) {
                testHelper.compareBoilerplateExpressions(expressions.get(index), returnedExpression[index]);
            }
            //retrieve last 4 expressions (asking for more than available)
            int expectedFromEnd = 4;
            returnedExpression = testHelper.issueSuccessfulGetAndReturnMvcResult(testHelper.constructGetExpressionsPagedRequestUrl(numExpressionsToCreate - 3, 5), BoilerplateExpression[].class);
            Assert.assertEquals("Expecting 4 expressions returned", expectedFromEnd, returnedExpression.length);
            for (int index = 0; index < returnedExpression.length; index++) {
                testHelper.compareBoilerplateExpressions(expressions.get(index + (20 - expectedFromEnd)), returnedExpression[index]);
            }
        }
    }

    @Test
    public void getPagedExpressionsByTagId() throws Exception {
        for (int i = 0; i < 30; i++) {
            testHelper.project_id = UUID.randomUUID().toString();
            //create multiple expressions
            int numExpressionsToCreate = 20;
            ArrayList<BoilerplateExpression> expressions = new ArrayList<>();
            for (int index = 0; index < numExpressionsToCreate; index++) {
                BoilerplateExpression createdExpression = testHelper.createExpressionViaWebService("Created expression 1", "This was created for test 1 .", "aaaa 1", "test 1");
                expressions.add(createdExpression);
            }

            //create tag
            int expressionsOnTag = 15;
            Collection<Long> expressionIds = expressions.stream().map(BoilerplateExpression::getId).limit(expressionsOnTag).collect(Collectors.toList());
            //assign first 15 to tag
            Tag createdTag = testHelper.createTagViaWebSerivce("Test Tag", "Testing Web service", "Redacted", expressionIds);
            //get first 5 expressions
            BoilerplateExpression[] returnedExpression = testHelper.issueSuccessfulGetAndReturnMvcResult(testHelper.constructGetExpressionsByTagPagedRequestUrl(createdTag.getId(), 1, 5), BoilerplateExpression[].class);
            Assert.assertEquals("Expecting 5 expressions returned", 5, returnedExpression.length);
            for (int index = 0; index < returnedExpression.length; index++) {
                testHelper.compareBoilerplateExpressions(expressions.get(index), returnedExpression[index]);
            }

            //retrieve last 4 expressions on tag (asking for more than available)
            int expectedFromEnd = 4;
            returnedExpression = testHelper.issueSuccessfulGetAndReturnMvcResult(testHelper.constructGetExpressionsByTagPagedRequestUrl(createdTag.getId(), expressionsOnTag - 3, 5), BoilerplateExpression[].class);
            Assert.assertEquals("Expecting 4 expressions returned", expectedFromEnd, returnedExpression.length);
            for (int index = 0; index < returnedExpression.length; index++) {
                //compareBoilerplateExpressions(expressions.get(index + (expressionsOnTag - expectedFromEnd)), returnedExpression[index]);
                BoilerplateExpression returnedExpressionObject = returnedExpression[index];
                BoilerplateExpression matchedOriginalExpression = expressions.stream().filter(e -> e.getId().equals(returnedExpressionObject.getId())).findFirst().get();

                Assert.assertNotNull("Expecting to find a match between returned page expressions and those set on tag ", matchedOriginalExpression == null);
                testHelper.compareBoilerplateExpressions(matchedOriginalExpression, returnedExpressionObject);
            }
        }
    }
}