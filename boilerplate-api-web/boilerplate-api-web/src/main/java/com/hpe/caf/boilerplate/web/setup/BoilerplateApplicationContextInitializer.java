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
package com.hpe.caf.boilerplate.web.setup;

import com.hpe.caf.boilerplate.api.EnvironmentConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Created by Michael.McAlynn on 11/12/2015.
 */
public class BoilerplateApplicationContextInitializer implements ApplicationContextInitializer {
    private final static Logger logger = LoggerFactory.getLogger(BoilerplateApplicationContextInitializer.class);

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        logger.trace("BoilerplateApplicationContextInitializer initialize");

        ConfigurableEnvironment configurableEnvironment = applicationContext.getEnvironment();
        configurableEnvironment.addActiveProfile("webserver");
        configurableEnvironment.addActiveProfile("apidirect");
        EnvironmentConfigurer.configure(configurableEnvironment);
    }
}
