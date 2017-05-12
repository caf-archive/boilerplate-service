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
package com.hpe.caf.boilerplate.web.exceptions;

import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.UUID;

/**
 * Created by Michael.McAlynn on 10/12/2015.
 */
public abstract class LocalizedException extends RuntimeException{
    private int errorCode;
    private ExceptionErrors error;
    private UUID correlationCode;

    protected abstract String getPropertyFileName();

    public LocalizedException(int ErrorCode, ExceptionErrors error) {
        super();
        errorCode = ErrorCode;
        this.error = error;
        correlationCode = generateCorrelationCode();
    }

    public LocalizedException(int ErrorCode, ExceptionErrors error, Throwable cause) {
        super(cause);
        errorCode = ErrorCode;
        this.error = error;
        correlationCode = generateCorrelationCode(cause);
    }

    public LocalizedException(int ErrorCode, ExceptionErrors error, UUID correlationCode) {
        super();
        errorCode = ErrorCode;
        this.error = error;
        this.correlationCode = correlationCode == null ? generateCorrelationCode() : correlationCode;
    }

    private UUID generateCorrelationCode(){
        return generateCorrelationCode(null);
    }

    /**
     * Returns the correlation code from the top LocalizedException or else generates a new one if needed
     * @param cause the cause
     * @return UUID for the exception
     */
    private UUID generateCorrelationCode(Throwable cause) {
        if(cause == null){
            return UUID.randomUUID();
        }
        if(cause instanceof LocalizedException) {
            return ((LocalizedException) cause).getCorrelationCode();
        }
        return generateCorrelationCode(cause.getCause());
    }

    public ExceptionErrors getError() {
        return error;
    }

    public UUID getCorrelationCode(){
        return correlationCode;
    }

    protected String getLocalisedString(ExceptionErrors key) {
        return getString(key, Locale.ENGLISH);
    }

    protected String getString(ExceptionErrors key) {
        return getString(key, Locale.ENGLISH);
    }

    private String getString(ExceptionErrors key, Locale locale) {
        String messageFormat = getResource("messageFormat", locale);
        String message = getResource("message", locale);
        String errorMessage = "";
        if(key != null) {
            errorMessage = key.getMessage(locale);
        }
        MessageFormat format = new MessageFormat(messageFormat);
        Object[] args = {message, errorMessage};
        String baseMessage = format.format(args);

        String exceptionMessageFormat = getResource("message", locale, "localizedExceptionMessage");
        if(correlationCode != null && StringUtils.isNotBlank(exceptionMessageFormat)) {
            MessageFormat exceptionFormat = new MessageFormat(exceptionMessageFormat);
            Object[] exceptionArgs = {baseMessage, correlationCode.toString()};
            return exceptionFormat.format(exceptionArgs);
        }
        return baseMessage;
    }

    protected String getResource(String key, Locale locale) {
        return getResource(key, locale, getPropertyFileName());
    }

    protected String getResource(String key, Locale locale, String bundleName) {
        ResourceBundle bundle = ResourceBundle.getBundle("localisation.exceptions." + bundleName, locale);

        if(key != null && bundle.containsKey(key)) {
            return bundle.getString(key);
        }

        return "";
    }

    @Override
    public String getMessage() {
        return getString(error);
    }

    @Override
    public String getLocalizedMessage() {
        return getLocalisedString(error);
    }

    public int getErrorCode() {
        return errorCode;
    }
}
