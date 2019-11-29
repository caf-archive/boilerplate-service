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
package com.hpe.caf.util.boilerplate.creation;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An OutputStream that allows writing to multiple configured output streams in a single call.
 */
public class MultiOutputStream extends OutputStream
{
    OutputStream[] outputStreams;

    public MultiOutputStream(OutputStream... outputStreams)
    {
        this.outputStreams= outputStreams;
    }

    @Override
    public void write(int b) throws IOException
    {
        for (OutputStream out: outputStreams)
            out.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException
    {
        for (OutputStream out: outputStreams)
            out.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        for (OutputStream out: outputStreams)
            out.write(b, off, len);
    }

    @Override
    public void flush() throws IOException
    {
        for (OutputStream out: outputStreams)
            out.flush();
    }

    @Override
    public void close() throws IOException
    {
        for (OutputStream out: outputStreams)
            out.close();
    }
}