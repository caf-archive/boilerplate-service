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
package com.hpe.caf.util.boilerplate.creation;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

/**
 * Writes the result of creating expressions and tags to a file.
 */
public class CreationResultWriter {
    public static void outputResult(CreationResult result){
        System.out.println("================================================================================");
        System.out.println("                             Writing output file");
        System.out.println("================================================================================");
        ObjectMapper mapper = new ObjectMapper();
        try {
            String outputFilePath = System.getProperty(Constants.Properties.OUTPUT_FILE);
            File outputFile = new File(outputFilePath);
            File parentFile = outputFile.getParentFile();
            if(parentFile!=null){
                parentFile.mkdirs();
            }
            mapper.writeValue((outputFile), result);
            System.out.println("IDs and Names for created Boilerplate Expressions and Tags output to '" + outputFilePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write output file: " + e, e);
        }
    }
}
