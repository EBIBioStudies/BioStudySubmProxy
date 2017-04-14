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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.exceptions.Exceptions;
import uk.ac.ebi.biostudies.submissiontool.bsclient.BioStudiesClient;
import uk.ac.ebi.biostudies.submissiontool.bsclient.BioStudiesClientException;
import uk.ac.ebi.biostudies.submissiontool.europepmc.EuropePmcClient;
import uk.ac.ebi.biostudies.submissiontool.rest.data.ModifiedSubmission;
import uk.ac.ebi.biostudies.submissiontool.rest.data.PageTabUtils;
import uk.ac.ebi.biostudies.submissiontool.rest.data.SubmissionListItem;
import uk.ac.ebi.biostudies.submissiontool.rest.data.UserSession;
import uk.ac.ebi.biostudies.submissiontool.rest.resources.params.EmailPathCaptchaParams;
import uk.ac.ebi.biostudies.submissiontool.rest.resources.params.KeyPasswordCaptchaParams;
import uk.ac.ebi.biostudies.submissiontool.rest.resources.params.SignUpParams;
import uk.ac.ebi.biostudies.submissiontool.rest.resources.params.SubmFilterParams;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

import static uk.ac.ebi.biostudies.submissiontool.rest.data.Json.objectMapper;

public class SubmissionService {

    private static final Logger logger = LoggerFactory.getLogger(SubmissionService.class);

