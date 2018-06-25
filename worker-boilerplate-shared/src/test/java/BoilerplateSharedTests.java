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
import com.hpe.caf.api.Codec;
import com.hpe.caf.api.CodecException;
import com.hpe.caf.codec.JsonCodec;
import com.hpe.caf.worker.boilerplateshared.*;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Created by gibsodom on 13/01/2016.
 */
public class BoilerplateSharedTests {

    Codec codec = new JsonCodec();

    @Test
    public void testSelectedItemsSerialization() throws CodecException {
        SelectedExpressions expression = new SelectedExpressions();
        expression.setExpressionIds(new ArrayList<>());
        expression.getExpressionIds().add(20L);
        SelectedItems tag = new SelectedTag();
        ((SelectedTag) tag).setTagId(10L);
        BoilerplateWorkerTask task = new BoilerplateWorkerTask();
        task.setExpressions(expression);
        BoilerplateWorkerTask deserializedTask = serializeAndThenDeserializeTask(task);
        compareExpressions(expression, (SelectedExpressions) deserializedTask.getExpressions());
        task = new BoilerplateWorkerTask();
        task.setExpressions(tag);
        deserializedTask = serializeAndThenDeserializeTask(task);
        compareTags((SelectedTag) tag, (SelectedTag) deserializedTask.getExpressions());

        //Now check how it deserializes with expressions as null
        task = new BoilerplateWorkerTask();
        task.setRedactionType(RedactionType.DO_NOTHING);
        task.setTenantId("Default");
        deserializedTask = serializeAndThenDeserializeTask(task);
        Assert.assertNull(deserializedTask.getExpressions());

        //Check SelectedEmail correctly deserializes
        task = new BoilerplateWorkerTask();
        SelectedEmail selectedEmail = new SelectedEmail();
        selectedEmail.primaryContent = "1";
        selectedEmail.secondaryContent = "LAST";
        selectedEmail.tertiaryContent = "3";
        task.setExpressions(selectedEmail);
        deserializedTask = serializeAndThenDeserializeTask(task);
        compareEmails(selectedEmail, (SelectedEmail) deserializedTask.getExpressions());

        //Now check if null values on selected properly deserialize
        task = new BoilerplateWorkerTask();
        selectedEmail = new SelectedEmail();
        selectedEmail.primaryContent = "1";
        selectedEmail.secondaryContent = "LAST";
        task.setExpressions(selectedEmail);
        deserializedTask = serializeAndThenDeserializeTask(task);
        compareEmails(selectedEmail, (SelectedEmail) deserializedTask.getExpressions());

        //Now check if SelectedEmail deserializes correctly if all fields are null
        task = new BoilerplateWorkerTask();
        selectedEmail = new SelectedEmail();
        task.setExpressions(selectedEmail);
        try {
            deserializedTask = serializeAndThenDeserializeTask(task);
        } catch (CodecException e) {
            Assert.assertEquals("Correct exception thrown", "Failed to deserialize SelectedItem", e.getCause().getCause().getMessage());
        }

        //Check selectedEmailSignature deserializes correctly.
        task = new BoilerplateWorkerTask();
        SelectedEmailSignature selectedEmailSignature = new SelectedEmailSignature();
        selectedEmailSignature.sender = "john.doe@example.com";
        task.setExpressions(selectedEmailSignature);
        deserializedTask = serializeAndThenDeserializeTask(task);
        compareSignatures(selectedEmailSignature, (SelectedEmailSignature) deserializedTask.getExpressions());

        //Now check if deserializes correctly when SelectedEmailSignature fields are null
        task = new BoilerplateWorkerTask();
        selectedEmailSignature = new SelectedEmailSignature();
        task.setExpressions(selectedEmailSignature);
        deserializedTask = serializeAndThenDeserializeTask(task);
        Assert.assertNull(((SelectedEmailSignature) deserializedTask.getExpressions()).sender);

    }

    private BoilerplateWorkerTask serializeAndThenDeserializeTask(BoilerplateWorkerTask task) throws CodecException {
        byte[] serializedTask = codec.serialise(task);
        return codec.deserialise(serializedTask, BoilerplateWorkerTask.class);
    }

    private void compareExpressions(SelectedExpressions expected, SelectedExpressions actual) {
        if (expected.getExpressionIds() == null && actual.getExpressionIds() == null) {
            return;
        }
        Assert.assertEquals("Size of Ids should match", expected.getExpressionIds().size(), actual.getExpressionIds().size());
        Assert.assertEquals("Ids should match", expected.getExpressionIds(), actual.getExpressionIds());
    }

    private void compareTags(SelectedTag expected, SelectedTag actual) {
        Assert.assertEquals("ids should match", expected.getTagId(), actual.getTagId());
    }

    private void compareEmails(SelectedEmail expected, SelectedEmail actual) {
        if (expected == null && actual == null) {
            return;
        }
        Assert.assertEquals("primaryContent should match", expected.primaryContent, actual.primaryContent);
        Assert.assertEquals("secondaryContent should match", expected.secondaryContent, actual.secondaryContent);
        Assert.assertEquals("tertiaryContent should match", expected.tertiaryContent, actual.tertiaryContent);
    }

    private void compareSignatures(SelectedEmailSignature expected, SelectedEmailSignature actual) {
        if (expected == null && actual == null) {
            return;
        }
        Assert.assertEquals("Sender should match", expected.sender, actual.sender);
    }
}
