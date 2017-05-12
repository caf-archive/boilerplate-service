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
package com.hpe.caf.worker.boilerplateshared.response;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 */
public class BoilerplateMatch {
    private Long boilerplateId;
    private String value;

    public BoilerplateMatch() {
    }

    public BoilerplateMatch(Long boilerplateId, String value) {
        this.boilerplateId = boilerplateId;
        this.value = value;
    }

    public Long getBoilerplateId() {
        return boilerplateId;
    }

    public void setBoilerplateId(Long boilerplateId) {
        this.boilerplateId = boilerplateId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(boilerplateId).append(value).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        BoilerplateMatch rhs = (BoilerplateMatch) obj;
        return new EqualsBuilder()
                .append(boilerplateId, rhs.getBoilerplateId())
                .append(value, rhs.getValue())
                .isEquals();
    }
}
