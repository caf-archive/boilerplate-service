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
package com.hpe.caf.worker.boilerplate;

import com.hpe.caf.api.worker.DataStore;
import com.hpe.caf.api.worker.DataStoreException;
import org.apache.commons.lang3.NotImplementedException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jason.Quinn on 19/01/2016.
 */
public class TestDataStore implements DataStore {
    Map<String, InputStream> dataMap = new HashMap<>();

    @Override
    public void delete(String reference) throws DataStoreException {
        dataMap.remove(reference);
    }

    @Override
    public InputStream retrieve(String s) throws DataStoreException {
        return dataMap.get(s);
    }

    @Override
    public long size(String s) throws DataStoreException {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public String store(InputStream inputStream, String s) throws DataStoreException {
        dataMap.put(s, inputStream);
        return s;
    }

    @Override
    public String store(byte[] bytes, String s) throws DataStoreException {
        dataMap.put(s, new ByteArrayInputStream(bytes));
        return s;
    }

    @Override
    public String store(Path path, String s) throws DataStoreException {
        throw new NotImplementedException("Not implemented");
    }
}
