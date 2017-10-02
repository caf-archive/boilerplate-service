/*
 * Copyright 2015-2017 EntIT Software LLC, a Micro Focus company.
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

import com.hpe.caf.boilerplate.webcaller.model.BoilerplateExpression;

/**
 * Representation of a boilerplate expression to be created using creation utility. Provides ability to record temporary ID
 * for usage before ID is assigned to expression after creation.
 */
public class CreationExpression extends BoilerplateExpression{
    /*
        An id we can use to link tags to already created expressions
     */
    private Long tempId;

    public Long getTempId(){return tempId;}
    public void setTempId(Long tempId){this.tempId = tempId;}

    /*
        Convenience method to create an actual BoilerplateExpression object from a CreationExpression object (
        not just upcasting and hiding properties (which json parser will still see).
     */
    public BoilerplateExpression getAsBoilerplateExpression(){
        BoilerplateExpression exp = new BoilerplateExpression();
        exp.setId(this.getId());
        exp.setDescription(this.getDescription());
        exp.setExpression(this.getExpression());
        exp.setName(this.getName());
        exp.setProjectId(this.getProjectId());
        exp.setReplacementText(this.getReplacementText());
        return exp;
    }
}
