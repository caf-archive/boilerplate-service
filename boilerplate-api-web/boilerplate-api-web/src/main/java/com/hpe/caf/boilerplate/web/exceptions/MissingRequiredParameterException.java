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
package com.hpe.caf.boilerplate.web.exceptions;

import java.util.UUID;

/**
 * Created by Michael.McAlynn on 10/12/2015.
 */
public class MissingRequiredParameterException extends LocalizedException {
    public MissingRequiredParameterException(MissingRequiredParameterErrors error, Throwable cause) {
        super(ErrorCodes.MISSING_REQUIRED_PARAMETER.getValue(), error, cause);
    }

    public MissingRequiredParameterException(MissingRequiredParameterErrors error) {
        super(ErrorCodes.MISSING_REQUIRED_PARAMETER.getValue(), error);
    }

    public MissingRequiredParameterException(LocalizedExceptionError error) {
        super(ErrorCodes.MISSING_REQUIRED_PARAMETER.getValue(), error);
    }

    public MissingRequiredParameterException(LocalizedExceptionError error, UUID correlationCode) {
        super(ErrorCodes.MISSING_REQUIRED_PARAMETER.getValue(), error, correlationCode);
    }

    @Override
    protected String getPropertyFileName() {
        return "missingRequiredParameterMessage";
    }
}
