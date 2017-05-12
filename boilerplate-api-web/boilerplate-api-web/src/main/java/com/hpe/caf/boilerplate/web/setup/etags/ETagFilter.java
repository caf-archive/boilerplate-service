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
package com.hpe.caf.boilerplate.web.setup.etags;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hpe.caf.boilerplate.api.Tag;
import com.hpe.caf.boilerplate.web.Constants;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Michael.McAlynn on 14/12/2015.
 */
public class ETagFilter implements Filter {
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        ByteArrayOutputStream eTagByteArrayOutputStream = new ByteArrayOutputStream();
        ETagResponseWrapper eTagResponseWrapper = new ETagResponseWrapper(response, eTagByteArrayOutputStream);
        chain.doFilter(req, eTagResponseWrapper);

        byte[] eTagResponseAsBytes = eTagByteArrayOutputStream.toByteArray();
        ObjectMapper mapper = new ObjectMapper();

        Class eTagCreationType = getETagCreationType(request, response, eTagResponseAsBytes);

        if(eTagResponseAsBytes.length == 0 || eTagCreationType==null){
            //no data to generate eTag from or unable to get eTag creation type, output stream just
            outputServletStream(response, eTagResponseAsBytes);
            return;
        }

        Object objectToCreate = mapper.readValue(eTagResponseAsBytes, eTagCreationType);
        addETagHeader(response, objectToCreate.hashCode());
        outputServletStream(response, eTagResponseAsBytes);
    }

    private Class getETagCreationType(HttpServletRequest request, HttpServletResponse response, byte[] eTagResponseAsBytes) throws IOException {
        Object eTagCreationTypeAsObject = request.getAttribute(Constants.Attributes.ETAG_CREATION_TYPE);
        if(eTagCreationTypeAsObject==null){

            return null;
        }
        //remove the EtagCreation attribute
        request.removeAttribute(Constants.Attributes.ETAG_CREATION_TYPE);
        String eTagCreationTypeAsString = eTagCreationTypeAsObject.toString();

        //find Class represented by String
        Class eTagCreationType = null;
        try {
            eTagCreationType = Class.forName(eTagCreationTypeAsString);
        } catch (ClassNotFoundException e) {
            //couldn't find the class, output the stream and exit doFilter
            e.printStackTrace();
            outputServletStream(response, eTagResponseAsBytes);
            return null;
        }
        return eTagCreationType;
    }

    private void addETagHeader(HttpServletResponse response, int eTagHasCode){
        response.setHeader(Constants.Headers.ETAG, Integer.toString(eTagHasCode));
    }

    private void outputServletStream(HttpServletResponse response, byte[] eTagResponseAsBytes) throws IOException {
        response.setContentLength(eTagResponseAsBytes.length);
        ServletOutputStream sos = response.getOutputStream();
        sos.write(eTagResponseAsBytes);
        sos.flush();
        sos.close();
    }

    public void init(FilterConfig filterConfig) {
    }

    public void destroy() {
    }
}
