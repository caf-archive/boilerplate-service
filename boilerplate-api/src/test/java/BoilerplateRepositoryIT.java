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
import com.hpe.caf.boilerplate.api.*;
import com.hpe.caf.boilerplate.api.hibernate.*;
import com.hpe.caf.boilerplate.api.hibernate.repositories.BoilerplateExpressionRepositoryImpl;
import com.hpe.caf.boilerplate.api.hibernate.repositories.TagRepositoryImpl;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.util.*;

/**
 * Created by gibsodom on 09/12/2015.
 */
public class BoilerplateRepositoryIT {
    private static HibernateProperties properties = getProperties();
    private HibernateSessionFactory sessionFactory;
    private UserContext userContext = new UserContextImpl();
    private TagRepositoryImpl tagRepository;// = new TagRepositoryImpl(new UserContextImpl());
    private BoilerplateExpressionRepositoryImpl boilerplateExpressionRepository;
    private BoilerplateApi sut;// = BoilerplateExpressionRepositoryImpl
    private PreInsertInterceptor preInsertInterceptor;// = new PreInsertInterceptor(UUID.randomUUID().toString());
    private static boolean setUp = false;
    private ExecutionContextProvider contextProvider;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    public BoilerplateRepositoryIT() throws Exception {
        userContext = new UserContextImpl();
        userContext.setProjectId(UUID.randomUUID().toString());
        tagRepository = new TagRepositoryImpl(userContext);
        boilerplateExpressionRepository = new BoilerplateExpressionRepositoryImpl(userContext);

        preInsertInterceptor = new PreInsertInterceptor(userContext);
        sessionFactory = new HibernateSessionFactory(properties, preInsertInterceptor);
        setUp = true;

//        context = new ExecutionContext(sessionFactory.getSession());
//        context = new ExecutionContext(new HibernateSessionFactory(properties, preInsertInterceptor));
        contextProvider = new ExecutionContextProvider(sessionFactory);
        sut = new BoilerplateApiRepositoryImpl(contextProvider, boilerplateExpressionRepository, tagRepository);

    }

    @Before
    public void setup() throws Exception {
//        userContext.setProjectId(UUID.randomUUID().toString());

    }

    @After
    public void clearUp() {
        contextProvider.closeExecutionContext();
    }

    @Test
    public void testCreateTag() throws Exception {
        Tag savedTag;
        Tag tagToSave = createNewTag();
        sut.createTag(tagToSave);
        savedTag = sut.getTag(tagToSave.getId());
        compareTags(tagToSave, savedTag);
    }

    @Test
    public void testCreateTagWithLongDescription() throws Exception {
        Tag savedTag;
        Tag tagToSave = createNewTag();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            stringBuilder.append("a");
        }
        createNewTag().setDescription(stringBuilder.toString());

