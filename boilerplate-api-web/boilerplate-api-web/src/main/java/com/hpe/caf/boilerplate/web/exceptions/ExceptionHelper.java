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
package com.hpe.caf.boilerplate.web.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hpe.caf.boilerplate.api.exceptions.ItemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Michael.McAlynn on 15/12/2015.
 */
public class ExceptionHelper {
    public static final Logger logger = LoggerFactory.getLogger(ExceptionHelper.class);

    public static ErrorResponse outputItemNotFoundException(HttpServletResponse response, ItemType errorType, RuntimeException originalException) throws RuntimeException{
        logger.error("ItemNotFoundException occurred.", originalException);
        LocalizedItemNotFoundErrors error = null;
        switch(errorType){
            case BOILERPLATE_EXPRESSION:
                error = LocalizedItemNotFoundErrors.EXPRESSION_NOT_FOUND;
                break;
            case TAG:
                error = LocalizedItemNotFoundErrors.TAG_NOT_FOUND;
                break;
            default:
                error = LocalizedItemNotFoundErrors.UNKNOWN_NOT_FOUND;
                break;
        }
        LocalizedItemNotFoundException itemNotFoundException = new LocalizedItemNotFoundException(error);
        return outputErrorResponse(response, itemNotFoundException, error.getStatusCode());
    }

    public static ErrorResponse outputInvalidRequestException(HttpServletResponse response, ItemType errorType, RuntimeException originalException) throws RuntimeException{
        logger.error("InvalidRequestException occurred.", originalException);
        LocalizedInvalidRequestErrors error = null;
        switch(errorType){
            case BOILERPLATE_EXPRESSION:
                error = LocalizedInvalidRequestErrors.EXPRESSION_INVALID;
                break;
            case TAG:
                error = LocalizedInvalidRequestErrors.TAG_INVALID;
                break;
            default:
                error = LocalizedInvalidRequestErrors.UNKNOWN_INVALID;
                break;
        }
        LocalizedInvalidRequestException invalidRequestException = new LocalizedInvalidRequestException(error);
        return outputErrorResponse(response, invalidRequestException, error.getStatusCode());
    }

    private static ErrorResponse outputErrorResponse(HttpServletResponse response, LocalizedException localizedException, HttpStatus status){
        ErrorResponse errorResponse = new ErrorResponse(localizedException.getErrorCode(), localizedException.getLocalizedMessage(),
                localizedException.getError().getLocalisedMessage(), localizedException.getCorrelationCode());
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String errorMessage = null;
        try {
            errorMessage = objectMapper.writeValueAsString(errorResponse);
        } catch (JsonProcessingException e) {
            logger.debug("Unable to write error message for item not found exception.", e);
        }

        response.setContentType("application/json");
        response.setStatus(status.value());
        try {
            response.getWriter().print(errorMessage);
        } catch (IOException e) {
            logger.debug("Unable to write error message for item not found exception.", e);
        }
        return errorResponse;
    }
}
