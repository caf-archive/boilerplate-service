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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hpe.caf.boilerplate.api.BoilerplateExpression;
import com.hpe.caf.boilerplate.api.Tag;
import com.hpe.caf.boilerplate.web.exceptions.ErrorResponse;
import com.hpe.caf.boilerplate.web.exceptions.LocalizedException;
import org.junit.Assert;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collection;

/**
 * Created by Michael.McAlynn on 16/12/2015.
 */
public class IntegrationTestHelper {
    WebApplicationContext wac;

    MockHttpSession session;

    MockHttpServletRequest request;

    private MockMvc mockMvc;
    public String project_id;

    private final String BOILERPLATE_CREATE_URL;
    private final String TAG_CREATE_URL;

    public IntegrationTestHelper(String projectId, MockMvc mockMvc, MockHttpServletRequest request, MockHttpSession session, WebApplicationContext wac,
                                 String boilerplateCreateUrl, String tagCreateUrl){
        this.project_id = projectId;
        this.mockMvc = mockMvc;
        this.request = request;
        this.session = session;
        this.wac = wac;

        this.BOILERPLATE_CREATE_URL = boilerplateCreateUrl;
        this.TAG_CREATE_URL = tagCreateUrl;
    }

    public Tag createTag(String name, String description, String defaultReplacementText, Collection<Long> expressions){
        Tag tagToCreate = new Tag();
        tagToCreate.setName(name);
        tagToCreate.setDescription(description);
        tagToCreate.setDefaultReplacementText(defaultReplacementText);
        tagToCreate.setBoilerplateExpressions(expressions);
        return tagToCreate;
    }

    public String serializeItem(Object item) throws JsonProcessingException {
        String serializedTag = new ObjectMapper().writeValueAsString(item);
        return serializedTag.replace("\"project_id\":null", "\"project_id\":\""+this.project_id+"\"");
    }

    public String getProjectIdParamForGet(){
        return "project_id="+project_id;
    }

    public String constructGetTagRequestUrl(Long id){
        return "/boilerplate/tag/" + id + "/?" + getProjectIdParamForGet();
    }

    public String constructGetBoilerplateExpressionRequestUrl(Long id){
        return "/boilerplate/expression/"+id+"/?" + getProjectIdParamForGet();
    }

    public String constructGetExpressionsByTagRequestUrl(Long id){
        return "/boilerplate/tag/" + id + "/expressions/?"+ getProjectIdParamForGet();
    }

    public String constructGetTagsByExpressionRequestUrl(Long id){
        return "/boilerplate/expression/" + id + "/tags/?"+ getProjectIdParamForGet();
    }

    public String constructUpdateTagRequestUrl(Long id){
        return "/boilerplate/tag/" + id;
    }

    public String constructUpdateExpressionRequestUrl(Long id){
        return "/boilerplate/expression/" + id;
    }

    public String constructDeleteTagRequestUrl(Long id){
        return "/boilerplate/tag/" + id +"/?" + getProjectIdParamForGet();
    }

    public String constructDeleteExpressionRequestUrl(Long id){
        return "/boilerplate/expression/" + id +"/?" + getProjectIdParamForGet();
    }

    public String constructGetTagsPagedRequestUrl(int pageNumber, Integer pageSize){
        String pageRequestUrl = "/boilerplate/tags?" + getProjectIdParamForGet()  + "&page="+pageNumber;
        if(pageSize != null){
            pageRequestUrl = pageRequestUrl + "&size="+pageSize;
        }
        return pageRequestUrl;
    }

    public String constructGetExpressionsPagedRequestUrl(int pageNumber, Integer pageSize){
        String pageRequestUrl = "/boilerplate/expressions?" + getProjectIdParamForGet()  + "&page="+pageNumber;
        if(pageSize != null){
            pageRequestUrl = pageRequestUrl + "&size="+pageSize;
        }
        return pageRequestUrl;
    }

    public String constructGetExpressionsByTagPagedRequestUrl(Long id, int pageNumber, Integer pageSize){
        String pageRequestUrl = constructGetExpressionsByTagRequestUrl(id) + "&page="+pageNumber;
        if(pageSize != null){
            pageRequestUrl = pageRequestUrl + "&size="+pageSize;
        }
        return pageRequestUrl;
    }

    public MvcResult issueSuccessfulCreate(String serializedData, String url) throws Exception {
        return this.mockMvc.perform(MockMvcRequestBuilders.post(url+"?"+getProjectIdParamForGet())
                .contentType(MediaType.APPLICATION_JSON)
                .content(serializedData).accept(MediaType.ALL))
                .andExpect(MockMvcResultMatchers.status().isCreated()) //verify that 201 status returned by create
                .andReturn();
    }

