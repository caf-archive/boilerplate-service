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

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Basic empty implementation of User Context for spring resolution. Consumers requiring specific tenant functionality should implement UserContext and exclude this bean.
 */
public class ThreadUserContext implements UserContext{
    private String projectId = null;

    @Override
    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId){
        this.projectId = projectId;
    }
}
