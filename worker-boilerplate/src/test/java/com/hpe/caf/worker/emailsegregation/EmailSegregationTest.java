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
package com.hpe.caf.worker.emailsegregation;

import com.hpe.caf.api.worker.DataStore;
import com.hpe.caf.util.ref.ReferencedData;
import com.hpe.caf.worker.boilerplate.emailsegregation.EmailSegregation;
import com.hpe.caf.worker.boilerplateshared.BoilerplateWorkerConstants;
import com.hpe.caf.worker.boilerplateshared.response.BoilerplateResult;
import java.nio.charset.StandardCharsets;
import jep.Jep;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Unit tests for the EmailSegregation Class
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ContentSegregation.class, Jep.class})
@SuppressStaticInitializationFor("jep.Jep")
public class EmailSegregationTest {

    private ContentSegregation contentSegregation;
    private Jep jep;
    private ThreadLocal<Jep> threadLocal;
    private EmailSegregation emailSegregation;
    private ExecutorService jepThreadPool = Executors.newSingleThreadExecutor();
    private String dataStoreReference = UUID.randomUUID().toString();
    private static String expectedLineMarkers = "teeeeeeeteeeteeeteeeteeeteteteeeteteteeeteteteteeeteteeeteteteeeteteteteeeeeeeteeeeeeeteeeeeeeseteteteteteteeeeeeeteeeeeeeteteeeeeeeteeeteteeeeeeeteeeteteeeeeeeteteeeteeeeeeeteeet";

    @Mock
    private DataStore dataStore;

    @Before
    public void setup() throws Exception {
        setUpJep();
        PowerMockito.mock(DataStore.class);
        Mockito.when(dataStore.store(Mockito.any(byte[].class), Mockito.contains(dataStoreReference))).thenReturn(dataStoreReference + "/" + UUID.randomUUID().toString());
        emailSegregation = new EmailSegregation(jepThreadPool, dataStore, dataStoreReference, 10000);

    }

    @Test
    public void testEmailSegregation() throws ExecutionException, InterruptedException {
        List<String> separatedEmails = emailSegregation.getSeparatedEmails(emailChain);
        Assert.assertEquals("Should have split the email into two,", 2, separatedEmails.size());
        Assert.assertEquals("First Email should match", firstEmail, separatedEmails.get(0)+"\n");
        Assert.assertEquals("Second Email should match", secondEmail, separatedEmails.get(1));
    }

    @Test
    public void testKeyContentSegregation() {
        String primaryExpression = "0";
        String secondaryExpression = "LAST";
        String tertiaryExpression = null;
        BoilerplateResult boilerplateResult = emailSegregation.retrieveKeyContent(emailChain, primaryExpression, secondaryExpression, tertiaryExpression);
        ReferencedData referencedData = boilerplateResult.getGroupedMatches().get(BoilerplateWorkerConstants.PRIMARY_CONTENT).stream().findFirst().orElse(null);
        Assert.assertNotNull("Should have a primary content field", referencedData);
        Assert.assertEquals("Primary content should match", firstEmail, new String(referencedData.getData(), StandardCharsets.UTF_8));

    }

    private void setUpJep() throws Exception {
        jep = PowerMockito.mock(Jep.class);
        PowerMockito.whenNew(Jep.class).withAnyArguments().thenReturn(jep);
        threadLocal = PowerMockito.mock(ThreadLocal.class);
        PowerMockito.when(threadLocal.get()).thenReturn(jep);
        contentSegregation = new ContentSegregation();
        Whitebox.setInternalState(contentSegregation, "threadLocal", threadLocal);
        PowerMockito.when(jep.eval(Mockito.anyString())).thenReturn(true);
        PowerMockito.when(jep.getValue(Mockito.same("x"))).thenReturn(expectedLineMarkers);
    }


