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
import java.util.stream.Collectors;

/**
 * Holds the result of creating the set of expressions and tags.
 */
public class CreationResult {
    private String projectId;
    private Collection<CreationResultOutput> expressions;
    private Collection<CreationResultOutput> tags;

    public CreationResult(String projectId, Collection<CreationExpression> expressions, Collection<Tag> tags){
        this.projectId = projectId;
        this.expressions = expressions.stream().map(e -> new CreationResultOutput(e.getId(), e.getName())).collect(Collectors.toList());;
        this.tags = tags.stream().map(t -> new CreationResultOutput(t.getId(), t.getName())).collect(Collectors.toList());
    }

    public Collection<CreationResultOutput> getTags(){
        return tags;
    }

    public void setTags(Collection<CreationResultOutput> tags){
        this.tags = tags;
    }

    public Collection<CreationResultOutput> getExpressions(){
        return expressions;
    }

    public void setExpressions(Collection<CreationResultOutput> boilerplateExpressions){
        this.expressions = boilerplateExpressions;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

}
