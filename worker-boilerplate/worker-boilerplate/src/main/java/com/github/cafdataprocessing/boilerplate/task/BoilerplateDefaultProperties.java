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
package com.github.cafdataprocessing.boilerplate.task;

import com.hpe.caf.worker.document.model.Document;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Provides access to environment properties that configure behaviour of the Boilerplate Handler.
 */
public class BoilerplateDefaultProperties
{

    public static Collection<String> getDefaultFields(final Document document)
    {
        String defaultFieldsAsString = document.getCustomData("boilerplateworkerhandler.defaultfields");
        if (defaultFieldsAsString == null) {
            return getDefaultFieldsInternal();
        }
        defaultFieldsAsString = defaultFieldsAsString.trim();
        if (defaultFieldsAsString.length() == 0) {
            return getDefaultFieldsInternal();
        }
        String[] defaultFields = defaultFieldsAsString.split(",");
        return Arrays.asList(defaultFields);
    }

    private static Collection<String> getDefaultFieldsInternal()
    {
        Collection<String> defaults = new ArrayList<>(1);
        defaults.add("CONTENT");
        return defaults;
    }
}
