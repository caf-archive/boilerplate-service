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
package com.hpe.caf.boilerplate.api;

import com.hpe.caf.boilerplate.api.exceptions.InvalidRequestException;
import com.hpe.caf.boilerplate.api.exceptions.ItemNotFoundException;
import com.hpe.caf.boilerplate.api.exceptions.ParameterOutOfRangeException;
import com.hpe.caf.boilerplate.api.exceptions.TransitoryBackEndFailureException;
import com.hpe.caf.boilerplate.api.hibernate.ExecutionContext;
import com.hpe.caf.boilerplate.api.hibernate.ExecutionContextProvider;
import com.hpe.caf.boilerplate.api.hibernate.repositories.BoilerplateExpressionRepositoryImpl;
import com.hpe.caf.boilerplate.api.hibernate.repositories.TagRepositoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Michael.McAlynn on 09/12/2015.
 */
@Component
public class BoilerplateApiRepositoryImpl implements BoilerplateApi {
    private BoilerplateExpressionRepositoryImpl boilerplateExpressionRepository;
    private TagRepositoryImpl tagRepository;
    private ExecutionContextProvider contextProvider;
    private static final Logger logger = LoggerFactory.getLogger(BoilerplateApiRepositoryImpl.class);

    @Autowired
    public BoilerplateApiRepositoryImpl(ExecutionContextProvider contextProvider,
                                        BoilerplateExpressionRepositoryImpl boilerplateExpressionRepository,
                                        TagRepositoryImpl tagRepository) {
        this.boilerplateExpressionRepository = boilerplateExpressionRepository;
        this.tagRepository = tagRepository;
        this.contextProvider = contextProvider;
    }