    public MvcResult issueSuccessfulUpdate(String serializedData, String url) throws Exception {
        return this.mockMvc.perform(MockMvcRequestBuilders.put(url+"?"+getProjectIdParamForGet())
                .contentType(MediaType.APPLICATION_JSON)
                .content(serializedData).accept(MediaType.ALL))
                .andExpect(MockMvcResultMatchers.status().isOk()) //verify that 200 status returned by update
                .andReturn();
    }

    public MvcResult issueIfMatchSuccessfulUpdate(String serializedData, String url, String ifMatchValue) throws Exception {
        return this.mockMvc.perform(MockMvcRequestBuilders.put(url+"?"+getProjectIdParamForGet())
                .contentType(MediaType.APPLICATION_JSON)
                .header("If-Match", ifMatchValue)
                .content(serializedData).accept(MediaType.ALL))
                .andExpect(MockMvcResultMatchers.status().isOk()) //verify that 200 status returned by update
                .andReturn();
    }

    public MvcResult issuePreconditionFailedUpdate(String serializedData, String url, String ifMatchValue) throws Exception {
        return this.mockMvc.perform(MockMvcRequestBuilders.put(url+"?"+getProjectIdParamForGet())
                .contentType(MediaType.APPLICATION_JSON)
                .content(serializedData).accept(MediaType.ALL)
                .header("If-Match", ifMatchValue))
                .andExpect(MockMvcResultMatchers.status().isPreconditionFailed())
                .andReturn();
    }

    public MvcResult issueSuccessfulDelete(String requestString) throws Exception {
        return this.mockMvc.perform(MockMvcRequestBuilders.delete(requestString)
                .accept(MediaType.ALL)).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
    }

    public MvcResult issueSuccessfulGet(String requestString) throws Exception {
        return this.mockMvc.perform(MockMvcRequestBuilders.get(requestString)
                .accept(MediaType.ALL)).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
    }

    public MvcResult issueItemNotFoundGet(String requestString) throws Exception {
        return this.mockMvc.perform(MockMvcRequestBuilders.get(requestString)
                .accept(MediaType.ALL)).andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn();
    }

    public MvcResult issueItemNotFoundCreate(String serializedData, String url) throws Exception {
        return this.mockMvc.perform(MockMvcRequestBuilders.post(url+"?"+getProjectIdParamForGet())
                .contentType(MediaType.APPLICATION_JSON)
                .content(serializedData).accept(MediaType.ALL))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn();
    }

    public MvcResult issueItemNotFoundUpdate(String serializedData, String url) throws Exception {
        return this.mockMvc.perform(MockMvcRequestBuilders.put(url+"?"+getProjectIdParamForGet())
                .contentType(MediaType.APPLICATION_JSON)
                .content(serializedData).accept(MediaType.ALL))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn();
    }

    public MvcResult issueItemNotFoundDelete(String requestString) throws Exception {
        return this.mockMvc.perform(MockMvcRequestBuilders.get(requestString)
                .accept(MediaType.ALL)).andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn();
    }

    public BoilerplateExpression createBoilerplateExpression(String name, String description, String expression, String replacementText){
        BoilerplateExpression boilerplateExpression = new BoilerplateExpression();
        boilerplateExpression.setName(name);
        boilerplateExpression.setDescription(description);
        boilerplateExpression.setExpression(expression);
        boilerplateExpression.setReplacementText(replacementText);
        return boilerplateExpression;
    }

    public <T> T issueSuccessfulCreateAndReturnMvcResult(String serializeditem, String url, Class<T> classToConvertTo) throws Exception {
        MvcResult getResult = issueSuccessfulCreate(serializeditem, url);
        return new ObjectMapper().readValue(getResult.getResponse().getContentAsString(), classToConvertTo);
    }

    public <T> T issueItemNotFoundCreateAndReturnMvcResult(String serializeditem, String url, Class<T> classToConvertTo) throws Exception {
        MvcResult getResult = issueItemNotFoundCreate(serializeditem, url);
        return new ObjectMapper().readValue(getResult.getResponse().getContentAsString(), classToConvertTo);
    }

    public <T> T issueSuccessfulUpdateAndReturnMvcResult(String serializeditem, String url, Class<T> classToConvertTo) throws Exception {
        MvcResult getResult = issueSuccessfulUpdate(serializeditem, url);
        return new ObjectMapper().readValue(getResult.getResponse().getContentAsString(), classToConvertTo);
    }

