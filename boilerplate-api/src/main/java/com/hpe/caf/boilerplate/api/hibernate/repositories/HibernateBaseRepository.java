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
package com.hpe.caf.boilerplate.api.hibernate.repositories;

import com.hpe.caf.boilerplate.api.UserContext;
import com.hpe.caf.boilerplate.api.hibernate.ExecutionContext;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by gibsodom on 09/12/2015.
 */
public abstract class HibernateBaseRepository<T> {

    protected UserContext userContext;

    public abstract T create(ExecutionContext context, T t);
    public abstract T update(ExecutionContext context, T t);
    public abstract T delete(ExecutionContext context, Long id);
    public abstract Collection<T>  retrieve(ExecutionContext context, Collection<Long> ids);
    public abstract T preSave(T t, Session session);
    public abstract Collection<T> retrieveAll(ExecutionContext context);
    public abstract Collection<T> retrievePaged(ExecutionContext context, int maxSize, int startPage);

    public HibernateBaseRepository(UserContext userContext){
        this.userContext = userContext;
    }

    protected void evict(Session session,T item){
        if(item !=null) {
            session.evict(item);
        }
    }

    protected void evict(Session session, Collection<T> items){
        for(T item:items){
            session.evict(item);
        }
    }

    protected T retrieveSingleItem(ExecutionContext context, Long id){
        Collection<T> items = retrieve(context, Collections.singletonList(id));
        if (items.size() > 1) {
            throw new RuntimeException("To many items returned");
        }
        if (items.size() == 0) {
            return null;
        }
        return items.stream().findFirst().get();
    }
}
