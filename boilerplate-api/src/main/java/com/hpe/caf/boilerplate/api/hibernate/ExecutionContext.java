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
package com.hpe.caf.boilerplate.api.hibernate;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by gibsodom on 10/12/2015.
 */
public class ExecutionContext implements AutoCloseable {

    private Session session;

    public ExecutionContext(Session session) {

        this.session = session;
    }

    public Session getSession() {
        return session;
    }

    public <R> R retry(Function<?, R> retry) {
        Transaction transaction = null;
        try {
            transaction = this.session.beginTransaction();
            R result = retry.apply(null);
            transaction.commit();
            return result;
        } catch (Exception e) {
            if(transaction != null) {
                try {
                    transaction.rollback();
                } catch (Exception ignored) {
                }
            }
            if(e instanceof RuntimeException){
                throw e;
            }
            throw new RuntimeException(e);
        }
    }

    public void retryNoReturn(Consumer<?> retry) {
        Transaction transaction = null;
        try {
            transaction = this.session.beginTransaction();
            retry.accept(null);
            transaction.commit();
        } catch (Exception e) {
            if(transaction != null) {
                try {
                    transaction.rollback();
                } catch (Exception ignored) {
                }
            }
            if(e instanceof RuntimeException){
                throw e;
            }
            throw new RuntimeException(e);
        }
    }

    public <R> R retryNonTransactional(Function<?, R> retry) {

        try {
            R result = retry.apply(null);
            return result;
        } catch (Exception e) {
            if(e instanceof RuntimeException){
                throw e;
            }
            throw new RuntimeException(e);
        }
    }


    @Override
    public void close() throws Exception {
        session.close();
    }
}
