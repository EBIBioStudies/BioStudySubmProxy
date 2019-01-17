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
package uk.ac.ebi.biostudies.submissiontool.rest.resources;

import static uk.ac.ebi.biostudies.submissiontool.rest.data.Json.objectMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.exceptions.Exceptions;
import uk.ac.ebi.biostudies.submissiontool.bsclient.BioStudiesClient;
import uk.ac.ebi.biostudies.submissiontool.europepmc.EuropePMCClient;
import uk.ac.ebi.biostudies.submissiontool.rest.data.PendingSubmission;
import uk.ac.ebi.biostudies.submissiontool.rest.data.UserSession;
import uk.ac.ebi.biostudies.submissiontool.rest.resources.params.EmailPathCaptchaParams;
import uk.ac.ebi.biostudies.submissiontool.rest.resources.params.SignUpParams;

public class SubmissionService {

    private static final Logger logger = LoggerFactory.getLogger(SubmissionService.class);

    private final BioStudiesClient bsclient;
    private final EuropePMCClient europePmc;

    private static final Map<String, String> europePmcAttributes = new HashMap<String, String>() {
        {
            put("title", "title");
            put("authorString", "authors");
            put("pubType", "type");
            put("issue", "issue");
            put("journalIssn", "issn");
            put("pubYear", "year");
            put("journalVolume", "volume");
        }
    };

    public SubmissionService(BioStudiesClient bsclient) {
        this.bsclient = bsclient;
        this.europePmc = new EuropePMCClient();
    }

    public Observable<String> getOriginalSubmissionRx(String accno, UserSession session) {
        return getSubmissionRx(accno, session)
                .map(resp -> {
                    try {
                        return PendingSubmission.wrap(resp).json().toString();
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                });
    }

    public Observable<String> getPendingSubmissionRx(String accno, UserSession session) {
        return bsclient.getPendingSubmissionRx(accno, session.id());
    }

    public Observable<String> getSubmissionRx(String accno, UserSession session) {
        return bsclient.getSubmissionRx(accno, session.id());
    }

    public Observable<String> createPendingSubmissionRx(String pageTab, UserSession session) {
        return bsclient.createPendingSubmissionRx(pageTab, session.id());
    }

    public Observable<String> savePendingSubmissionRx(String pageTab, String accno, UserSession session) {
        return bsclient.getPendingSubmissionRx(accno, session.id())
                .onErrorResumeNext(
                        bsclient.createPendingSubmissionRx(pageTab, session.id())
                ).switchMap(s -> bsclient.savePendingSubmissionRx(pageTab, accno, session.id()));
    }

    public Observable<String> submitPendingSubmissionRx(String pageTab, String accno, UserSession session) {
        return bsclient.submitPendingSubmissionRx(pageTab, accno, session.id());
    }

    public Observable<String> directSubmitRx(boolean create, String pageTab, UserSession session) {
        return bsclient.directSubmitRx(create, pageTab, session.id());
    }

    public Observable<Boolean> deleteSubmissionRx(String accno, UserSession session) {
        return getPendingSubmissionRx(accno, session)
                .onErrorResumeNext(Observable.just(""))
                .flatMap(resp -> resp.isEmpty() ?
                        deleteOriginalRx(accno, session) : deletePendingRx(accno, session)
                );
    }

    private Observable<Boolean> deleteOriginalRx(String accno, UserSession session) {
        return bsclient.deleteSubmissionRx(accno, session.id())
                .map(resp -> {
                    try {
                        JsonNode json = objectMapper().readTree(resp);
                        String level = json.get("level").asText();
                        return level.equalsIgnoreCase("success");

                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                });
    }

    private Observable<Boolean> deletePendingRx(String accno, UserSession session) {
        return bsclient.deletePendingSubmissionRx(accno, session.id()).map(resp -> true);
    }

    public Observable<String> signOutRx(UserSession session) {
        ObjectNode obj = JsonNodeFactory.instance.objectNode();
        obj.put("sessid", session.id());
        return bsclient.signOutRx(obj.toString(), session.id());
    }

    public Observable<String> signUpRx(SignUpParams params) throws JsonProcessingException {
        return bsclient.signUpRx(objectMapper().writerWithView(SignUpParams.class).writeValueAsString(params));
    }

    public Observable<String> passwordResetRequestRx(EmailPathCaptchaParams params) throws JsonProcessingException {
        return bsclient.passwordResetRequestRx(objectMapper()
                .writerWithView(EmailPathCaptchaParams.PasswordResetView.class).writeValueAsString(params));
    }

    public Observable<String> resendActivationLinkRx(EmailPathCaptchaParams params) throws JsonProcessingException {
        return bsclient.resendActivationLinkRx(objectMapper()
                .writerWithView(EmailPathCaptchaParams.ActivationLinkView.class).writeValueAsString(params));
    }

    public Observable<String> pubMedSearchRx(String id) {
        logger.debug("pubMedSearch(ID={})", id);
        return europePmc
                .pubMedSearchRx(id)
                .map(resp -> {
                    try {
                        JsonNode respNode = objectMapper().readTree(resp);
                        int hitCount = respNode.path("hitCount").asInt();
                        ObjectNode data = JsonNodeFactory.instance.objectNode();

                        if (hitCount >= 1) {
                            final JsonNode publ = respNode.path("resultList").path("result").get(0);
                            europePmcAttributes.forEach((key, value) -> {
                                if (publ.has(key)) {
                                    data.put(value, publ.get(key).asText(""));
                                }
                            });
                        }
                        return data.toString();
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                });
    }
}
