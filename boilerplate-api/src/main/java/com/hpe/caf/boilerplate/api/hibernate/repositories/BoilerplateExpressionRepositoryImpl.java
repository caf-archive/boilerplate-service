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
package com.hpe.caf.boilerplate.api.hibernate.repositories;

import com.hpe.caf.boilerplate.api.BoilerplateExpression;
import com.hpe.caf.boilerplate.api.Tag;
import com.hpe.caf.boilerplate.api.UserContext;
import com.hpe.caf.boilerplate.api.exceptions.InvalidRequestException;
import com.hpe.caf.boilerplate.api.exceptions.ItemNotFoundException;
import com.hpe.caf.boilerplate.api.exceptions.ItemType;
import com.hpe.caf.boilerplate.api.exceptions.ParameterOutOfRangeException;
import com.hpe.caf.boilerplate.api.hibernate.ExecutionContext;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Created by gibsodom on 09/12/2015.
 */
@Component
public class BoilerplateExpressionRepositoryImpl extends HibernateBaseRepository<BoilerplateExpression> {

    @Autowired
    public BoilerplateExpressionRepositoryImpl(UserContext userContext) {
        super(userContext);
    }

    @Override
    public BoilerplateExpression create(ExecutionContext context, BoilerplateExpression boilerplateExpression) {
        boilerplateExpression.setId(null);
        if (boilerplateExpression.getId() != null) {
            throw new RuntimeException("Id should be null");
        }
        if (boilerplateExpression.getExpression() == null) {
            throw new InvalidRequestException("expression should not be null.", new Exception("Unable to save Boilerplate Expression."), ItemType.BOILERPLATE_EXPRESSION);
        }
        context.getSession().save(boilerplateExpression);
        context.getSession().flush();
        evict(context.getSession(), boilerplateExpression);
        return retrieveSingleItem(context, boilerplateExpression.getId());
    }

    @Override
    public BoilerplateExpression update(ExecutionContext context, BoilerplateExpression boilerplateExpression) {
        retrieve(context, Collections.singletonList(boilerplateExpression.getId()));
        if (boilerplateExpression.getExpression() == null) {
            throw new InvalidRequestException("expression should not be null.", new Exception("Unable to update Boilerplate Expression."), ItemType.BOILERPLATE_EXPRESSION);
        }
        context.getSession().update(boilerplateExpression);
        context.getSession().flush();
        evict(context.getSession(), boilerplateExpression);
        return retrieveSingleItem(context, boilerplateExpression.getId());
    }

    @Override
    public BoilerplateExpression delete(ExecutionContext context, Long id) {
        BoilerplateExpression expression = retrieveSingleItem(context, id);
//        context.getSession().delete(expression);
        String hql = "delete from BoilerplateExpression where id= :id";
        int i = context.getSession().createQuery(hql)
                .setLong("id", id)
                .executeUpdate();
        if (i == 0) {
            throw new RuntimeException("Boilerplate Expression could not be deleted");
        }
        return expression;
    }

    @Override
    public Collection<BoilerplateExpression> retrieve(ExecutionContext context, Collection<Long> ids) {
        Criteria criteria = context.getSession().createCriteria(BoilerplateExpression.class)
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.add(Restrictions.in("id", ids))
                .add(Restrictions.eq("projectId", userContext.getProjectId()))
                .addOrder(Order.asc("id"));
        List<BoilerplateExpression> items = criteria.list();
        evict(context.getSession(), items);

        if (!ids.stream().distinct().allMatch(u -> items.stream().anyMatch(i -> Objects.equals(i.getId(), u)))) {
            if (ids.size() == 1) {
                throw new ItemNotFoundException("Request Failure", new Exception("Could not find a match for the BoilerplateExpression requested."), ItemType.BOILERPLATE_EXPRESSION);
            }
            throw new ItemNotFoundException("Request Failure", new Exception("Could not find a match for the BoilerplateExpressions requested."), ItemType.BOILERPLATE_EXPRESSION);

        }
        return items;
    }

    @Override
    public BoilerplateExpression preSave(BoilerplateExpression boilerplateExpression, Session session) {
        String projectId = userContext.getProjectId();
        if (projectId != null && !projectId.isEmpty()) {
            return boilerplateExpression;
        }
        throw new ParameterOutOfRangeException("Missing Parameter", new Exception("Cannot save item, projectId is null."));
    }

    @Override
    public Collection<BoilerplateExpression> retrieveAll(ExecutionContext context) {
        String projectId = userContext.getProjectId();
        Criteria criteria = context.getSession().createCriteria(BoilerplateExpression.class)
                .add(Restrictions.eq("projectId", projectId))
                .addOrder(Order.asc("id"));
        Collection<BoilerplateExpression> results = criteria.list();
        for (BoilerplateExpression expression : results) {
            context.getSession().evict(expression);
        }
        return results;
    }

    @Override
    public Collection<BoilerplateExpression> retrievePaged(ExecutionContext context, int maxSize, int startPage) {
        String projectId = userContext.getProjectId();
        Criteria criteria = context.getSession().createCriteria(BoilerplateExpression.class)
                .add(Restrictions.eq("projectId", projectId))
                .addOrder(Order.asc("id"))
                .setMaxResults(maxSize)
                .setFirstResult(startPage - 1);
        Collection<BoilerplateExpression> results = criteria.list();
        for (BoilerplateExpression expression : results) {
            context.getSession().evict(expression);
        }
        return results;
    }

    public Collection<BoilerplateExpression> retrieveByTag(ExecutionContext context, Tag tag) {
        Collection<Long> boilerplateExpressionsOnTag = tag.getBoilerplateExpressions();
        if (boilerplateExpressionsOnTag == null || boilerplateExpressionsOnTag.size() == 0) {
            return new ArrayList<>();
        }
        String projectId = userContext.getProjectId();
        Criteria criteria = context.getSession().createCriteria(BoilerplateExpression.class)
                .add(Restrictions.eq("projectId", projectId))
                .add(Restrictions.in("id", tag.getBoilerplateExpressions()))
                .addOrder(Order.asc("id"));
        Collection<BoilerplateExpression> results = criteria.list();
        for (BoilerplateExpression expression : results) {
            context.getSession().evict(expression);
        }
        return results;
    }

    public Collection<BoilerplateExpression> retrieveByTagPaged(ExecutionContext context, Tag tag, int maxSize, int startPage) {
        Collection<Long> boilerplateExpressionsOnTag = tag.getBoilerplateExpressions();
        if (boilerplateExpressionsOnTag == null || boilerplateExpressionsOnTag.size() == 0) {
            return new ArrayList<>();
        }
        String projectId = userContext.getProjectId();
        Criteria criteria = context.getSession().createCriteria(BoilerplateExpression.class)
                .add(Restrictions.eq("projectId", projectId))
                .add(Restrictions.in("id", tag.getBoilerplateExpressions()))
                .addOrder(Order.asc("id"))
                .setMaxResults(maxSize)
                .setFirstResult(startPage - 1);
        Collection<BoilerplateExpression> results = criteria.list();
        for (BoilerplateExpression expression : results) {
            context.getSession().evict(expression);
        }
        return results;
    }

}