    @Override
    public Tag getTag(long id) {
        try (ExecutionContext context = contextProvider.getExecutionContext()) {
            return context.retryNonTransactional(r -> {
                Tag result = tagRepository.retrieve(context, Collections.singletonList(id)).stream().findFirst().get();
                return result;
            });
        } catch (TransitoryBackEndFailureException | ItemNotFoundException | ParameterOutOfRangeException e) {
            throw e;
        }  catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Tag> getTags() {
        try (ExecutionContext context = contextProvider.getExecutionContext()) {
            return context.retryNonTransactional(r -> {
                List<Tag> result = new ArrayList(tagRepository.retrieveAll(context));
                return result;
            });
        } catch (TransitoryBackEndFailureException | ItemNotFoundException | ParameterOutOfRangeException e) {
            throw e;
        }  catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Tag> getTags(int pageNumber, int size) {
        if (pageNumber < 1) {
            throw new ParameterOutOfRangeException("Invalid Parameter", new Exception("The field start must be >0"));
        }
        try (ExecutionContext context = contextProvider.getExecutionContext()) {
            return context.retryNonTransactional(r -> {
                List<Tag> result = new ArrayList<>(tagRepository.retrievePaged(context, size, pageNumber));
                return result;
            });
        } catch (TransitoryBackEndFailureException | ItemNotFoundException | ParameterOutOfRangeException e) {
            throw e;
        }  catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BoilerplateExpression getExpression(long id) {
        try (ExecutionContext context = contextProvider.getExecutionContext()) {
            return context.retryNonTransactional(r -> {
                BoilerplateExpression expression = boilerplateExpressionRepository.retrieve(context, Collections.singletonList(id)).stream().findFirst().get();
                return expression;
            });
        } catch (TransitoryBackEndFailureException | ItemNotFoundException | ParameterOutOfRangeException e) {
            throw e;
        }  catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<BoilerplateExpression> getExpressions(int pageNumber, int size) {
        if (pageNumber < 1) {
            throw new ParameterOutOfRangeException("Invalid Parameter", new Exception("The field start must be >0"));
        }
        try (ExecutionContext context = contextProvider.getExecutionContext()) {
            return context.retryNonTransactional(r -> {
                List<BoilerplateExpression> result = new ArrayList<>(boilerplateExpressionRepository.retrievePaged(context, size, pageNumber));
                return result;
            });
        } catch (TransitoryBackEndFailureException | ItemNotFoundException | ParameterOutOfRangeException e) {
            throw e;
        }  catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Tag> getTagsByExpressionId(long id) {
        try (ExecutionContext context = contextProvider.getExecutionContext()) {
            return context.retryNonTransactional(r -> {
                List<Tag> result = new ArrayList<>(tagRepository.retrieveByExpression(context, id));
                return result;
            });
        } catch (TransitoryBackEndFailureException | ItemNotFoundException | ParameterOutOfRangeException e) {
            throw e;
        }  catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<BoilerplateExpression> getExpressionsByTagId(long id) {
        try (ExecutionContext context = contextProvider.getExecutionContext()) {
            return context.retryNonTransactional(r -> {
                List<BoilerplateExpression> result = new ArrayList<>(boilerplateExpressionRepository.retrieveByTag(context, getTag(id)));
                return result;
            });
        } catch (TransitoryBackEndFailureException | ItemNotFoundException | ParameterOutOfRangeException e) {
            throw e;
        }  catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<BoilerplateExpression> getExpressionsByTagId(long id, int pageNumber, int size) {
        try (ExecutionContext context = contextProvider.getExecutionContext()) {
            return context.retryNonTransactional(r -> {
                List<BoilerplateExpression> result = new ArrayList<>(boilerplateExpressionRepository.retrieveByTagPaged(context, getTag(id), size, pageNumber));
                return result;
            });
        } catch (TransitoryBackEndFailureException | ItemNotFoundException | ParameterOutOfRangeException e) {
            throw e;
        }  catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Tag createTag(Tag tag) {
        try (ExecutionContext context = contextProvider.getExecutionContext()) {
            return context.retry(r -> {
                Tag result = tagRepository.create(context, tag);
                return result;
            });
        } catch (TransitoryBackEndFailureException | ItemNotFoundException | ParameterOutOfRangeException e) {
            throw e;
        }  catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BoilerplateExpression createExpression(BoilerplateExpression expression) {
        try (ExecutionContext context = contextProvider.getExecutionContext()) {
            return context.retry(r -> {
                BoilerplateExpression result = boilerplateExpressionRepository.create(context, expression);
                return result;
            });
        } catch (TransitoryBackEndFailureException | ItemNotFoundException | ParameterOutOfRangeException | InvalidRequestException e) {
            throw e;
        }  catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Tag updateTag(Tag tag) {
        try (ExecutionContext context = contextProvider.getExecutionContext()) {
            return context.retry(r -> {
                Tag result = tagRepository.update(context, tag);
                return result;
            });
        } catch (TransitoryBackEndFailureException | ItemNotFoundException | ParameterOutOfRangeException e) {
            throw e;
        }  catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BoilerplateExpression updateExpression(BoilerplateExpression expression) {
        try (ExecutionContext context = contextProvider.getExecutionContext()) {
            return context.retry(r -> {
                BoilerplateExpression result = boilerplateExpressionRepository.update(context, expression);
                return result;
            });
        } catch (TransitoryBackEndFailureException | ItemNotFoundException | ParameterOutOfRangeException e) {
            throw e;
        }  catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Tag deleteTag(long id) {
        try (ExecutionContext context = contextProvider.getExecutionContext()) {
            return context.retry(r -> {
                return tagRepository.delete(context, id);
            });
        } catch (TransitoryBackEndFailureException | ItemNotFoundException | ParameterOutOfRangeException e) {
            throw e;
        }  catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BoilerplateExpression deleteExpression(long id) {
        try (ExecutionContext context = contextProvider.getExecutionContext()) {
            return context.retry(r -> {
                return boilerplateExpressionRepository.delete(context, id);
            });
        } catch (TransitoryBackEndFailureException | ItemNotFoundException | ParameterOutOfRangeException e) {
            throw e;
        }  catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
