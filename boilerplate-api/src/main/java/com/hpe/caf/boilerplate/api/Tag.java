/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development LP.
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

import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.Collection;

/**
 * Created by Michael.McAlynn on 08/12/2015.
 */
public class Tag extends DtoBase {
    private Long id;
    private String name;
    private String description;
    private String defaultReplacementText;
    private Collection<Long> boilerplateExpressions;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDefaultReplacementText() {
        return defaultReplacementText;
    }

    public void setDefaultReplacementText(String defaultReplacementText) {
        this.defaultReplacementText = defaultReplacementText;
    }

    public Collection<Long> getBoilerplateExpressions() {
        return boilerplateExpressions;
    }

    public void setBoilerplateExpressions(Collection<Long> boilerplateExpressions) {
        this.boilerplateExpressions = boilerplateExpressions;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37)
                .append(id)
                .append(name)
                .append(description)
                .append(defaultReplacementText);

        //add boilerplate expression ids
        if(boilerplateExpressions!=null) {
            boilerplateExpressions.forEach(builder::append);
        }
        return builder.toHashCode();
    }
}
