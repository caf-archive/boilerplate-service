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
package com.hpe.caf.boilerplate.web;

import com.hpe.caf.boilerplate.api.BoilerplateApi;
import com.hpe.caf.boilerplate.api.BoilerplateExpression;
import com.hpe.caf.boilerplate.api.Tag;
import com.hpe.caf.boilerplate.api.exceptions.InvalidRequestException;
import com.hpe.caf.boilerplate.api.exceptions.ItemNotFoundException;
import com.hpe.caf.boilerplate.web.exceptions.ErrorCodes;
import com.hpe.caf.boilerplate.web.exceptions.ErrorResponse;
import com.hpe.caf.boilerplate.web.exceptions.ExceptionHelper;
import com.hpe.caf.boilerplate.web.setup.etags.ETagSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.UUID;

/**
 * Created by Michael.McAlynn on 08/12/2015.
 */
@RequestMapping(value = "/boilerplate")
@RestController
@Api(value = "/boilerplate", description = "Boilerplate CRUD operations")
public class BoilerplateApiController {
    public static final Logger logger = LoggerFactory.getLogger(BoilerplateApiController.class);
    private final BoilerplateApi boilerplateApi;

    @Autowired
    public BoilerplateApiController(BoilerplateApi boilerplateApi) {
        this.boilerplateApi = boilerplateApi;
    }

    /*
        Method that can be called to verify the web service is running.
     */
    @RequestMapping(value = "/checkhealth")
    public boolean checkHealth() {
        return true;
    }

    //get tag by id
    @ETagSupport(eTagRetriever = TagCurrentETagRetriever.class)
    @ApiOperation(value = "Get tag by ID",
            authorizations = @Authorization(value = "api_key")
    )
    @RequestMapping(method = {RequestMethod.GET}, value = "/tag/{id}")
    public Tag getTag(@ApiParam @PathVariable(value = Constants.MethodParameters.Shared.TAG_ID) Long id) {
        return this.boilerplateApi.getTag(id);
    }

    //get tags

    @ApiOperation(value = "Gets all the tags or a page of tags",
            response = Tag[].class,
            authorizations = @Authorization(value = "api_key")
    )
    @RequestMapping(method = {RequestMethod.GET}, value = "/tags")
    public Collection<Tag> getTags(@RequestParam(value = "page", required = false, defaultValue = "-1") int pageNumber,
                                   @RequestParam(value = "size", required = false, defaultValue = "20") int pageSize) throws Exception {
        if (pageNumber < 1) {
            return this.boilerplateApi.getTags();
        }
        return this.boilerplateApi.getTags(pageNumber, pageSize);
    }

    //get expression by id
    @ETagSupport(eTagRetriever = BoilerplateExpressionCurrentETagRetriever.class)
    @RequestMapping(method = {RequestMethod.GET}, value = "/expression/{id}")
    @ApiOperation(value = "Gets a BoilerplateExpression by ID",
            authorizations = @Authorization(value = "api_key")
    )
    public BoilerplateExpression getExpression(@PathVariable(value = Constants.MethodParameters.Shared.EXPRESSION_ID) Long id) {
        return this.boilerplateApi.getExpression(id);
    }

    //get a page of expressions
    @RequestMapping(method = {RequestMethod.GET}, value = "/expressions")
    @ApiOperation(value = "Gets all the BoilerplateExpressions or a page of BoilerplateExpressions",
            response = BoilerplateExpression[].class,
            authorizations = @Authorization(value = "api_key")
    )
    public Collection<BoilerplateExpression> getExpressions(@RequestParam(value = "page") int pageNumber,
                                                            @RequestParam(value = "size", required = false, defaultValue = "20") int pageSize) throws Exception {
        if (pageNumber < 1) {
            pageNumber = 1;
        }

        return this.boilerplateApi.getExpressions(pageNumber, pageSize);
    }

    //get the tags for an expression id
    @RequestMapping(method = {RequestMethod.GET}, value = "/expression/{id}/tags")
    @ApiOperation(value = "Gets the Tags used by an expression",
            response = Tag[].class,
            authorizations = @Authorization(value = "api_key")
    )
    public Collection<Tag> getTagsByExpression(@PathVariable(value = "id") Long id) {
        return this.boilerplateApi.getTagsByExpressionId(id);
    }

