/*
 * Copyright 2017-2020 Micro Focus or one of its affiliates.
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
import org.hibernate.sql.JoinType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by gibsodom on 09/12/2015.
 */
@Component
public class TagRepositoryImpl extends HibernateBaseRepository<Tag> {

    @Autowired
    public TagRepositoryImpl(UserContext userContext) {
        super(userContext);
    }

    @Override
    public Tag create(ExecutionContext context, Tag tag) {
        tag.setId(null);
        if (tag.getId() != null) {
            throw new RuntimeException("Id should be null");
        }
        if(tag.getBoilerplateExpressions() == null){
            throw new InvalidRequestException("Boilerplate expressions should not be null.", new Exception("Unable to save Tag."), ItemType.TAG);
        }
        tag.setBoilerplateExpressions(new HashSet<>(tag.getBoilerplateExpressions()));
        tag = preSave(tag, context.getSession());
        context.getSession().save(tag);
        context.getSession().flush();
        evict(context.getSession(), tag);
        return retrieveSingleItem(context, tag.getId());
    }

    @Override
    public Tag update(ExecutionContext context, Tag tag) {
        retrieve(context, Collections.singletonList(tag.getId()));
        tag.setBoilerplateExpressions(new HashSet<>(tag.getBoilerplateExpressions()));
        preSave(tag,context.getSession());
        context.getSession().update(tag);
        context.getSession().flush();
        evict(context.getSession(), tag);
        return retrieveSingleItem(context, tag.getId());
    }

    @Override
    public Tag delete(ExecutionContext context, Long id) {
        Tag tag = retrieveSingleItem(context, id);
        context.getSession().delete(tag);
//        String hql = "delete from Tag where id= :id";
//        int i = context.getSession().createQuery(hql)
//                .setLong("id", id)
//                .executeUpdate();
//        if (i == 0) {
//            throw new RuntimeException("Tag could not be deleted");
//        }
        return tag;
    }

    @Override
    public Collection<Tag> retrieve(ExecutionContext context, Collection<Long> ids) {
        Criteria criteria = context.getSession().createCriteria(Tag.class)
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.add(Restrictions.in("id", ids))
                .add(Restrictions.eq("projectId", userContext.getProjectId()))
                .addOrder(Order.asc("id"));
        List<Tag> items = criteria.list();
        evict(context.getSession(), items);

        if (!ids.stream().distinct().allMatch(u -> items.stream().anyMatch(i -> Objects.equals(i.getId(), u)))) {
            if (ids.size() == 1) {

                throw new ItemNotFoundException("Request Failure",new Exception("Could not find a match for the Tag requested."), ItemType.TAG);

            }

            throw new ItemNotFoundException("Request Failure",new Exception("Could not find a match for all Tags requested."), ItemType.TAG);

        }
        return items;
    }

    @Override
    public Tag preSave(Tag tag, Session session) {
        String projectId = userContext.getProjectId();
        if (projectId != null && !projectId.isEmpty()) {
            if (tag.getBoilerplateExpressions().size() > 0) {

                Criteria criteria = session.createCriteria(BoilerplateExpression.class)
                        .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
                criteria.add(Restrictions.in("id", tag.getBoilerplateExpressions()))
                        .add(Restrictions.eq("projectId", userContext.getProjectId()));
                List<BoilerplateExpression> items = criteria.list();
                if (items.size() != tag.getBoilerplateExpressions().size()) {
                    throw new ItemNotFoundException("Request Failure",new RuntimeException("Cannot save Tag using Boilerplate Expression ids from different tenant."), ItemType.BOILERPLATE_EXPRESSION);
                }
                for (BoilerplateExpression i : items) {
                    session.evict(i);
                }
//                evict(session, items);
            }
            return tag;
        }
        throw new ParameterOutOfRangeException("Missing Parameter", new Exception("Cannot save item, projectId is null."));
    }

    @Override
    public Collection<Tag> retrieveAll(ExecutionContext context) {
        String projectId = userContext.getProjectId();
        Criteria criteria = context.getSession().createCriteria(Tag.class)
                .add(Restrictions.eq("projectId", projectId))
                .addOrder(Order.asc("id"));
        Collection<Tag> results = criteria.list();
        for (Tag tag : results) {
            context.getSession().evict(tag);
        }
        return results;
    }

    @Override
    public Collection<Tag> retrievePaged(ExecutionContext context, int maxSize, int startPage) {
        String projectId = userContext.getProjectId();
        Criteria criteria = context.getSession().createCriteria(Tag.class)
                .add(Restrictions.eq("projectId", projectId))
                .addOrder(Order.asc("id"))
                .setMaxResults(maxSize)
                .setFirstResult(startPage - 1);
        Collection<Tag> results = criteria.list();
        for (Tag tag : results) {
            context.getSession().evict(tag);
        }
        return results;
    }

    public Collection<Tag> retrieveByExpression(ExecutionContext context, Long expressionId) {
        String projectId = userContext.getProjectId();

        Collection<Tag> results = context.getSession()
                .createCriteria(Tag.class)
                .createAlias("boilerplateExpressions", "boilerplateExpressions", JoinType.INNER_JOIN)
                .addOrder(Order.asc("id")).add(Restrictions.isNotEmpty("boilerplateExpressions"))
                .add(Restrictions.in("boilerplateExpressions.elements", Collections.singletonList(expressionId)))
                .add(Restrictions.eq("projectId", projectId))
                .list();
        for (Tag tag : results) {
            context.getSession().evict(tag);
        }
        return results;
    }

}
