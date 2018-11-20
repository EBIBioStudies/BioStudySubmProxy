/*
 * Copyright (c) 2017 European Molecular Biology Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or impl
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.ebi.biostudies.submissiontool.rest.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

import static uk.ac.ebi.biostudies.submissiontool.rest.data.Json.objectMapper;
import static uk.ac.ebi.biostudies.submissiontool.rest.data.PageTabUtils.*;

/**
 * @author olkin
 */
public class PendingSubmission {

    private static final String ACCNO_PREFIX = "TMP_";

    @JsonProperty("accno")
    private String accession;

    @JsonProperty("changed")
    private long changed;

    @JsonProperty("data")
    private JsonNode data;

    @SuppressWarnings("unused")
    public PendingSubmission() {
    }

    public PendingSubmission(String accession, long changed, JsonNode data) {
        this.accession = accession;
        this.changed = changed;
        this.data = data;
    }

    private static String accession(String accno) {
        return accno.matches("[\\w\\-]+") ? accno : generateAccession();
    }

    private static String generateAccession() {
        return ACCNO_PREFIX + System.currentTimeMillis();
    }

    public static PendingSubmission wrap(String json) throws IOException {
        JsonNode subm = objectMapper().readTree(json);
        return new PendingSubmission(accession(accnoField(subm)), System.currentTimeMillis(), subm);
    }

    public JsonNode json() {
        return objectMapper().valueToTree(this);
    }

    public String getAccno() {
        return accession;
    }

    public static PendingSubmission parse(String resp) throws IOException {
        return objectMapper().readValue(resp, PendingSubmission.class);
    }

    public JsonNode getData() {
        return data;
    }

    public boolean isNew() {
        return accession.startsWith(ACCNO_PREFIX);
    }
}
