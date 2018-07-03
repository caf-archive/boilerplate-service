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
package com.hpe.caf.worker.boilerplate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Multimap;
import com.hpe.caf.worker.boilerplateshared.response.BoilerplateMatch;
import com.hpe.caf.worker.boilerplateshared.response.SignatureExtractStatus;
import com.hpe.caf.worker.testing.ContentFileTestExpectation;

import java.util.Collection;

/**
 * Created by Michael.McAlynn on 13/01/2016.
 */
public class BoilerplateResultForTest {
    private Collection<ContentFileTestExpectation> data;
    private Collection<BoilerplateMatch> matches;

    // Use json include property as signature extract status will exist only in signature extraction mode results
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private SignatureExtractStatus signatureExtractStatus;

    //Uses json include property to avoid the older library failing to deserialization due to the unknown property.
    /**
     * A map that identifies segments of content extracted by Boilerplate Worker to specific key names.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Multimap<String,ContentFileTestExpectation> groupedMatches;

    public BoilerplateResultForTest(){}

    public SignatureExtractStatus getSignatureExtractStatus(){return this.signatureExtractStatus;}
    public void setSignatureExtractStatus(SignatureExtractStatus signatureExtractStatus) { this.signatureExtractStatus = signatureExtractStatus;}

    public Collection<ContentFileTestExpectation> getData(){return this.data;}
    public void setData(Collection<ContentFileTestExpectation> data) { this.data = data;}

    public Collection<BoilerplateMatch> getMatches() {return this.matches;}
    public void setMatches(Collection<BoilerplateMatch> matches){this.matches = matches;}

    public Multimap<String, ContentFileTestExpectation> getGroupedMatches() {
        return groupedMatches;
    }

    public void setGroupedMatches(Multimap<String, ContentFileTestExpectation> groupedMatches) {
        this.groupedMatches = groupedMatches;
    }
}
