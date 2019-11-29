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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * Created by Michael.McAlynn on 09/12/2015.
 */
public class ErrorResponse{
    @JsonProperty("error")
    public Number error;
    @JsonProperty("message")
    private String errorMessage;
    @JsonProperty("correlation_code")
    private UUID correlationCode;
    @JsonProperty("reason")
    public String reason;

    ErrorResponse(){}

    public ErrorResponse(Number errorCode, String errorReason){
        this.error = errorCode;
        this.reason = sanitizeReason(errorReason);
        this.correlationCode = null;
        this.errorMessage = null;
    }

    public ErrorResponse(Number errorCode, String errorReason, String errorMessage, UUID correlationCode){
        this.error = errorCode;
        this.errorMessage = errorMessage;
        this.correlationCode = correlationCode;
        this.reason = sanitizeReason(errorReason);
    }

    static String sanitizeReason(String errorReason){
        if(errorReason == null || errorReason.isEmpty())
            return errorReason;

        int firstColonPosition = errorReason.indexOf(':');

        if(firstColonPosition == -1)
            return errorReason.trim();

        //so we have xxxx:yyyyy(:zzzz?), we need to remote xxxx: if it is exception name.
        String stringStart = errorReason.substring(0, firstColonPosition).trim();

        //we only care if we have e.g. some.namespace.Exception:, not there was an Exception: xxx. Latter should be ok.
        if(!stringStart.contains(" ") && stringStart.toUpperCase().contains("EXCEPTION")){
            return sanitizeReason(errorReason.substring(firstColonPosition + 1)).trim();
        }

        return errorReason;
    }
}