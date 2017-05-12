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
package com.hpe.caf.worker.boilerplateshared;

import java.util.Collection;

/**
 * Created by Jason.Quinn on 04/01/2016.
 */
public class SelectedExpressions extends SelectedItems {
    private Collection<Long> expressionIds;

    public SelectedExpressions() {
    }

    public Collection<Long> getExpressionIds() {
        return expressionIds;
    }

    public void setExpressionIds(Collection<Long> expressionIds) {
        this.expressionIds = expressionIds;
    }
}
