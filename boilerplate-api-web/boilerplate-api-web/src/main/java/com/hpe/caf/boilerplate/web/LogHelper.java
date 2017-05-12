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
package com.hpe.caf.boilerplate.web;

import com.google.common.base.CharMatcher;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by Michael.McAlynn on 09/12/2015.
 */
public class LogHelper {
    /**
     * Util method to log the whole http request as a debug log message. Only logs get params
     * (i.e. the url + query string), because logging post params would be too much data in terms of size.
     * @param logger       The Logger to use to log the request.
     * @param request   The request to log.
     */
    public static void logRequest(Logger logger, HttpServletRequest request){
        logger.info("REQUEST " + ZonedDateTime.now().format(DateTimeFormatter.ofPattern("YYYY/MM/dd HH:mm:ss")) + " " + buildUrlLogStringFromRequest(request));
    }

    private static String buildUrlLogStringFromRequest(HttpServletRequest request) {

        //Note it is not possible to log the contents of the POST request ourselves here, the methods on
        // HttpServletRequest to obtain this can be used ONLY ONCE IN THE ENTIRE LIFETIME of the request, including
        //when params are parsed to the method arguments
        return removeWhiteSpace( //Remove new line characters
                request.getScheme() + "://" + //http scheme
                        request.getServerName() + //server name
                        ("http".equals(request.getScheme()) && request.getServerPort() == 80 || "https".equals(request.getScheme()) && request.getServerPort() == 443 ? "" : ":" + request.getServerPort()) + //port
                        request.getRequestURI() + //request path
                        (request.getQueryString() != null ? "?" + request.getQueryString() : "")//query string
        );
    }

    private static String removeWhiteSpace(String string) {
        if(string==null){
            return null;
        }
        return CharMatcher.BREAKING_WHITESPACE.removeFrom(string);
    }
}