    public <T> T issueIfMatchSuccessfulUpdateAndReturnMvcResult(String serializeditem, String url, Class<T> classToConvertTo, String ifMatchValue) throws Exception {
        MvcResult getResult = issueIfMatchSuccessfulUpdate(serializeditem, url, ifMatchValue);
        return new ObjectMapper().readValue(getResult.getResponse().getContentAsString(), classToConvertTo);
    }

    /*public <T> T issuePreconditionFailedUpdateAndReturnMvcResult(String serializeditem, String url, Class<T> classToConvertTo) throws Exception {
        MvcResult getResult = issuePreconditionFailedUpdate(serializeditem, url);
        return new ObjectMapper().readValue(getResult.getResponse().getContentAsString(), classToConvertTo);
    }*/

    public <T> T issueItemNotFoundUpdateAndReturnMvcResult(String serializeditem, String url, Class<T> classToConvertTo) throws Exception {
        MvcResult getResult = issueItemNotFoundUpdate(serializeditem, url);
        return new ObjectMapper().readValue(getResult.getResponse().getContentAsString(), classToConvertTo);
    }

    public <T> T issueSuccessfulDeleteAndReturnMvcResult(String url, Class<T> classToConvertTo) throws Exception {
        MvcResult getResult = issueSuccessfulDelete(url);
        return new ObjectMapper().readValue(getResult.getResponse().getContentAsString(), classToConvertTo);
    }

    public <T> T issueItemNotFoundDeleteAndReturnMvcResult(String url, Class<T> classToConvertTo) throws Exception {
        MvcResult getResult = issueItemNotFoundDelete(url);
        return new ObjectMapper().readValue(getResult.getResponse().getContentAsString(), classToConvertTo);
    }

    public <T> T issueSuccessfulGetAndReturnMvcResult(String url, Class<T> classToConvertTo) throws Exception {
        MvcResult getResult = issueSuccessfulGet(url);
        return new ObjectMapper().readValue(getResult.getResponse().getContentAsString(), classToConvertTo);
    }

    public <T> T issueItemNotFoundGetAndReturnMvcResult(String url, Class<T> classToConvertTo) throws Exception {
        MvcResult getResult = issueItemNotFoundGet(url);
        return new ObjectMapper().readValue(getResult.getResponse().getContentAsString(), classToConvertTo);
    }

    public Tag createTagViaWebSerivce(String name, String description, String defaultReplacementText, Collection<Long> expressions) throws Exception {
        Tag tagToCreate = createTag(name, description, defaultReplacementText, expressions);
        tagToCreate.setId(-1L);
        String serializedTag = serializeItem(tagToCreate);
        Tag createdTag = issueSuccessfulCreateAndReturnMvcResult(serializedTag, TAG_CREATE_URL, Tag.class);
        //compare the tag returned by create with the object sent to be created
        compareTags(tagToCreate, createdTag, true);
        return createdTag;
    }

    public Tag updateTagViaWebService(Long id, String name, String description, String defaultReplacementText, Collection<Long> expressions) throws Exception {
        Tag tagToUpdate = createTag(name, description, defaultReplacementText, expressions);
        tagToUpdate.setId(id);
        String serializedTag = serializeItem(tagToUpdate);
        Tag updatedTag = issueSuccessfulUpdateAndReturnMvcResult(serializedTag, constructUpdateTagRequestUrl(id), Tag.class);
        compareTags(tagToUpdate, updatedTag);
        return updatedTag;
    }

    public Tag deleteTagViaWebService(Long id, String name, String description, String defaultReplacementText, Collection<Long> expressions) throws Exception {
        Tag tagToDelete = createTag(name, description, defaultReplacementText, expressions);
        tagToDelete.setId(id);
        Tag deletedTag = issueSuccessfulDeleteAndReturnMvcResult(constructDeleteTagRequestUrl(id), Tag.class);
        compareTags(tagToDelete, deletedTag);
        return deletedTag;
    }

    public BoilerplateExpression updateBoilerplateExpressionViaWebService(Long id, String name, String description, String expression, String replacementText) throws Exception {
        BoilerplateExpression expressionToUpdate = createBoilerplateExpression(name, description, expression, replacementText);
        expressionToUpdate.setId(id);
        String serializedEx = serializeItem(expressionToUpdate);
        BoilerplateExpression updatedExpression =
                issueSuccessfulUpdateAndReturnMvcResult(serializedEx, constructUpdateExpressionRequestUrl(id), BoilerplateExpression.class);
        compareBoilerplateExpressions(expressionToUpdate, updatedExpression);
        return updatedExpression;
    }

