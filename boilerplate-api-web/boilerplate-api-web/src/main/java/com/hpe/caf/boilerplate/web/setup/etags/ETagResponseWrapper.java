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
package com.hpe.caf.boilerplate.web.setup.etags;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Created by Michael.McAlynn on 14/12/2015.
 */
public class ETagResponseWrapper extends HttpServletResponseWrapper {
    private HttpServletResponse response = null;
    private ServletOutputStream stream = null;
    private PrintWriter writer = null;
    private OutputStream buffer = null;

    public ETagResponseWrapper(HttpServletResponse response, OutputStream buffer) {
        super(response);
        this.response = response;
        this.buffer = buffer;
    }

    public PrintWriter getWriter() throws IOException {
        if (writer == null)
            writer = new PrintWriter(new OutputStreamWriter(getOutputStream(), "UTF-8"));

        return writer;
    }

    public ServletOutputStream getOutputStream() throws IOException {
        if (stream == null)
            stream = new ETagResponseStream(response, buffer);

        return stream;
    }

    public void flushBuffer() throws IOException {
        stream.flush();
    }
}