        sut.createTag(tagToSave);
        savedTag = sut.getTag(tagToSave.getId());
        compareTags(tagToSave, savedTag);
    }

    @Test
    public void testCreateBoilerplateExpression() throws Exception {
        BoilerplateExpression savedBoilerplate;
        BoilerplateExpression expressionToSave = createNewBoilerplate();
        sut.createExpression(expressionToSave);
        savedBoilerplate = sut.getExpression(expressionToSave.getId());
        compareExpressions(expressionToSave, savedBoilerplate);
    }

    @Test
    public void testCreateBoilerplateExpressionWithLongDescription() throws Exception {
        BoilerplateExpression savedBoilerplate;
        BoilerplateExpression expressionToSave = createNewBoilerplate();

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            stringBuilder.append("a");
        }

        expressionToSave.setDescription(stringBuilder.toString());
        expressionToSave.setExpression(stringBuilder.toString());

        sut.createExpression(expressionToSave);
        savedBoilerplate = sut.getExpression(expressionToSave.getId());
        compareExpressions(expressionToSave, savedBoilerplate);
    }

    @Test
    public void testCreateTagWithExpressionId() throws Exception {
        BoilerplateExpression savedBoilerplate;
        BoilerplateExpression expressionToSave = createNewBoilerplate();
        savedBoilerplate = sut.createExpression(expressionToSave);
        compareExpressions(expressionToSave, savedBoilerplate);

        Tag tagToSave = createNewTag();
        Tag savedTag;
        tagToSave.getBoilerplateExpressions().add(savedBoilerplate.getId());
        sut.createTag(tagToSave);
        savedTag = sut.getTag(tagToSave.getId());

        compareTags(tagToSave, savedTag);

    }

    @Test
    public void testRetrieveAllTags() {
        Tag tag1 = sut.createTag(createNewTag());
        Tag tag2 = sut.createTag(createNewTag());
        List<Tag> retrievedTags = sut.getTags();
        Assert.assertNotNull(retrievedTags);
        Assert.assertEquals("Should retrieve only 2 tags", 2, retrievedTags.size());
        Tag savedTag1 = retrievedTags.stream().filter(e ->
                e.getId().equals(tag1.getId())
        ).findFirst().get();
        Assert.assertNotNull(savedTag1);
        compareTags(tag1, savedTag1);
        Tag savedTag2 = retrievedTags.stream().filter(e -> e.getId().equals(tag2.getId())).findFirst().get();
        Assert.assertNotNull(savedTag2);
        compareTags(tag2, savedTag2);
    }

    @Test
    public void testRetrieveTagPage() throws Exception {
        List<Long> createdIds = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            Tag tag = sut.createTag(createNewTag());
            createdIds.add(tag.getId());
        }
        List<Tag> retrievedTags = sut.getTags(1, 10);
        List<Long> retrievedIds = new ArrayList<>();
        retrievedTags.stream().forEachOrdered(e -> retrievedIds.add(e.getId()));
        Assert.assertEquals("Should have retrieved back 10 tags", 10, retrievedIds.size());
        Assert.assertTrue("Ids should match", createdIds.containsAll(retrievedIds));

        retrievedIds.removeAll(createdIds);
        retrievedTags = sut.getTags(1, 20);
        retrievedTags.stream().forEachOrdered(e -> retrievedIds.add(e.getId()));
        Assert.assertEquals("Should have retrieved back 15 tags", 15, retrievedIds.size());

        retrievedIds.removeAll(createdIds);
        retrievedTags = sut.getTags(16, 20);
        retrievedTags.stream().forEachOrdered(e -> retrievedIds.add(e.getId()));
        Assert.assertEquals("Should have retrieved no results, start is outside range of tags created", 0, retrievedIds.size());
    }

    @Test
    public void testRetrieveExpressionPage() throws Exception {
        List<Long> createdIds = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            BoilerplateExpression expression = sut.createExpression(createNewBoilerplate());
            createdIds.add(expression.getId());
        }
        List<BoilerplateExpression> retrievedExpressions = sut.getExpressions(1, 10);
        List<Long> retrievedIds = new ArrayList<>();
        retrievedExpressions.stream().forEachOrdered(e -> retrievedIds.add(e.getId()));
        Assert.assertEquals("Should have retrieved back 10 tags", 10, retrievedIds.size());
        Assert.assertTrue("Ids should match", createdIds.containsAll(retrievedIds));

        retrievedIds.removeAll(createdIds);
        retrievedExpressions = sut.getExpressions(1, 20);
        retrievedExpressions.stream().forEachOrdered(e -> retrievedIds.add(e.getId()));
        Assert.assertEquals("Should have retrieved back 15 tags", 15, retrievedIds.size());

        retrievedIds.removeAll(createdIds);
        retrievedExpressions = sut.getExpressions(16, 20);
        retrievedExpressions.stream().forEachOrdered(e -> retrievedIds.add(e.getId()));
        Assert.assertEquals("Should have retrieved no results, start is outside range of tags created", 0, retrievedIds.size());
    }

    @Test
    public void testGetTagByExpression() {
        BoilerplateExpression expression = sut.createExpression(createNewBoilerplate());
        Tag tag = createNewTag();
        tag.getBoilerplateExpressions().add(expression.getId());
        tag = sut.createTag(tag);
        Tag savedTag = sut.getTagsByExpressionId(expression.getId()).stream().findFirst().get();
        compareTags(tag, savedTag);
    }

    @Test
    public void testGetExpressionByTag() {
        BoilerplateExpression expression1 = sut.createExpression(createNewBoilerplate());
        Tag tag = createNewTag();
        tag.getBoilerplateExpressions().add(expression1.getId());
        BoilerplateExpression expression2 = sut.createExpression(createNewBoilerplate());
        tag.getBoilerplateExpressions().add(expression2.getId());
        tag = sut.createTag(tag);
        List<BoilerplateExpression> savedExpressions = sut.getExpressionsByTagId(tag.getId());
        Assert.assertNotNull(savedExpressions);
        Assert.assertEquals("Should have retrieved two tags", 2, savedExpressions.size());
        BoilerplateExpression savedExpression1 = savedExpressions.stream().filter(e -> e.getId().equals(expression1.getId())).findFirst().get();
        compareExpressions(expression1, savedExpression1);
        BoilerplateExpression savedExpression2 = savedExpressions.stream().filter(e -> e.getId().equals(expression2.getId())).findFirst().get();
        compareExpressions(expression2, savedExpression2);
    }


    @Test
    public void testUpdateTag() {
        Tag tag = sut.createTag(createNewTag());
        Assert.assertNotNull(tag);
        Tag updatedTag = sut.getTag(tag.getId());
        Assert.assertNotNull(updatedTag);
        updatedTag.setName("Updated Name for test");
        updatedTag = sut.updateTag(updatedTag);
        Assert.assertEquals("Ids should match", tag.getId(), updatedTag.getId());
        Assert.assertNotEquals("Names should have changed", tag.getName(), updatedTag.getName());
        Assert.assertEquals("Replacement Text should match", tag.getDefaultReplacementText(), updatedTag.getDefaultReplacementText());
        Assert.assertEquals("Boilerplate Expressions size should match", tag.getBoilerplateExpressions().size(), updatedTag.getBoilerplateExpressions().size());
        Assert.assertEquals("Description should match", tag.getDescription(), updatedTag.getDescription());
    }

    @Test
    public void testUpdateExpression() {
        BoilerplateExpression expression = sut.createExpression(createNewBoilerplate());
        Assert.assertNotNull(expression);
        BoilerplateExpression updatedExpression = sut.getExpression(expression.getId());
        Assert.assertNotNull(updatedExpression);
        updatedExpression.setName("Changed Name for update Test");
        updatedExpression.setDescription("Changed desc for update Test");
        updatedExpression = sut.updateExpression(updatedExpression);
        Assert.assertEquals("Ids should mathc", expression.getId(), updatedExpression.getId());
        Assert.assertNotEquals("Name should have been updated", expression.getName(), updatedExpression.getName());
        Assert.assertNotEquals("Description should have changed", expression.getDescription(), updatedExpression.getDescription());
    }

    @Test
    public void testDeleteTag() {
        Tag tag = sut.createTag(createNewTag());
        Assert.assertNotNull(tag);
        sut.deleteTag(tag.getId());
        exception.expect(RuntimeException.class);
        sut.getTag(tag.getId());
    }

    @Test
    public void testDeleteExpressions() {
        BoilerplateExpression expression = sut.createExpression(createNewBoilerplate());
        Assert.assertNotNull(expression);
        sut.deleteExpression(expression.getId());
        exception.expect(RuntimeException.class);
        sut.getExpression(expression.getId());
    }

    private static HibernateProperties getProperties() {
        AnnotationConfigApplicationContext propertiesApplicationContext = new AnnotationConfigApplicationContext();
        propertiesApplicationContext.register(PropertySourcesPlaceholderConfigurer.class);
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setBeanClass(HibernateProperties.class);
        propertiesApplicationContext.registerBeanDefinition("HibernateProperties", beanDefinition);
        propertiesApplicationContext.refresh();
        return propertiesApplicationContext.getBean(HibernateProperties.class);
    }

    private void compareTags(Tag expected, Tag actual) {
        Assert.assertNotNull(actual);
        Assert.assertNotNull(actual.getId());
        Assert.assertEquals("Names should match", expected.getName(), actual.getName());
        Assert.assertEquals("Description should match", expected.getDescription(), actual.getDescription());
        Assert.assertEquals("Default replacement text should match", expected.getDefaultReplacementText(), actual.getDefaultReplacementText());
        Assert.assertEquals("Number of boilerplate expression ids should match", expected.getBoilerplateExpressions().size(), actual.getBoilerplateExpressions().size());
        Assert.assertTrue("Boilerplate expression ids should match", expected.getBoilerplateExpressions().containsAll(actual.getBoilerplateExpressions()));
    }

    private void compareExpressions(BoilerplateExpression expected, BoilerplateExpression actual) {
        Assert.assertNotNull(actual);
        Assert.assertNotNull(actual.getId());
        Assert.assertEquals("Names should match", expected.getName(), actual.getName());
        Assert.assertEquals("Description should match", expected.getDescription(), actual.getDescription());
        Assert.assertEquals("Expression should match", expected.getExpression(), actual.getExpression());
        Assert.assertEquals("Replacement Text should match", expected.getReplacementText(), actual.getReplacementText());
    }

    private Tag createNewTag() {
        Tag tagToSave = new Tag();
        tagToSave.setName(UUID.randomUUID().toString());
        tagToSave.setDescription("Test create Tag");
        tagToSave.setDefaultReplacementText("[REDACTED]");
        tagToSave.setBoilerplateExpressions(new HashSet<>());
        return tagToSave;
    }

    private BoilerplateExpression createNewBoilerplate() {
        BoilerplateExpression expression = new BoilerplateExpression();
        expression.setName("Test BoilerplateExpression" + new Random().nextInt());
        expression.setDescription("Test boilerplate expression");
        expression.setExpression("/[a-zA-Z]");
        return expression;
    }
}
