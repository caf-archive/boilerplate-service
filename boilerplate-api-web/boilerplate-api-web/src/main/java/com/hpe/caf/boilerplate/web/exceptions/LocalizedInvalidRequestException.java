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

import java.util.UUID;

/**
 * Created by Michael.McAlynn on 15/12/2015.
 */
public class LocalizedInvalidRequestException extends LocalizedException {
    public LocalizedInvalidRequestException(LocalizedInvalidRequestErrors error, Throwable cause) {
        super(ErrorCodes.INVALID_REQUEST.getValue(), error, cause);
    }

    public LocalizedInvalidRequestException(LocalizedInvalidRequestErrors error) {
        super(ErrorCodes.INVALID_REQUEST.getValue(), error);
    }

    public LocalizedInvalidRequestException(LocalizedExceptionError error) {
        super(ErrorCodes.INVALID_REQUEST.getValue(), error);
    }

    public LocalizedInvalidRequestException(LocalizedExceptionError error, UUID correlationCode) {
        super(ErrorCodes.INVALID_REQUEST.getValue(), error, correlationCode);
    }

    @Override
    protected String getPropertyFileName() {
        return "invalidRequestMessage";
    }
}