    private final BioStudiesClient bsclient;
    private final EuropePmcClient europePmc;

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
        this.europePmc = new EuropePmcClient();
    }

    public Observable<String> getSubmissionRx(String accno, boolean origin, UserSession session) {
        logger.debug("getSubmission(session={}, accnoAttr={}, origin={})", session, accno, origin);
        if (!origin) {
            return getModifiedRx(accno, session)
                    .flatMap(resp -> resp.isEmpty() ? getWrappedOriginalRx(accno, session) : Observable.just(resp));
        }
        return getWrappedOriginalRx(accno, session);
    }

    private Observable<String> getModifiedRx(String accno, UserSession session) {
        return bsclient.getModifiedSubmissionRx(accno, session.id());
    }

    private Observable<String> getOriginalRx(String accno, UserSession session) {
        return bsclient.getSubmissionRx(accno, session.id());
    }

    private Observable<String> getWrappedOriginalRx(String accno, UserSession session) {
        return getOriginalRx(accno, session)
                .map(resp -> {
                    try {
                        return ModifiedSubmission.wrap(resp).json().toString();
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                });
    }
/*
    public ModifiedSubmission getSubmission(final String accno, boolean origin, UserSession session)
            throws BioStudiesClientException, IOException {
        logger.debug("getSubmission(session={}, accnoAttr={}, origin={})", session, accno, origin);
        if (!origin) {
            String resp = bsclient.getModifiedSubmission(accno, session.id());
            if (!resp.isEmpty()) {
                return ModifiedSubmission.parse(resp);
            }
        }
        return ModifiedSubmission.wrap(bsclient.getSubmission(accno, session.id()));
    }
*/

    public ModifiedSubmission createSubmission(String subm, UserSession session) throws IOException, BioStudiesClientException {
        logger.debug("createSubmission(session={})", session);
        ModifiedSubmission modified = ModifiedSubmission.wrap(subm);
        saveSubmission(modified, session);
        return modified;
    }

    public void saveSubmission(String subm, UserSession session) throws IOException, BioStudiesClientException {
        this.saveSubmission(ModifiedSubmission.parse(subm), session);
    }

    private void saveSubmission(ModifiedSubmission subm, UserSession session) throws IOException, BioStudiesClientException {
        logger.debug("saveSubmission(session={}, obj={})", session, subm);
        bsclient.saveModifiedSubmission(subm.update().json().toString(), subm.getAccno(), session.id());
    }

   /* public String editSubmission(String accno, UserSession session)
            throws BioStudiesClientException, IOException {
        logger.debug("editSubmission(session={}, accnoAttr={})", session, accno);
        String sbm = bsclient.getModifiedSubmission(accno, session.id());
        if (sbm.isEmpty()) {
            logger.debug("no temporary copy; creating one...");
            ModifiedSubmission modified = getSubmission(accno, true, session);
            saveSubmission(modified, session);
            return modified.json().toString();
        }
        return sbm;
    }*/

    public Observable<String> submitModifiedRx(String subm, UserSession session) throws IOException {
        logger.debug("submitSubmission(session={}, obj={})", session, subm);
        ModifiedSubmission modified = ModifiedSubmission.parse(subm);
        return submitRx(modified.isNew(), modified.getData(), session)
                .flatMap(resp -> {
                    try {
                        JsonNode resultNode = objectMapper().readTree(resp);
                        String status = resultNode.get("status").asText();
                        if (status.equals("OK")) {
                            return deleteModifiedRx(modified.getAccno(), session)
                                    .map(delResp -> resp);
                        }
                        return Observable.just(resp);
                    } catch (IOException e) {
                        return Observable.error(e);
                    }
                });
    }

    /*public String submitModified(String subm, UserSession session) throws IOException, BioStudiesClientException {
        logger.debug("submitSubmission(session={}, obj={})", session, subm);
        ModifiedSubmission modified = ModifiedSubmission.parse(subm);
        String result = submit(modified.isNew(), modified.getData(), session);

        //TODO: better way to read BioStudies API response
        JsonNode resultNode = objectMapper().readTree(result);
        String status = resultNode.get("status").asText();
        if (status.equals("OK")) {
            deleteSubmission(modified.getAccno(), session);
        }
        return result;
    }*/

    public Observable<String> submitPlainRx(boolean create, String subm, UserSession session) throws IOException {
        JsonNode node = objectMapper().readTree(subm);
        return submitRx(create, node, session);
    }

   /* public String submitPlain(boolean create, String subm, UserSession session) throws IOException, BioStudiesClientException {
        JsonNode node = objectMapper().readTree(subm);
        return submit(create, node, session);
    }*/

    private Observable<String> submitRx(boolean create, JsonNode subm, UserSession session) {
        return create ? submitNewRx(subm, session) : submitExistedRx(subm, session);
    }

    /*private String submit(boolean create, JsonNode subm, UserSession session) throws IOException, BioStudiesClientException {
        return create ? submitNew(subm, session) : submitExisted(subm, session);
    }*/

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

/*
    private String submitNew(JsonNode subm, UserSession session) throws IOException, BioStudiesClientException {
        ObjectNode copy = subm.deepCopy();
        copy.put("accno", accnoTemplate(copy, session));

        ArrayNode array = JsonNodeFactory.instance.arrayNode();
        array.add(copy);

        ObjectNode submissions = JsonNodeFactory.instance.objectNode();
        submissions.set("submissions", array);

        return bsclient.submitNew(submissions.toString(), session.id());
    }*/

    private String submitExisted(JsonNode subm, UserSession session) throws IOException, BioStudiesClientException {
        ArrayNode array = JsonNodeFactory.instance.arrayNode();
        array.add(subm);

        ObjectNode submissions = JsonNodeFactory.instance.objectNode();
        submissions.set("submissions", array);
        return bsclient.submitUpdated(submissions.toString(), session.id());
    }

    /*private String accnoTemplate(JsonNode subm, UserSession session) throws IOException, BioStudiesClientException {
        final String defaultTmpl = "!{S-BSST}";

        List<String> accessions = PageTabUtils.attachToAttr(subm);
        if (accessions.size() != 1) {
            return defaultTmpl;
        }

        String accno = accessions.get(0);
        if (accno == null || accno.isEmpty()) {
            return defaultTmpl;
        }
        String accnoTmpl = projectAccnoTemplate(accno, session);
        return accnoTmpl == null || accno.isEmpty() ? defaultTmpl : accnoTmpl;
    }


    private String projectAccnoTemplate(String accno, UserSession session) throws IOException, BioStudiesClientException {
        ModifiedSubmission proj = getSubmission(accno, true, session);
        return PageTabUtils.accnoTemplateAttr(proj.getData());
    }*/

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
        return getOriginalRx(accno, session)
                .map(resp -> {
                    try {
                        return PageTabUtils.accnoTemplateAttr(objectMapper().readTree(resp));
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                });
    }

 /*   public boolean deleteSubmission(String acc, UserSession session)
            throws BioStudiesClientException, IOException {
        logger.debug("deleteSubmission(session={}, acc={})", session, acc);
        String sbm = bsclient.getModifiedSubmission(acc, session.id());
        if (!sbm.isEmpty()) {
            bsclient.deleteModifiedSubmission(acc, session.id());
            return true;
        }
        sbm = bsclient.getSubmission(acc, session.id());
        //TODO do it better way
        if (sbm.isEmpty()) {
            return true;
        }
        String resp = bsclient.deleteSubmission(acc, session.id());
        JsonNode json = objectMapper().readTree(resp);
        String level = json.get("level").asText();
        return level.equalsIgnoreCase("success");
    }*/

    public Observable<Boolean> deleteSubmissionRx(String accno, UserSession session) {
        logger.debug("deleteSubmission(session={}, acc={})", session, accno);
        return getModifiedRx(accno, session)
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
        return bsclient.deleteModifiedSubmissionRx(accno, session.id())
                .map(resp -> true);
    }

    /*
    public String getSubmittedSubmissions(int offset, int limit, Map<String, String> paramMap, UserSession session)
            throws BioStudiesClientException, IOException {
        String sessId = session.id();
        return bsclient.getSubmissions(sessId, offset, limit, paramMap);
    }
      public String getModifiedSubmissions(int offset, int limit, Map<String, String> paramMap, UserSession session)
            throws BioStudiesClientException, IOException {
        SubmFilterParams params = SubmFilterParams.fromMap(paramMap);
        String sessId = session.id();

        JsonNode json = objectMapper().readTree(bsclient.getModifiedSubmissions(sessId));
        List<SubmissionListItem> submList = new ArrayList<>();

        if (json.isArray()) {
            json.forEach((JsonNode node) -> {
                try {
                    submList.add(SubmissionListItem.from(ModifiedSubmission.convert(node)));
                } catch (JsonProcessingException e) {
                    logger.error("Converting modified submissions error", e);
                }
            });
        }

        submList.sort(SubmissionListItem.sortByMTime());
        List<SubmissionListItem> filtered =
                submList
                        .stream()
                        .filter(params.asPredicate())
                        .skip(offset)
                        .limit(limit)
                        .collect(Collectors.toList());

        ObjectNode obj = JsonNodeFactory.instance.objectNode();
        obj.set("submissions", objectMapper().valueToTree(filtered));
        return obj.toString();
    }

*/
    public Observable<String> getSubmittedSubmissionsRx(int offset, int limit, Map<String, String> paramMap, UserSession session) {
        return bsclient.getSubmissionsRx(session.id(), offset, limit, paramMap);
        // todo check status property ?
    }

    public Observable<String> getModifiedSubmissionsRx(int offset, int limit, Map<String, String> paramMap, UserSession session) {
        SubmFilterParams params = SubmFilterParams.fromMap(paramMap);
        Predicate<? super SubmissionListItem> predicate = params.asPredicate();

        return bsclient.getModifiedSubmissionsRx(session.id())
                .map((String resp) -> {
                    try {
                        return objectMapper().readTree(resp);
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                })
                .flatMap(jsonNode -> {
                    List<JsonNode> list = new ArrayList<>();
                    if (jsonNode.isArray()) {
                        jsonNode.iterator().forEachRemaining(list::add);
                    }
                    return Observable.from(list);
                })
                .map(jsonNode -> {
                    try {
                        return SubmissionListItem.from(ModifiedSubmission.convert(jsonNode));
                    } catch (JsonProcessingException e) {
                        logger.error("Converting modified submissions error", e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .sorted((item1, item2) -> SubmissionListItem.sortByMTime().compare(item1, item2))
                .filter(predicate::test)
                .skip(offset)
                .limit(limit)
                .toList()
                .map((List<SubmissionListItem> items) -> {
                    ObjectNode obj = JsonNodeFactory.instance.objectNode();
                    obj.set("submissions", objectMapper().valueToTree(items));
                    return obj.toString();
                });
    }

/*
    public String getProjects(UserSession session) throws BioStudiesClientException, IOException {
        return bsclient.getProjects(session.id());
    }
*/

    public Observable<String> getProjectsRx(UserSession session) {
        return bsclient.getProjectsRx(session.id());
    }

    public String getFilesDir(String path, int depth, boolean showArchive, UserSession session) throws BioStudiesClientException, IOException {
        logger.debug("getFilesDir(session={}, path={}, depth={}, showArchive={})", session, path, depth, showArchive);
        return bsclient.getFilesDir(path, depth, showArchive, session.id());
    }

    public String deleteFile(String path, UserSession session)
            throws BioStudiesClientException, IOException {
        logger.debug("deleteFile(session={}, path={})", session, path);
        return bsclient.deleteFile(path, session.id());
    }

    public String signOut(UserSession session) throws BioStudiesClientException, IOException {
        logger.debug("signOut(session={})", session);
        ObjectNode obj = JsonNodeFactory.instance.objectNode();
        obj.put("sessid", session.id());
        return bsclient.signOut(obj.toString(), session.id());
    }

    public String signIn(String obj) throws BioStudiesClientException, IOException {
        return bsclient.signIn(obj);
    }

    public String signUp(SignUpParams params) throws BioStudiesClientException, IOException {
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
        return bsclient.signUp(json.toString());
    }

    public String passwordResetRequest(EmailPathCaptchaParams params) throws BioStudiesClientException, IOException {
        logger.debug("passwordResetRequest(obj={})", params);
        ObjectNode json = JsonNodeFactory.instance.objectNode();
        json.put("email", params.getEmail());
        json.put("recaptcha2-response", params.getCaptcha());
        json.put("resetURL", params.getPath());
        return bsclient.passwordResetRequest(json.toString());
    }

    public String passwordReset(KeyPasswordCaptchaParams params) throws IOException, BioStudiesClientException {
        logger.debug("passwordReset(obj={})", params);
        ObjectNode json = JsonNodeFactory.instance.objectNode();
        json.put("key", params.getKey());
        json.put("password", params.getPassword());
        json.put("recaptcha2-response", params.getCaptcha());
        return bsclient.passwordReset(json.toString());
    }

    public String resendActivationLink(EmailPathCaptchaParams params) throws IOException, BioStudiesClientException {
        logger.debug("resendActivationLink(params={})", params);
        ObjectNode json = JsonNodeFactory.instance.objectNode();
        json.put("email", params.getEmail());
        json.put("recaptcha2-response", params.getCaptcha());
        json.put("activationURL", params.getPath());
        return bsclient.resendActivationLink(json.toString());
    }

    public String activate(String key) throws IOException, BioStudiesClientException {
        logger.debug("activate(key={})", key);
        return bsclient.activate(key);
    }

    public String pubMedSearch(String id) {
        logger.debug("pubMedSearch(ID={})", id);
        ObjectNode result = JsonNodeFactory.instance.objectNode();

        try {
            JsonNode resp = objectMapper().readTree(europePmc.pubMedSearch(id));

            int hitCount = resp.path("hitCount").asInt();
            ObjectNode data = JsonNodeFactory.instance.objectNode();

            if (hitCount >= 1) {
                final JsonNode publ = resp.path("resultList").path("result").get(0);
                europePmcAttributes.forEach((key, value) -> {
                    if (publ.has(key)) {
                        data.put(value, publ.get(key).asText(""));
                    }
                });
            }
            result.put("status", "OK");
            result.set("data", data);
        } catch (IOException e) {
            logger.warn("EuropePMC call for ID={} failed: {}", id, e);
            result.put("status", "FAIL");
            result.set("data", JsonNodeFactory.instance.objectNode());
        }
        return result.toString();
    }
}
