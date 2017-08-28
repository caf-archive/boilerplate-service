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
package com.github.cafdataprocessing.boilerplate.result;

import com.google.common.collect.Multimap;
import com.hpe.caf.policyworker.policyboilerplatefields.PolicyBoilerplateFields;
import com.hpe.caf.util.ref.ReferencedData;
import com.github.cafdataprocessing.boilerplate.BoilerplateWorkerConstants;
import com.hpe.caf.worker.document.model.Document;

import java.util.Collection;
import java.util.Map;

/**
 * Updates data with the result returned from boilerplate worker.
 */
public class ResultTranslator
{
    public static void translate(Document document, BoilerplateWorkerResponse boilerplateWorkerResult)
    {
        Map<String, BoilerplateResult> boilerplateResults = boilerplateWorkerResult.getTaskResults();
        for (String fieldName : boilerplateResults.keySet()) {
            BoilerplateResult boilerplateResult = boilerplateResults.get(fieldName);

            //if there were no boilerplate matches for the field then no match fields
            Collection<BoilerplateMatch> boilerplateMatches = boilerplateResult.getMatches();
            //add match fields onto document
            updateWithMatchFields(document, boilerplateMatches);
            //replace old fields with modified field content
            updateWithModifiedFields(document, fieldName, boilerplateResult.getData());
            //Add any groupedMatches data, such as email key content or signatures.
            updateWithEmailKeyContent(document, boilerplateResult.getGroupedMatches());

            updateWithEmailSignatureExtractionStatus(document, boilerplateResult);

            updateWithEmailSignatures(document, boilerplateResult);
        }
    }

    /*
        Add the boilerplate matches onto the document as fields
     */
    private static void updateWithMatchFields(final Document document, final Collection<BoilerplateMatch> boilerplateMatches)
    {
        if (boilerplateMatches == null || boilerplateMatches.isEmpty()) {
            return;
        }
        for (BoilerplateMatch boilerplateMatch : boilerplateMatches) {
            String boilerplateId = boilerplateMatch.getBoilerplateId().toString();
            document.getField(BoilerplateFields.BOILERPLATE_MATCH_ID).add(boilerplateId);
            document.getField(BoilerplateFields.getMatchValueFieldName(boilerplateId)).add(boilerplateMatch.getValue());
        }
    }

    private static void updateWithModifiedFields(Document document, String fieldName,
                                                 Collection<ReferencedData> modifiedDataCollection)
    {
        if (modifiedDataCollection == null || modifiedDataCollection.isEmpty()) {
            return;
        }
        // remove old field(s) with unmodified value (note that we are not cleaning up any caf storage
        // references at the moment and will need to in the future when deletion is supported)
        document.getField(fieldName).clear();

        for (ReferencedData modifiedData : modifiedDataCollection) {
            if (modifiedData.getReference() != null) {
                document.getField(fieldName).addReference(modifiedData.getReference());
            }
            if (modifiedData.getData() != null) {
                document.getField(fieldName).add(modifiedData.getData());
            }
        }
    }

