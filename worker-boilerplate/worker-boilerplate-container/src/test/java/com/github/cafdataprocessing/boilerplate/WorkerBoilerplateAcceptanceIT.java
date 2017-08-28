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
package com.github.cafdataprocessing.boilerplate;

import com.github.cafdataprocessing.boilerplate.testing.WorkerTestControllerProvider;
import com.hpe.caf.worker.testing.TestControllerSingle;
import com.hpe.caf.worker.testing.TestItem;
import com.hpe.caf.worker.testing.UseAsTestName;
import com.hpe.caf.worker.testing.UseAsTestName_TestBase;
import com.hpe.caf.worker.testing.execution.TestControllerProvider;
import com.hpe.caf.worker.testing.execution.TestRunnerSingle;
import org.testng.annotations.*;
import java.util.Iterator;
import java.util.Set;

/**
 * Integration test for worker-boilerplate, running the testing framework.
 */
public class WorkerBoilerplateAcceptanceIT extends UseAsTestName_TestBase
{
    TestControllerProvider testControllerProvider;
    TestControllerSingle controller;

    @BeforeClass
    public void setUp() throws Exception
    {
        testControllerProvider = new WorkerTestControllerProvider();
        controller = TestRunnerSingle.getTestController(testControllerProvider, true);
        controller.initialise();
    }

    @AfterClass
    public void tearDown() throws Exception
    {
        controller.close();
    }

    @DataProvider(name = "MainTest")
    public Iterator<Object[]> createData() throws Exception
    {
        Set<Object[]> s = TestRunnerSingle.setUpTest(testControllerProvider);
        return s.iterator();
    }

    @UseAsTestName(idx = 1)
    @Test(dataProvider = "MainTest")
    public void testWorker(TestItem testItem, String testName) throws Exception
    {
        controller.runTests(testItem);
    }
}
