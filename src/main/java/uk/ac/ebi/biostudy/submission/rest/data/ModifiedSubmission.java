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

package uk.ac.ebi.biostudy.submission.rest.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

import static uk.ac.ebi.biostudy.submission.rest.data.PageTabUtils.accno;
import static uk.ac.ebi.biostudy.submission.rest.data.PageTabUtils.rtimeInSeconds;
import static uk.ac.ebi.biostudy.submission.rest.data.PageTabUtils.title;

/**
 * @author olkin
 */
public class ModifiedSubmission {

    private static final String ACCNO_PREFIX = "TMP_";

    @JsonProperty("accno")
    private String accession;

    @JsonProperty("changed")
    private long changed;

    @JsonProperty("data")
    private JsonNode data;

    @SuppressWarnings("unused")
    public ModifiedSubmission() {
    }

    public ModifiedSubmission(String accession, long changed, JsonNode data) {
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

    public static ModifiedSubmission wrap(String json) throws IOException {
        JsonNode subm = new ObjectMapper().readTree(json);
        return new ModifiedSubmission(accession(accno(subm)), System.currentTimeMillis(), subm);
    }

    public JsonNode json() {
        return new ObjectMapper().valueToTree(this);
    }

    public String getAccno() {
        return accession;
    }

    public ModifiedSubmission update() {
        this.changed = System.currentTimeMillis();
        return this;
    }

    public static ModifiedSubmission parse(String resp) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(resp);
        return mapper.treeToValue(jsonNode, ModifiedSubmission.class);
    }

    public JsonNode getData() {
        return data;
    }

    public boolean isNew() {
        return  accession.startsWith(ACCNO_PREFIX);
    }

    public static ModifiedSubmission convert(JsonNode node) throws JsonProcessingException {
        return new ObjectMapper().treeToValue(node, ModifiedSubmission.class);
    }

    public String getTitle() {
        return title(this.data);
    }

    public Long getRTimeInSeconds() {
        return rtimeInSeconds(this.data);
    }

    public Long getMTimeInSeconds() {
        return changed / 1000;
    }
}