    private String emailChain = "Sayed,\n" +
            "\n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            "No, this doesn’t work for us either. \n" +
            " \n" +
            " \n" +
            " \n" +
            "I’ll try to explain what we need, with some additional description: \n" +
            " \n" +
            " \n" +
            " \n" +
            "1)      Example project \n" +
            " \n" +
            " \n" +
            " \n" +
            "What is an example project: \n" +
            " \n" +
            " \n" +
            " \n" +
            "A project with a reference to iSTF in the pom file that is not testing \n" +
            " \n" +
            "any product. Something that we can run here without any additional \n" +
            " \n" +
            "external dependencies. \n" +
            " \n" +
            " \n" +
            " \n" +
            "Aspen tests are no good because we do not have aspen here, same for any \n" +
            " \n" +
            "other tests you have there. We need something that we can *easily* build \n" +
            " \n" +
            "and run here. \n" +
            " \n" +
            " \n" +
            " \n" +
            "Just a project with a few example tests – something that, let’s say, \n" +
            " \n" +
            "adds numbers and verifies the sum is correct. It should use iSTF to do \n" +
            " \n" +
            "that. No external dependencies except iSTF should be present in this \n" +
            " \n" +
            "project. \n" +
            " \n" +
            " \n" +
            " \n" +
            "In the example project there should be no copy/pasted code from iSTF \n" +
            " \n" +
            "base code. Just reference(s) to iSTF libraries. \n" +
            " \n" +
            " \n" +
            " \n" +
            "2)      Written instructions how to configure iSTF stack and run the \n" +
            " \n" +
            "tests in example project. This should be just a list of steps we need to \n" +
            " \n" +
            "make to get the sample running. \n" +
            " \n" +
            " \n" +
            " \n" +
            "3)      Packaged iSTF – we will need this in maven repository before we \n" +
            " \n" +
            "integrate. It’s ok to supply example project with a JAR but before we \n" +
            " \n" +
            "start integrating it here, this needs to be in maven repository so we \n" +
            " \n" +
            "can consume it. \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            "Please let me know if you need some more details. \n" +
            " \n" +
            " \n" +
            " \n" +
            "  \n" +
            " \n" +
            " \n" +
            " \n" +
            "-Kris \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            "From: Sayed, Mohammed Chand Kousar  \n" +
            " \n" +
            "Sent: 14 March 2016 10:56 \n" +
            " \n" +
            "To: Ploch, Krzysztof <krzysztof.ploch@hpe.com> \n" +
            " \n" +
            "Cc: Reid, Andy <andrew.reid@hpe.com>; Paul, Navamoni \n" +
            " \n" +
            "<paul.navamoni@hpe.com>; Yanamalamanda, Purushotham \n" +
            " \n" +
            "<purushotham.yanamalamanda@hpe.com> \n" +
            " \n" +
            "Subject: RE: iSTF example code \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            "Hi Krzystof, \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            "I have already added you to share point page some time ago where we \n" +
            " \n" +
            "have documentation related to iSTF \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            "iSTF code base: \n" +
            " \n" +
            " \n" +
            " \n" +
            "http://16.185.208.26/CoreAutomation/Gen_jelly \n" +
            " \n" +
            "<http://16.185.208.26/CoreAutomation/Gen_jelly>  \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            "for API example: \n" +
            " \n" +
            " \n" +
            " \n" +
            "http://16.185.208.26/CoreAutomation/Chateau/qa_aspen \n" +
            " \n" +
            "<http://16.185.208.26/CoreAutomation/Chateau/qa_aspen>  \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            "In the above qa_aspen project example we can make use of common classes \n" +
            " \n" +
            "what we have rather aspen specific. \n" +
            " \n" +
            " \n" +
            " \n" +
            "This is what we have, I think this is good enough to go ahead. \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            "Thanks, \n" +
            " \n" +
            " \n" +
            " \n" +
            "Sayed MCK\n";

