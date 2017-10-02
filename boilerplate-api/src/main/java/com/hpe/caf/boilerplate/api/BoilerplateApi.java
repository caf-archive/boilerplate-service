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
package com.hpe.caf.boilerplate.api;

import java.beans.Expression;
import java.util.List;

/**
 * Created by Michael.McAlynn on 08/12/2015.
 */
public interface BoilerplateApi {
    /*
        Returns the tag with the matching 'id'.
     */
    Tag getTag(long id);

    /*
        Returns all tags.
     */
    List<Tag> getTags();

    /*
        Returns a page of tags, starting at 'pageNumber' and returning 'size' number of Tags.
     */
    List<Tag> getTags(int pageNumber, int size) throws Exception;

    /*
        Returns the expression with the matching 'id'.
    */
    BoilerplateExpression getExpression(long id);

    /*
        Returns a page of expressions, starting at 'pageNumber' and returning 'size' number of expressions.
     */
    List<BoilerplateExpression> getExpressions(int pageNumber, int size) throws Exception;
    /*
        Returns the tags for an expression id.
     */
    List<Tag> getTagsByExpressionId(long id);

    /*
        Returns the expressions for a tag id.
     */
    List<BoilerplateExpression> getExpressionsByTagId(long id);

    /*
        Returns a page of expressions for a tag id.
     */
    List<BoilerplateExpression> getExpressionsByTagId(long id, int pageNumber, int size);

    /*
        Creates a tag and returns the new tag.
    */
    Tag createTag(Tag tag);

    /*
        Creates an expressions and returns the new expression.
     */
    BoilerplateExpression createExpression(BoilerplateExpression expression);

    /*
        Update stored tag with tag object data passed in. Returns updated tag.
    */
    Tag updateTag(Tag tag);

    /*
        Update stored expression with expression object data passed in. Returns updated expression.
    */
    BoilerplateExpression updateExpression(BoilerplateExpression expression);

    /*
        Deletes the tag with a matching id. Returns the deleted tag.
     */
    Tag deleteTag(long id);
    /*
        Deletes the expression with a matching id. Returns the deleted expression.
     */
    BoilerplateExpression deleteExpression(long id);
}
