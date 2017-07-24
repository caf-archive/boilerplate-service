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
 * Retrieves expressions and tags to create information from file.
 */
public class CreationInputReader {
    public static CreationInput getInput(){
        System.out.println("================================================================================");
        System.out.println("                             Reading input file");
        System.out.println("================================================================================");
        ObjectMapper mapper = new ObjectMapper();
        String inputFileLocation = System.getProperty(Constants.Properties.INPUT_FILE);
        File inputFile = new File(inputFileLocation);
        if(!inputFile.exists()){
            throw new RuntimeException("Input file not accessible: "+inputFileLocation);
        }
        try {
            System.out.println("Reading input file from "+inputFileLocation);
            return mapper.readValue(inputFile, CreationInput.class);
        }
        catch (IOException ex) {
            System.out.println("Failed to read input file from "+inputFileLocation);
            throw new RuntimeException(ex.getMessage());
        }
    }
}
