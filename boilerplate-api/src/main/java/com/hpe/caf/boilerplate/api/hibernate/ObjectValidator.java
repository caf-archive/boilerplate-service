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
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by gibsodom on 09/12/2015.
 */

@Primary
@Component
@Scope("prototype")
public class ObjectValidator<Object>{
    private ValidatorFactory validatorFactory;

    @Autowired
    public ObjectValidator(ValidatorFactory validatorFactory){
        this.validatorFactory = validatorFactory;
    }

    public ValidationResult validate(Object objectToValidate) {
        Set<ConstraintViolation<Object>> validate = validatorFactory.getValidator().validate(objectToValidate);

        if(validate!= null && validate.size() > 0) {
            return new ValidationResult(validate.stream().map(ConstraintViolation::getMessage).distinct().collect(Collectors.joining("\n")));
        }
        return new ValidationResult();
    }

    class ValidationResult {
        private String reason;
        private boolean valid;

        /**
         * Constructor that should be used if validation fails.
         * @param reason    The reason that the condition is not valid.
         */
        public ValidationResult(String reason) {
            this.reason = reason;
            this.valid = false;
        }

        /**
         * Default constructor, use for a successful validation.
         */
        public ValidationResult() {
            this.reason = null;
            this.valid = true;
        }

        public String getReason() {
            return reason;
        }

        public boolean isValid() {
            return valid;
        }
    }
}
