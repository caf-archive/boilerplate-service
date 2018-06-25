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
package com.hpe.caf.boilerplate.web.interceptors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hpe.caf.boilerplate.web.exceptions.ErrorResponse;
import com.hpe.caf.boilerplate.web.exceptions.MissingRequiredParameterErrors;
import com.hpe.caf.boilerplate.web.exceptions.MissingRequiredParameterException;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Michael.McAlynn on 09/12/2015.
 */
public class ProjectIdArgumentValidatorInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (request.getRequestURI().equals("/favicon.ico"))
            return true;

        String project_id = request.getParameter("project_id");
        if (project_id == null) {
            MissingRequiredParameterException missingRequiredParameterCpeException = new MissingRequiredParameterException(MissingRequiredParameterErrors.PROJECT_ID_REQUIRED);
            ErrorResponse errorResponse = new ErrorResponse(missingRequiredParameterCpeException.getErrorCode(), missingRequiredParameterCpeException.getLocalizedMessage(), missingRequiredParameterCpeException.getError().getLocalisedMessage(), missingRequiredParameterCpeException.getCorrelationCode());
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            String errorMessage = objectMapper.writeValueAsString(errorResponse);

            response.setContentType("application/json");
            response.setStatus(MissingRequiredParameterErrors.PROJECT_ID_REQUIRED.getStatusCode().value());
            response.getOutputStream().print(errorMessage);
            return false;
        }
        return true;
    }
}