    public BoilerplateExpression deleteBoilerplateExpressionViaWebService(Long id, String name, String description, String expression, String replacementText) throws Exception {
        BoilerplateExpression expressionToDelete = createBoilerplateExpression(name, description, expression, replacementText);
        expressionToDelete.setId(id);
        BoilerplateExpression deletedExpression = issueSuccessfulDeleteAndReturnMvcResult(constructDeleteExpressionRequestUrl(id), BoilerplateExpression.class);
        compareBoilerplateExpressions(expressionToDelete, deletedExpression);
        return deletedExpression;
    }

    public BoilerplateExpression createExpressionViaWebService(String name, String description, String expression, String replacementText) throws Exception {
        BoilerplateExpression expressionToCreate = createBoilerplateExpression(name, description, expression, replacementText);
        expressionToCreate.setId(-1L);
        String serializedBoilerplateExpression = serializeItem(expressionToCreate);
        BoilerplateExpression createdBoilerplateExpression = issueSuccessfulCreateAndReturnMvcResult(serializedBoilerplateExpression,
                BOILERPLATE_CREATE_URL, BoilerplateExpression.class);

        compareBoilerplateExpressions(expressionToCreate, createdBoilerplateExpression, true);
        return createdBoilerplateExpression;
    }

    public static void checkErrorResponse(LocalizedException expectedException, ErrorResponse receivedResponse){
        Assert.assertEquals("Expecting same error code.", expectedException.getErrorCode(), receivedResponse.error);
        //have to remove the correlation code as they will be different
        Assert.assertEquals("Expecting localized message to match received reason.", expectedException.getLocalizedMessage().split("\\.")[0], receivedResponse.reason.split("\\.")[0]);
    }

    public static void compareTags(Tag expectedTag, Tag returnedTag){
        compareTags(expectedTag, returnedTag, false);
    }

    public static void compareTags(Tag expectedTag, Tag returnedTag, boolean createdIdCheck){
        //verify that id's on tags are different (e.g. when creating tag we send it with null ID and the returned created tag has ID assigned by database)
        if(createdIdCheck){
            Assert.assertNotEquals("Expecting tags to have different ID.", expectedTag.getId(), returnedTag.getId());
        }
        else{
            Assert.assertEquals("Expecting tags to have same ID.", expectedTag.getId(), returnedTag.getId());
        }
        Assert.assertEquals("Expecting tags to have same name.", expectedTag.getName(), returnedTag.getName());
        Assert.assertEquals("Expecting tags to have same description.", expectedTag.getDescription(), returnedTag.getDescription());
        Assert.assertEquals("Expecting tags to have same default replacement text.", expectedTag.getDefaultReplacementText(), returnedTag.getDefaultReplacementText());
        Assert.assertTrue("Expecting boilerplate expressions to contain same ids", returnedTag.getBoilerplateExpressions().containsAll(expectedTag.getBoilerplateExpressions()));
    }

    public static void compareBoilerplateExpressions(BoilerplateExpression expectedBExpression, BoilerplateExpression returnedBExpression){
        compareBoilerplateExpressions(expectedBExpression, returnedBExpression, false);
    }

    public static void compareBoilerplateExpressions(BoilerplateExpression expectedBExpression, BoilerplateExpression returnedBExpression, boolean createdIdCheck){
        //verify that id's on expressions are different (e.g. when creating expressions we send it with null ID and the returned created expressions has ID assigned by database)
        if(createdIdCheck){
            Assert.assertNotEquals("Expecting boilerplate expressions to have different ID.", expectedBExpression.getId(), returnedBExpression.getId());
        }
        else{
            Assert.assertEquals("Expecting boilerplate expressions to have same ID.", expectedBExpression.getId(), returnedBExpression.getId());
        }
        Assert.assertEquals("Expecting boilerplate expressions to have same name.", expectedBExpression.getName(), returnedBExpression.getName());
        Assert.assertEquals("Expecting boilerplate expressions to have same description.", expectedBExpression.getDescription(), returnedBExpression.getDescription());
        Assert.assertEquals("Expecting boilerplate expressions to have same replacement text.", expectedBExpression.getReplacementText(), returnedBExpression.getReplacementText());
        Assert.assertEquals("Expecting boilerplate expressions to have same ", expectedBExpression.getExpression(), returnedBExpression.getExpression());
    }
}