    private static String firstEmail = "Sayed,\n" +
            "\n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            "No, this doesn’t work for us either. \n" +
            " \n" +
            " \n" +
            " \n" +
            "I’ll try to explain what we need, with some additional description: \n" +
            " \n" +
            " \n" +
            " \n" +
            "1)      Example project \n" +
            " \n" +
            " \n" +
            " \n" +
            "What is an example project: \n" +
            " \n" +
            " \n" +
            " \n" +
            "A project with a reference to iSTF in the pom file that is not testing \n" +
            " \n" +
            "any product. Something that we can run here without any additional \n" +
            " \n" +
            "external dependencies. \n" +
            " \n" +
            " \n" +
            " \n" +
            "Aspen tests are no good because we do not have aspen here, same for any \n" +
            " \n" +
            "other tests you have there. We need something that we can *easily* build \n" +
            " \n" +
            "and run here. \n" +
            " \n" +
            " \n" +
            " \n" +
            "Just a project with a few example tests – something that, let’s say, \n" +
            " \n" +
            "adds numbers and verifies the sum is correct. It should use iSTF to do \n" +
            " \n" +
            "that. No external dependencies except iSTF should be present in this \n" +
            " \n" +
            "project. \n" +
            " \n" +
            " \n" +
            " \n" +
            "In the example project there should be no copy/pasted code from iSTF \n" +
            " \n" +
            "base code. Just reference(s) to iSTF libraries. \n" +
            " \n" +
            " \n" +
            " \n" +
            "2)      Written instructions how to configure iSTF stack and run the \n" +
            " \n" +
            "tests in example project. This should be just a list of steps we need to \n" +
            " \n" +
            "make to get the sample running. \n" +
            " \n" +
            " \n" +
            " \n" +
            "3)      Packaged iSTF – we will need this in maven repository before we \n" +
            " \n" +
            "integrate. It’s ok to supply example project with a JAR but before we \n" +
            " \n" +
            "start integrating it here, this needs to be in maven repository so we \n" +
            " \n" +
            "can consume it. \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            "Please let me know if you need some more details. \n" +
            " \n" +
            " \n" +
            " \n" +
            "  \n" +
            " \n" +
            " \n" +
            " \n" +
            "-Kris \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            "\n";

    private static String secondEmail = "From: Sayed, Mohammed Chand Kousar  \n" +
            " \n" +
            "Sent: 14 March 2016 10:56 \n" +
            " \n" +
            "To: Ploch, Krzysztof <krzysztof.ploch@hpe.com> \n" +
            " \n" +
            "Cc: Reid, Andy <andrew.reid@hpe.com>; Paul, Navamoni \n" +
            " \n" +
            "<paul.navamoni@hpe.com>; Yanamalamanda, Purushotham \n" +
            " \n" +
            "<purushotham.yanamalamanda@hpe.com> \n" +
            " \n" +
            "Subject: RE: iSTF example code \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            "Hi Krzystof, \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            "I have already added you to share point page some time ago where we \n" +
            " \n" +
            "have documentation related to iSTF \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            "iSTF code base: \n" +
            " \n" +
            " \n" +
            " \n" +
            "http://16.185.208.26/CoreAutomation/Gen_jelly \n" +
            " \n" +
            "<http://16.185.208.26/CoreAutomation/Gen_jelly>  \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            "for API example: \n" +
            " \n" +
            " \n" +
            " \n" +
            "http://16.185.208.26/CoreAutomation/Chateau/qa_aspen \n" +
            " \n" +
            "<http://16.185.208.26/CoreAutomation/Chateau/qa_aspen>  \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            "In the above qa_aspen project example we can make use of common classes \n" +
            " \n" +
            "what we have rather aspen specific. \n" +
            " \n" +
            " \n" +
            " \n" +
            "This is what we have, I think this is good enough to go ahead. \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            "Thanks, \n" +
            " \n" +
            " \n" +
            " \n" +
            "Sayed MCK\n";
}
