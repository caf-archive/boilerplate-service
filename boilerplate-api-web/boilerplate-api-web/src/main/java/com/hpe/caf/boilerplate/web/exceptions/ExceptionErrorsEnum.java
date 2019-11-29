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
package com.hpe.caf.boilerplate.web.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by Michael.McAlynn on 10/12/2015.
 */
public interface ExceptionErrorsEnum extends ExceptionErrors {
    String getKey();

    default String getMessage(Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle("localisation.exceptions.exceptionErrors." + getResourceName(), locale);
        if (getKey() != null && bundle.containsKey(getKey())) {
            return bundle.getString(getKey());
        }
        return "";
    }

    String getResourceName();
    HttpStatus getStatusCode();
}
