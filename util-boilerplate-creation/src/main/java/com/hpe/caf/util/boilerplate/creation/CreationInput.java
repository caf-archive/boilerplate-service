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

import com.hpe.caf.boilerplate.webcaller.model.Tag;

import java.util.Collection;

/**
 * Represents the input paramters to use in expression and tag creation.
 */
public class CreationInput {
    private String projectId;
    private Collection<CreationExpression> expressions;
    private Collection<Tag> tags;

    public CreationInput(){}

    public String getProjectId(){return this.projectId;};
    public void setProjectId(String projectId){this.projectId = projectId;}

    public Collection<CreationExpression> getExpressions(){return this.expressions;}
    public void setExpressions(Collection<CreationExpression> expressions){this.expressions = expressions;}

    public Collection<Tag> getTags(){return this.tags;}
    public void setTags(){this.tags = tags;}
}
