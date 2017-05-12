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
package com.hpe.caf.worker.boilerplate.expressionprocessing;

import com.hpe.caf.worker.boilerplateshared.response.BoilerplateMatch;

import java.io.InputStream;
import java.util.Set;

/**
 * Created by Jason.Quinn on 08/01/2016.
 */
public class ProcessorResult {
    private InputStream inputStream;
    private Set<BoilerplateMatch> matches;

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public Set<BoilerplateMatch> getMatches() {
        return matches;
    }

    public void setMatches(Set<BoilerplateMatch> matches) {
        this.matches = matches;
    }
}