    private static void updateWithEmailKeyContent(Document document, Multimap<String, ReferencedData> groupedMatches)
    {
        if (groupedMatches == null || groupedMatches.isEmpty()) {
            return;
        }
        //Add primary email content
        String fieldName = document.getField(PolicyBoilerplateFields.POLICY_BOILERPLATE_PRIMARY_FIELDNAME).getStringValues().stream().findFirst().orElse(null);
        Collection<ReferencedData> emailContent = groupedMatches.get(BoilerplateWorkerConstants.PRIMARY_CONTENT);
        for (ReferencedData content : emailContent) {
            if (content.getData() != null) {
                document.getField(fieldName == null ? BoilerplateWorkerConstants.PRIMARY_CONTENT : fieldName).add(content.getData());
            }
            if (content.getReference() != null) {
                document.getField(fieldName == null ? BoilerplateWorkerConstants.PRIMARY_CONTENT : fieldName).addReference(content.getReference());
            }
        }

        //Add secondary email content
        fieldName = document.getField(PolicyBoilerplateFields.POLICY_BOILERPLATE_SECONDARY_FIELDNAME).getStringValues().stream().findFirst().orElse(null);
        emailContent = groupedMatches.get(BoilerplateWorkerConstants.SECONDARY_CONTENT);
        for (ReferencedData content : emailContent) {
            if (content.getData() != null) {
                document.getField(fieldName == null ? BoilerplateWorkerConstants.SECONDARY_CONTENT : fieldName).add(content.getData());
            }
            if (content.getReference() != null) {
                document.getField(fieldName == null ? BoilerplateWorkerConstants.SECONDARY_CONTENT : fieldName).addReference(content.getReference());
            }
        }

        //Add tertiary email content
        fieldName = document.getField(PolicyBoilerplateFields.POLICY_BOILERPLATE_TERTIARY_FIELDNAME).getStringValues().stream().findFirst().orElse(null);
        emailContent = groupedMatches.get(BoilerplateWorkerConstants.TERTIARY_CONTENT);
        for (ReferencedData content : emailContent) {
            if (content.getData() != null) {
                document.getField(fieldName == null ? BoilerplateWorkerConstants.TERTIARY_CONTENT : fieldName).add(content.getData());
            }
            if (content.getReference() != null) {
                document.getField(fieldName == null ? BoilerplateWorkerConstants.TERTIARY_CONTENT : fieldName).addReference(content.getReference());
            }
        }

        //Remote temp fields from document
        document.getField(PolicyBoilerplateFields.POLICY_BOILERPLATE_PRIMARY_FIELDNAME).clear();
        document.getField(PolicyBoilerplateFields.POLICY_BOILERPLATE_SECONDARY_FIELDNAME).clear();
        document.getField(PolicyBoilerplateFields.POLICY_BOILERPLATE_TERTIARY_FIELDNAME).clear();
    }

    private static void updateWithEmailSignatureExtractionStatus(Document document, BoilerplateResult boilerplateResult)
    {

        SignatureExtractStatus signatureExtractStatus = boilerplateResult.getSignatureExtractStatus();

        // If a signature extraction status exists add the status to the document's metadata
        if (signatureExtractStatus == null) {
        } else {
            document.getField(BoilerplateWorkerConstants.SIGNATURE_EXTRACTION_STATUS).add(signatureExtractStatus.toString());
        }
    }

    private static void updateWithEmailSignatures(Document document, BoilerplateResult boilerplateResult)
    {
        String fieldName = document.getField(PolicyBoilerplateFields.POLICY_BOILERPLATE_EMAIL_SIGNATURES_FIELDNAME)
            .getStringValues().stream().findFirst().orElse(BoilerplateWorkerConstants.EXTRACTED_SIGNATURES);

        // Remove temp field from document
        document.getField(PolicyBoilerplateFields.POLICY_BOILERPLATE_EMAIL_SIGNATURES_FIELDNAME).clear();

        Multimap<String, ReferencedData> groupedMatches = boilerplateResult.getGroupedMatches();
        // If there are no grouped matches remove metadata from the temp field and return
        if (groupedMatches == null || groupedMatches.isEmpty()) {
            return;
        }

        Collection<ReferencedData> extractedEmailSignatures = groupedMatches.get(BoilerplateWorkerConstants.EXTRACTED_SIGNATURES);

        SignatureExtractStatus signatureExtractStatus = boilerplateResult.getSignatureExtractStatus();

        // If there are extracted signatures add each of them as metadata references to the document
        if (extractedEmailSignatures.size() > 0 && signatureExtractStatus.equals(SignatureExtractStatus.SIGNATURES_EXTRACTED)) {
            for (ReferencedData content : extractedEmailSignatures) {
                if (content.getData() != null) {
                    document.getField(fieldName).add(content.getData());
                }
                if (content.getReference() != null) {
                    document.getField(fieldName).addReference(content.getReference());
                }
            }
        }
    }
}
