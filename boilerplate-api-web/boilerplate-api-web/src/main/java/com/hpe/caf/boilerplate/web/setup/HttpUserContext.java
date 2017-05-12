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
package com.hpe.caf.boilerplate.web.setup;

import com.hpe.caf.boilerplate.api.UserContext;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Michael.McAlynn on 09/12/2015.
 */
@Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
@Component
public class HttpUserContext implements UserContext {
    private final HttpServletRequest httpServletRequest;

    @Autowired
    public HttpUserContext(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    public String getProjectId() {
        return getParameter();
    }

    public void setProjectId(String projectId) {
        throw new NotImplementedException();
    }

    private String getParameter() {
        return httpServletRequest.getParameter("project_id");
    }
}