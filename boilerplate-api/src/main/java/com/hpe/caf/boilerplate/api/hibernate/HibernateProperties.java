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
package com.hpe.caf.boilerplate.api.hibernate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

/**
 * Created by gibsodom on 09/12/2015.
 */
@Configuration
@PropertySource("classpath:hibernate.properties")
@PropertySource(value = "file:${BOILERPLATE_CONFIG}/hibernate.properties", ignoreResourceNotFound = true)
public class HibernateProperties {

    @Autowired
    private Environment environment;

    public String getConnectionString(){
        return environment.getProperty("hibernate.connectionstring");
    }

    public String getUser(){
        return environment.getProperty("hibernate.user");
    }

    public String getPassword(){
        return environment.getProperty("hibernate.password");
    }
}
