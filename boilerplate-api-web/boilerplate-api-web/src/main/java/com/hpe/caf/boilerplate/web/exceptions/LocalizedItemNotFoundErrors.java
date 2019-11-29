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

/**
 * Created by Michael.McAlynn on 15/12/2015.
 */
    public enum LocalizedItemNotFoundErrors implements ExceptionErrorsEnum {
    TAG_NOT_FOUND(1),
    EXPRESSION_NOT_FOUND(2),
    UNKNOWN_NOT_FOUND(3);

    private final Integer errorCode;

    private LocalizedItemNotFoundErrors(Integer errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String getKey() {
        return errorCode.toString();
    }

    @Override
    public String getResourceName() {
        return "itemNotFoundErrors";
    }

    public HttpStatus getStatusCode() {
        return HttpStatus.NOT_FOUND;
    }
}
