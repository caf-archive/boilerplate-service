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
package com.hpe.caf.boilerplate.api.exceptions;

/**
 * Created by gibsodom on 15/12/2015.
 */
public class TransitoryBackEndFailureException extends RuntimeException {

    public TransitoryBackEndFailureException(){
        super();
    }

    public TransitoryBackEndFailureException(String message, Throwable cause){
        super(message,cause);
    }
}
