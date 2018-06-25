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
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Entrypoint for application to create boilerplate expressions and tags based on file input.
 */
public class Main {
    public static void main(final String[] args) throws Exception {

        System.out.println("================================================================================");
        System.out.println("=                                                                              =");
        System.out.println("=                         Boilerplate Creation Utility                         =");
        System.out.println("=                                                                              =");
        System.out.println("================================================================================");
        System.out.println();

        SettingsProvider.defaultProvider.getConfiguration().addConfiguration(new PropertiesConfiguration("application.properties"));

        FileOutputStream errStream = new FileOutputStream(Constants.ERROR_LOG);
        MultiOutputStream multiErr= new MultiOutputStream(System.err, errStream);
        PrintStream stderr= new PrintStream(multiErr);
        System.setErr(stderr);

        try {
            CompositeConfiguration configuration = SettingsProvider.defaultProvider.getConfiguration();
            Collection<String> systemProperties = new ArrayList<>();
            systemProperties.add(Constants.Properties.BOILERPLATE_URL);
            systemProperties.add(Constants.Properties.INPUT_FILE);
            systemProperties.add(Constants.Properties.OUTPUT_FILE);
            for(String propertyName:systemProperties) {
                setSystemPropertyFromConfig(configuration, propertyName);
            }

            BoilerplateCreator creator = new BoilerplateCreator();
            creator.run();
        }
        catch (Exception e) {
            System.err.println();
            System.err.println("Creation failed.");
            e.printStackTrace();
            System.exit(0);
            return;
        }

        System.out.println("Creation completed successfully");
        System.exit(0);
    }

    private static void setSystemPropertyFromConfig(CompositeConfiguration config, String paramName){
        String appname = config.getString(paramName);
        if (appname != null && !appname.isEmpty()) {
            System.setProperty(paramName, appname);
        }
        else {
            throw new RuntimeException("Required property not set: "+paramName);
        }
    }
}