    //get a page of expressions by tag id
    @RequestMapping(method = {RequestMethod.GET}, value = "/tag/{id}/expressions")
    @ApiOperation(value = "Gets a page of expressions for a tag",
            response = BoilerplateExpression[].class,
            authorizations = @Authorization(value = "api_key")
    )
    public Collection<BoilerplateExpression> getExpressionsByTagPaged(@PathVariable(value = "id") Long tagId,
                                                                      @RequestParam(value = "page", required = false, defaultValue = "-1") int pageNumber,
                                                                      @RequestParam(value = "size", required = false, defaultValue = "20") int pageSize) {
        if (pageNumber < 1) {
            return this.boilerplateApi.getExpressionsByTagId(tagId);
        }
        return this.boilerplateApi.getExpressionsByTagId(tagId, pageNumber, pageSize);
    }

    //create tag
    @ETagSupport(eTagRetriever = TagCurrentETagRetriever.class)
    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method = {RequestMethod.POST}, value = "/tag")
    @ApiOperation(value = "Creates a tag",
            authorizations = @Authorization(value = "api_key")
    )
    public Tag createTag(@RequestBody Tag tag) {
        return this.boilerplateApi.createTag(tag);
    }

    //create expression
    @ETagSupport(eTagRetriever = BoilerplateExpressionCurrentETagRetriever.class)
    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method = {RequestMethod.POST}, value = "/expression")
    @ApiOperation(value = "Creates an expression",
            authorizations = @Authorization(value = "api_key")
    )
    public BoilerplateExpression createExpression(@RequestBody BoilerplateExpression expression) {
        return this.boilerplateApi.createExpression(expression);
    }

    //update tag by id
    @ETagSupport(eTagRetriever = TagCurrentETagRetriever.class)
    @RequestMapping(method = {RequestMethod.PUT}, value = "/tag/{id}")
    @ApiOperation(value = "Updates a tag",
            authorizations = @Authorization(value = "api_key")
    )
    public Tag updateTag(@RequestBody Tag tag, @PathVariable(value = Constants.MethodParameters.Shared.TAG_ID) Long tagId) {
        tag.setId(tagId);
        return this.boilerplateApi.updateTag(tag);
    }

    //update expression by id
    @ETagSupport(eTagRetriever = BoilerplateExpressionCurrentETagRetriever.class)
    @RequestMapping(method = {RequestMethod.PUT}, value = "/expression/{id}")
    @ApiOperation(value = "Updates an expression",
            authorizations = @Authorization(value = "api_key")
    )
    public BoilerplateExpression updateExpression(@RequestBody BoilerplateExpression expression,
                                                  @PathVariable(value = Constants.MethodParameters.Shared.EXPRESSION_ID) Long expressionId) {
        expression.setId(expressionId);
        return this.boilerplateApi.updateExpression(expression);
    }

    //delete tag by id
    @ETagSupport(eTagRetriever = TagCurrentETagRetriever.class)
    @RequestMapping(method = {RequestMethod.DELETE}, value = "/tag/{id}")
    @ApiOperation(value = "Deletes a tag",
            authorizations = @Authorization(value = "api_key")
    )
    public Tag deleteTag(@PathVariable(value = "id") Long id) {
        return this.boilerplateApi.deleteTag(id);
    }

    //delete expression by id
    @ETagSupport(eTagRetriever = BoilerplateExpressionCurrentETagRetriever.class)
    @RequestMapping(method = {RequestMethod.DELETE}, value = "/expression/{id}")
    @ApiOperation(value = "Deletes an expression",
            authorizations = @Authorization(value = "api_key")
    )
    public BoilerplateExpression deleteExpression(@PathVariable(value = "id") Long id) {
        return this.boilerplateApi.deleteExpression(id);
    }

    @ExceptionHandler(ItemNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse defaultErrorHandler(HttpServletRequest request, HttpServletResponse response, ItemNotFoundException e) {
        return ExceptionHelper.outputItemNotFoundException(response, e.getType(), e);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse defaultErrorHandler(HttpServletRequest request, HttpServletResponse response, Exception e) {
        return new ErrorResponse(ErrorCodes.GENERIC_ERROR.getValue(), e.getLocalizedMessage(), e.getMessage(), UUID.randomUUID());
    }

    @ExceptionHandler(InvalidRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse defaultErrorHandler(HttpServletRequest request, HttpServletResponse response, InvalidRequestException e) {
        return ExceptionHelper.outputInvalidRequestException(response, e.getType(), e);
    }
}
