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
package com.hpe.caf.boilerplate.api.hibernate;

import com.hpe.caf.boilerplate.api.DtoBase;
import com.hpe.caf.boilerplate.api.UserContext;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * Created by gibsodom on 09/12/2015.
 */
@Component
public class PreInsertInterceptor extends EmptyInterceptor {

    private UserContext userContext;
//    private ObjectValidator objectValidator;

    @Autowired
    public PreInsertInterceptor(UserContext userContext) {
//        this.objectValidator = objectValidator;
        this.userContext = userContext;
    }

    public boolean onSave(
            Object entity,
            Serializable id,
            Object[] state,
            String[] propertyNames,
            Type[] types) {
//        validate(entity);

        return addProjectId(propertyNames,state);
    }

    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
//        validate(entity);
        return addProjectId(propertyNames,currentState);
    }

//    private void validate(Object object) {
//        ObjectValidator.ValidationResult result = objectValidator.validate(object);
//        if (!result.isValid()) {
//            throw new RuntimeException(result.getReason());
//        }
//    }

    private boolean addProjectId(String[] propertyNames, Object[] state) {
        boolean stateModified = false;

        for (int i = 0; i < propertyNames.length; i++) {
            Object o = state[i];
            if("projectId".equalsIgnoreCase(propertyNames[i])||"project_id".equalsIgnoreCase(propertyNames[i])){
                state[i] = userContext.getProjectId();
                stateModified = true;
                break;
            }
            else if (o instanceof DtoBase) {
                try {
                    Field projectIdField = DtoBase.class.getDeclaredField("projectId");
                    if (projectIdField != null) {
                        projectIdField.setAccessible(true);
                        try {
                            projectIdField.set(o, userContext.getProjectId());
                        } catch (IllegalAccessException ignored) {
                        }
                    }
                } catch (NoSuchFieldException ignored) {
                }
            }
        }
        return stateModified;
    }
}
