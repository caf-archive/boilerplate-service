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
package com.hpe.caf.util.boilerplate.creation;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.commons.configuration.SystemConfiguration;

import java.util.Objects;

/**
 * Allows access to environment settings.
 */
public class SystemSettingsProvider extends SettingsProvider {

    private final CompositeConfiguration configuration = createConfiguration();

    private static CompositeConfiguration createConfiguration() {
        CompositeConfiguration configuration = new CompositeConfiguration();
        configuration.addConfiguration(new SystemConfiguration());
        configuration.addConfiguration(new EnvironmentConfiguration());
        return configuration;
    }

    public CompositeConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public String getSetting(String name) {
        Objects.requireNonNull(name);
        return configuration.getString(name);
    }

    @Override
    public boolean getBooleanSetting(String name) {
        return configuration.getBoolean(name);
    }

    @Override
    public boolean getBooleanSetting(String name, boolean defaultValue) {
        return configuration.getBoolean(name, defaultValue);
    }
}