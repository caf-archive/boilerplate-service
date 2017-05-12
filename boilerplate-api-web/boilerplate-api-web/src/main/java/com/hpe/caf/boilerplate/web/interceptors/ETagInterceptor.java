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
package com.hpe.caf.boilerplate.web.interceptors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hpe.caf.boilerplate.api.Tag;
import com.hpe.caf.boilerplate.web.Constants;
import com.hpe.caf.boilerplate.web.setup.etags.ETagRetriever;
import com.hpe.caf.boilerplate.web.setup.etags.ETagSupport;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;

/**
 * Interceptor with the purpose of checking that Put requests which specify "if-match", pass up an Etag that matches the ETagCreator marked for the request
 */
public class ETagInterceptor extends HandlerInterceptorAdapter implements ApplicationContextAware{
    private static final String HEADER_IF_MATCH = "If-Match";

    private ApplicationContext context = null;

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(!request.getMethod().equals("PUT")){
            //not a PUT request, no work to do
            return true;
        }
        if(handler == null || !(handler instanceof HandlerMethod)){
            //need handler method to get annotation describing how to create ETag
            return true;
        }
        HandlerMethod handlerMethod = ((HandlerMethod)handler);
        ETagSupport eTagAnnotation = handlerMethod.getMethodAnnotation(ETagSupport.class);
        if(eTagAnnotation==null){
            //no ETagSupport annotation on method, no work to do
            return true;
        }
        //check that 'if-match' is in list of headers
        Enumeration<String> headerNames = request.getHeaderNames();
        boolean hasIfMatchHeader = false;
        while(headerNames.hasMoreElements()){
            String headerName = headerNames.nextElement();
            if(headerName.toLowerCase().equals(HEADER_IF_MATCH.toLowerCase())){
                hasIfMatchHeader = true;
                break;
            }
        }
        if(!hasIfMatchHeader){
            //no if-match header specified, no work to do
            return true;
        }
        String passedETagValue = request.getHeader(HEADER_IF_MATCH);

        //generate an etag representing what we expect for the PUT to be allowed to succeed
        String expectedETag = generateETagToCompare(eTagAnnotation, request);
        if(passedETagValue==null){
            if(expectedETag ==null) {
                //both eTags are null proceed with execution
                return true;
            }
            response.setStatus(HttpStatus.PRECONDITION_FAILED.value());
            return false;
        }
        if(passedETagValue.equals(expectedETag)){
            //eTag matches, continue with request
            return true;
        }
        //eTag did not match expected value, reject request
        response.setStatus(HttpStatus.PRECONDITION_FAILED.value());
        return false;
    }

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if(handler == null || !(handler instanceof HandlerMethod)){
            //need handler method to get annotation describing how to create ETag
            return;
        }
        HandlerMethod handlerMethod = ((HandlerMethod)handler);
        ETagSupport eTagAnnotation = handlerMethod.getMethodAnnotation(ETagSupport.class);
        if(eTagAnnotation==null){
            //no ETagSupport annotation on method, no work to do
            return;
        }
        setETagCreationType(eTagAnnotation, request);
    }

    private ETagRetriever getETagRetrieverFromAnnotation(ETagSupport eTagAnnotation){
        Class<? extends ETagRetriever> beanType = eTagAnnotation.eTagRetriever();
        return context.getBean(beanType);
    }

    /*
        We can't access the returned value here so provide the type to derive eTag from so that a Filter can do so later
     */
    private void setETagCreationType(ETagSupport eTagAnnotation, HttpServletRequest request){
        ETagRetriever eTagRetriever = getETagRetrieverFromAnnotation(eTagAnnotation);
        Class classToGenerateETagFrom = eTagRetriever.getETagGeneratingType();
        //relying on this header being removed by Filter
        //response.setHeader(Constants.Headers.ETAG_CREATION_TYPE, classToGenerateETagFrom.toString());
        request.setAttribute(Constants.Attributes.ETAG_CREATION_TYPE, classToGenerateETagFrom.getName().toString());
    }

    private String generateETagToCompare(ETagSupport eTagAnnotation, HttpServletRequest request){
        Class<? extends ETagRetriever> beanType = eTagAnnotation.eTagRetriever();
        ETagRetriever eTagRetriever = context.getBean(beanType);
        return eTagRetriever.getExpectedETag(request);
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }
}
