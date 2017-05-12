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
package com.hpe.caf.worker.boilerplateshared.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Multimap;
import com.hpe.caf.util.ref.ReferencedData;

import java.util.Collection;

/**
 */
public class BoilerplateResult {
    private Collection<ReferencedData> data;
    private Collection<BoilerplateMatch> matches;

    //Uses json include property to avoid the older library failing to deserialization due to the unknown property.
    /**
     * A map that identifies segments of content extracted by Boilerplate Worker to specific key names.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Multimap<String, ReferencedData> groupedMatches;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private SignatureExtractStatus signatureExtractStatus;


    public BoilerplateResult() {

    }

    public Collection<ReferencedData> getData() {
        return data;
    }

    public void setData(Collection<ReferencedData> data) {
        this.data = data;
    }

    public Collection<BoilerplateMatch> getMatches() {
        return matches;
    }

    public void setMatches(Collection<BoilerplateMatch> matches) {
        this.matches = matches;
    }

    public Multimap<String, ReferencedData> getGroupedMatches() {
        return groupedMatches;
    }

    public void setGroupedMatches(Multimap<String, ReferencedData> groupedMatches) {
        this.groupedMatches = groupedMatches;
    }

    public SignatureExtractStatus getSignatureExtractStatus() {
        return signatureExtractStatus;
    }

    public void setSignatureExtractStatus(SignatureExtractStatus signatureExtractStatus) {
        this.signatureExtractStatus = signatureExtractStatus;
    }
}
