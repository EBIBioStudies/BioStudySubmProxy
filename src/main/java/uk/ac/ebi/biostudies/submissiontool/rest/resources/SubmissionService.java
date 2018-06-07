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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.exceptions.Exceptions;
import uk.ac.ebi.biostudies.submissiontool.bsclient.BioStudiesClient;
import uk.ac.ebi.biostudies.submissiontool.europepmc.EuropePMCClient;
import uk.ac.ebi.biostudies.submissiontool.rest.data.ModifiedSubmission;
import uk.ac.ebi.biostudies.submissiontool.rest.data.PageTabUtils;
import uk.ac.ebi.biostudies.submissiontool.rest.data.UserSession;
import uk.ac.ebi.biostudies.submissiontool.rest.resources.params.SubmissionListFilterParams;
import uk.ac.ebi.biostudies.submissiontool.rest.resources.params.EmailPathCaptchaParams;
import uk.ac.ebi.biostudies.submissiontool.rest.resources.params.KeyPasswordCaptchaParams;
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

    public Observable<String> findSubmissionRx(String accno, UserSession session) {
        logger.debug("getSubmission(session={}, accnoAttr={}, origin={})", session, accno);
        return getPendingSubmissionRx(accno, session)
                .flatMap(resp -> resp.isEmpty() ? getOriginalSubmissionRx(accno, session) : Observable.just(resp))
                .flatMap(resp -> resp.isEmpty() ? Observable.just("{}") : Observable.just(resp));
    }

    public Observable<String> getOriginalSubmissionRx(String accno, UserSession session) {
        return getSubmissionRx(accno, session)
                .map(resp -> {
                    try {
                        return ModifiedSubmission.wrap(resp).json().toString();
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                });
    }

    private Observable<String> getPendingSubmissionRx(String accno, UserSession session) {
        return bsclient.getPendingSubmissionRx(accno, session.id());
    }

    private Observable<String> getSubmissionRx(String accno, UserSession session) {
        return bsclient.getSubmissionRx(accno, session.id());
    }

    public Observable<String> createPendingSubmissionRx(String subm, UserSession session) {
        logger.debug("createSubmission(session={})", session);
        return bsclient.createPendingSubmissionRx(subm, session.id());
    }

    public Observable<String> savePendingSubmissionRx(String subm, String accno, UserSession session) {
        return bsclient.savePendingSubmissionRx(subm, accno, session.id());
    }

    public Observable<String> submitPendingSubmissionRx(String subm, UserSession session) throws IOException {
        logger.debug("submitSubmission(session={}, obj={})", session, subm);
        ModifiedSubmission modified = ModifiedSubmission.parse(subm);
        return submitRx(modified.isNew(), modified.getData(), session)
                .flatMap(resp ->
                        deleteModifiedRx(modified.getAccno(), session)
                                .map(delResp -> resp)
                );
    }

    public Observable<String> submitPlainRx(boolean create, String subm, UserSession session) throws IOException {
        JsonNode node = objectMapper().readTree(subm);
        return submitRx(create, node, session);
    }

    private Observable<String> submitRx(boolean create, JsonNode subm, UserSession session) {
        return create ? submitNewRx(subm, session) : submitExistedRx(subm, session);
    }

    private Observable<String> submitNewRx(JsonNode subm, UserSession session) {
        return accnoTemplateRx(subm, session)
                .flatMap(accnoTmpl -> {
                    ObjectNode copy = subm.deepCopy();
                    copy.put("accno", accnoTmpl);

                    ArrayNode array = JsonNodeFactory.instance.arrayNode();
                    array.add(copy);

                    ObjectNode submissions = JsonNodeFactory.instance.objectNode();
                    submissions.set("submissions", array);
                    return bsclient.submitNewRx(submissions.toString(), session.id());
                });
    }

    private Observable<String> submitExistedRx(JsonNode subm, UserSession session) {
        ArrayNode array = JsonNodeFactory.instance.arrayNode();
        array.add(subm);

        ObjectNode submissions = JsonNodeFactory.instance.objectNode();
        submissions.set("submissions", array);
        return bsclient.submitUpdatedRx(submissions.toString(), session.id());
    }

    private Observable<String> accnoTemplateRx(JsonNode subm, UserSession session) {
        final Observable<String> defaultTmpl = Observable.just("!{S-BSST}");

        List<String> accessions = PageTabUtils.attachToAttr(subm);
        if (accessions.size() != 1) {
            return defaultTmpl;
        }

        String accno = accessions.get(0);
        if (accno == null || accno.isEmpty()) {
            return defaultTmpl;
        }
        return projectAccnoTemplateRx(accno, session)
                .flatMap(tmpl ->
                        tmpl == null || tmpl.isEmpty() ? defaultTmpl : Observable.just(tmpl));
    }

    private Observable<String> projectAccnoTemplateRx(String accno, UserSession session) {
        return getSubmissionRx(accno, session)
                .map(resp -> {
                    try {
                        return PageTabUtils.accnoTemplateAttr(objectMapper().readTree(resp));
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                });
    }

    public Observable<Boolean> deleteSubmissionRx(String accno, UserSession session) {
        logger.debug("deleteSubmission(session={}, acc={})", session, accno);
        return getPendingSubmissionRx(accno, session)
                .flatMap(resp -> resp.isEmpty() ?
                        deleteOriginalRx(accno, session) : deleteModifiedRx(accno, session)
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

    private Observable<Boolean> deleteModifiedRx(String accno, UserSession session) {
        return bsclient.deletePendingSubmissionRx(accno, session.id()).map(resp -> true);
    }

    public Observable<String> getSubmittedSubmissionsRx(SubmissionListFilterParams filterParams, UserSession session) {
        return bsclient.getSubmissionsRx(session.id(), filterParams.asMap());
    }

    public Observable<String> getPendingSubmissionsRx(SubmissionListFilterParams filterParams, UserSession session) {
        return bsclient.getPendingSubmissionsRx(session.id(), filterParams.asMap());
    }

    public Observable<String> getProjectsRx(UserSession session) {
        return bsclient.getProjectsRx(session.id());
    }

    public Observable<String> getFilesRx(String path, int depth, boolean showArchive, UserSession session) {
        logger.debug("getFilesDir(session={}, path={}, depth={}, showArchive={})", session, path, depth, showArchive);
        return bsclient.getFilesDirRx(path, depth, showArchive, session.id());
    }

    public Observable<String> deleteFileRx(String path, UserSession session) {
        logger.debug("deleteFile(session={}, path={})", session, path);
        return bsclient.deleteFileRx(path, session.id());
    }

    public Observable<String> signOutRx(UserSession session) {
        logger.debug("signOut(session={})", session);
        ObjectNode obj = JsonNodeFactory.instance.objectNode();
        obj.put("sessid", session.id());
        return bsclient.signOutRx(obj.toString(), session.id());
    }

    public Observable<String> signUpRx(SignUpParams params) {
        logger.debug("signUp(obj={})", params);
        ObjectNode json = JsonNodeFactory.instance.objectNode();
        json.put("username", params.getUsername());
        json.put("password", params.getPassword());
        json.put("email", params.getEmail());
        json.put("recaptcha2-response", params.getCaptcha());
        json.put("activationURL", params.getPath());
        ArrayNode aux = JsonNodeFactory.instance.arrayNode();
        aux.add("orcid:" + (params.hasOrcid() ? params.getOrcid() : ""));
        json.set("aux", aux);
        return bsclient.signUpRx(json.toString());
    }

    public Observable<String> passwordResetRequestRx(EmailPathCaptchaParams params) {
        logger.debug("passwordResetRequest(obj={})", params);
        ObjectNode json = JsonNodeFactory.instance.objectNode();
        json.put("email", params.getEmail());
        json.put("recaptcha2-response", params.getCaptcha());
        json.put("resetURL", params.getPath());
        return bsclient.passwordResetRequestRx(json.toString());
    }

    public Observable<String> passwordResetRx(KeyPasswordCaptchaParams params) {
        logger.debug("passwordReset(obj={})", params);
        ObjectNode json = JsonNodeFactory.instance.objectNode();
        json.put("key", params.getKey());
        json.put("password", params.getPassword());
        json.put("recaptcha2-response", params.getCaptcha());
        return bsclient.passwordResetRx(json.toString());
    }

    public Observable<String> resendActivationLinkRx(EmailPathCaptchaParams params) {
        logger.debug("resendActivationLink(params={})", params);
        ObjectNode json = JsonNodeFactory.instance.objectNode();
        json.put("email", params.getEmail());
        json.put("recaptcha2-response", params.getCaptcha());
        json.put("activationURL", params.getPath());
        return bsclient.resendActivationLinkRx(json.toString());
    }

    public Observable<String> activateRx(String key) {
        logger.debug("activate(key={})", key);
        return bsclient.activateRx(key);
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
