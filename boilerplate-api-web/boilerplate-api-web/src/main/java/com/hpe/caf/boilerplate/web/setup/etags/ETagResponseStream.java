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
package com.hpe.caf.boilerplate.web.setup.etags;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Michael.McAlynn on 14/12/2015.
 */
public class ETagResponseStream extends ServletOutputStream {
    private boolean closed = false;
    private OutputStream stream = null;

    public ETagResponseStream(HttpServletResponse response, OutputStream stream) throws IOException {
        super();
        this.stream = stream;
    }

    public void close() throws IOException {
        if (!closed) {
            stream.close();
            closed = true;
        }
    }

    public void flush() throws IOException {
        if (!closed) {
            stream.flush();
        }
    }

    public void write(int b) throws IOException {
        if (!closed) {
            stream.write((byte) b);
        }
    }

    public void write(byte b[], int off, int len) throws IOException {
        if (!closed) {
            stream.write(b, off, len);
        }
    }

    public void write(byte b[]) throws IOException {
        write(b, 0, b.length);
    }

    public boolean closed() {
        return closed;
    }

    public void reset() {
    }
}