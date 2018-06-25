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
package com.hpe.caf.worker.boilerplateshared;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by gibsodom on 13/01/2016.
 */
public class SelectedItemDeserializer extends JsonDeserializer<SelectedItems> {
    @Override
    public SelectedItems deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        if (node.get("tagId") != null) {
            SelectedTag tag = new SelectedTag();
            tag.setTagId(node.get("tagId").asLong());
            return tag;
        } else if (node.get("expressionIds") != null) {
            SelectedExpressions expressions = new SelectedExpressions();
            expressions.setExpressionIds(new ArrayList<>());
            if (node.get("expressionIds").isArray()) {
                ArrayNode arrayNode = (ArrayNode) node.get("expressionIds");
                Collection<Long> idList = expressions.getExpressionIds();
                arrayNode.forEach(e -> idList.add(e.asLong()));
                return expressions;
            } else {
                throw new RuntimeException("Failed to deserialize SelectedItem");
            }
        }
        if(node.get("sender") != null){
            SelectedEmailSignature signature = new SelectedEmailSignature();
            if(node.get("sender").isNull()) {
                signature.sender = null;
            } else {
                signature.sender = node.get("sender").asText();
            }
            return signature;
        } else {
            JsonNode primary = node.get("primaryContent");
            JsonNode secondary = node.get("secondaryContent");
            JsonNode tertiary = node.get("tertiaryContent");
            if ((primary != null) || (secondary != null) || (tertiary != null)) {
                SelectedEmail email = new SelectedEmail();
                email.primaryContent = primary != null ? primary.asText() : null;
                email.secondaryContent = secondary != null ? secondary.asText() : null;
                email.tertiaryContent = tertiary != null ? tertiary.asText() : null;
                return email;
            } else {
                throw new RuntimeException("Failed to deserialize SelectedItem");
            }
        }
    }
}
