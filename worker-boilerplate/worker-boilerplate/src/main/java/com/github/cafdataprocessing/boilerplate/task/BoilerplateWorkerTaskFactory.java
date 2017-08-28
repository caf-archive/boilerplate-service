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
package com.github.cafdataprocessing.boilerplate.task;

import com.github.cafdataprocessing.boilerplate.utils.RedactionType;
import com.github.cafdataprocessing.boilerplate.utils.SelectedEmail;
import com.github.cafdataprocessing.boilerplate.utils.SelectedEmailSignature;
import com.github.cafdataprocessing.boilerplate.utils.SelectedExpressions;
import com.github.cafdataprocessing.boilerplate.utils.SelectedItems;
import com.github.cafdataprocessing.boilerplate.utils.SelectedTag;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.hpe.caf.policyworker.policyboilerplatefields.PolicyBoilerplateFields;
import com.hpe.caf.util.ref.ReferencedData;
import com.hpe.caf.worker.document.model.Document;
import com.hpe.caf.worker.document.model.Field;
import com.hpe.caf.worker.document.model.FieldValue;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author mcgreeva
 */
public class BoilerplateWorkerTaskFactory
{
    private BoilerplateWorkerTaskFactory()
    {
    }

    public static BoilerplateWorkerTask getTask(final Document document)
    {
        BoilerplateWorkerTask task = new BoilerplateWorkerTask();

        task.setDataStorePartialReference(document.getCustomData("outputPartialReference"));
        task.setExpressions(getExpressions(document));
        task.setRedactionType(getRedactionType(document.getCustomData("redactionType")));
        task.setReturnMatches(Boolean.parseBoolean(document.getCustomData("returnMatches")));
        task.setSourceData(getSourceData(document));
        task.setTenantId(document.getCustomData("tennatId"));

        return task;
    }

    private static RedactionType getRedactionType(final String type)
    {
        if (!Strings.isNullOrEmpty(type)) {
            switch (type) {
                case "DO_NOTHING": {
                    return RedactionType.DO_NOTHING;
                }
                case "REMOVE": {
                    return RedactionType.REMOVE;
                }
                case "REPLACE": {
                    return RedactionType.REPLACE;
                }
                default:
                    return RedactionType.DO_NOTHING;
            }
        }
        return null;
    }

    private static Multimap<String, ReferencedData> getSourceData(final Document document)
    {
        final Gson gson = new Gson();
        final Multimap<String, ReferencedData> sourceData = ArrayListMultimap.create();
        final String fieldToUse = document.getCustomData("fields");
        if (!Strings.isNullOrEmpty(fieldToUse)) {
            final List<String> sourceDataFields = gson.fromJson(fieldToUse, List.class);
            for (Field field : document.getFields()) {
                for (FieldValue value : field.getValues()) {
                    if (value.isReference() && sourceDataFields.contains(field.getName())) {
                        sourceData.put(field.getName(), ReferencedData.getReferencedData(value.getReference()));
                    } else if (value.isStringValue() && sourceDataFields.contains(field.getName())) {
                        sourceData.put(field.getName(), ReferencedData.getWrappedData(value.getValue()));
                    }
                }
            }
        } else {
            for (Field field : document.getFields()) {
                for (FieldValue value : field.getValues()) {
                    if (value.isReference()) {
                        sourceData.put(field.getName(), ReferencedData.getReferencedData(value.getReference()));
                    } else if (value.isStringValue()) {
                        sourceData.put(field.getName(), ReferencedData.getWrappedData(value.getValue()));
                    }
                }
            }
        }
        return sourceData;
    }

    private static SelectedItems getExpressions(final Document document)
    {
        final Long tagId = getTagId(document);
        final Set<Long> expressionIds = getExpressionIds(document);
        final EmailSignatureDetection emailSignatureDetection = getEmailSignatureDetection(document);
        final EmailSegregationRules emailSegregationRules = getEmailSegregationRules(document);
        SelectedItems selectedItems = null;
        if (expressionIds != null) {
            SelectedExpressions expression = new SelectedExpressions();
            expression.setExpressionIds(expressionIds);
            selectedItems = expression;
        } else if (tagId != null) {
            SelectedTag tag = new SelectedTag();
            tag.setTagId(tagId);
            selectedItems = tag;
        } else if (emailSegregationRules != null) {
            SelectedEmail selectedEmail = new SelectedEmail();
            selectedEmail.primaryContent = emailSegregationRules.primaryExpression;
            selectedEmail.secondaryContent = emailSegregationRules.secondaryExpression;
            selectedEmail.tertiaryContent = emailSegregationRules.tertiaryExpression;
            if (emailSegregationRules.primaryFieldName != null) {
                document.getField(PolicyBoilerplateFields.POLICY_BOILERPLATE_PRIMARY_FIELDNAME).add(emailSegregationRules.primaryFieldName);
            }
            if (emailSegregationRules.secondaryFieldName != null) {
                document.getField(PolicyBoilerplateFields.POLICY_BOILERPLATE_SECONDARY_FIELDNAME).add(emailSegregationRules.secondaryFieldName);
            }
            if (emailSegregationRules.tertiaryFieldName != null) {
                document.getField(PolicyBoilerplateFields.POLICY_BOILERPLATE_TERTIARY_FIELDNAME).add(emailSegregationRules.tertiaryFieldName);
            }
            selectedItems = selectedEmail;
        } else if (emailSignatureDetection != null) {
            SelectedEmailSignature selectedEmailSignature = new SelectedEmailSignature();
            if (emailSignatureDetection.extractedEmailSignaturesFieldName != null) {
                document.getField(PolicyBoilerplateFields.POLICY_BOILERPLATE_EMAIL_SIGNATURES_FIELDNAME).add(emailSignatureDetection.extractedEmailSignaturesFieldName);
            }
            if (emailSignatureDetection.sender != null) {
                selectedEmailSignature.sender = emailSignatureDetection.sender;
            } else {
                selectedEmailSignature.sender = "";
            }
            selectedItems = selectedEmailSignature;
        } else {
        }
        return selectedItems;
    }

    private static Set<Long> getExpressionIds(final Document document)
    {
        final Gson gson = new Gson();
        final String expresionIdsJson = document.getCustomData("expresionIds");
        if (!Strings.isNullOrEmpty(expresionIdsJson)) {
            Set<String> list = gson.fromJson(expresionIdsJson, Set.class);
            Set<Long> listSet = new HashSet<>();
            list.forEach((id) -> {
                listSet.add(Long.parseLong(id));
            });
            return listSet;
            
        }
        return null;
    }

    private static EmailSignatureDetection getEmailSignatureDetection(final Document document)
    {
        final Gson gson = new Gson();
        final String emailSignatureDetectionJson = document.getCustomData("emailSignature");
        if (!Strings.isNullOrEmpty(emailSignatureDetectionJson)) {
            return gson.fromJson(emailSignatureDetectionJson, EmailSignatureDetection.class);
        }
        return null;
    }

    private static EmailSegregationRules getEmailSegregationRules(final Document document)
    {
        final Gson gson = new Gson();
        final String emailSegregationRules = document.getCustomData("emailSegregation");
        if (!Strings.isNullOrEmpty(emailSegregationRules)) {
            return gson.fromJson(emailSegregationRules, EmailSegregationRules.class);
        }
        return null;
    }

    private static Long getTagId(final Document document)
    {
        String id = document.getCustomData("tagId");
        if (!Strings.isNullOrEmpty(id)) {
            return Long.parseLong(document.getCustomData("tagId"));
        }
        return null;
    }
}
