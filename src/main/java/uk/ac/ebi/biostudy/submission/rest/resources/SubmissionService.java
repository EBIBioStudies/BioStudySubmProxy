/*
 * Copyright (c) 2016 European Molecular Biology Laboratory
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
package uk.ac.ebi.biostudy.submission.rest.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.biostudy.submission.bsclient.BioStudiesClient;
import uk.ac.ebi.biostudy.submission.bsclient.BioStudiesClientException;
import uk.ac.ebi.biostudy.submission.europepmc.EuropePmcClient;
import uk.ac.ebi.biostudy.submission.rest.data.ModifiedSubmission;
import uk.ac.ebi.biostudy.submission.rest.data.SubmissionListItem;
import uk.ac.ebi.biostudy.submission.rest.data.UserSession;
import uk.ac.ebi.biostudy.submission.rest.resources.params.EmailPathCaptchaParams;
import uk.ac.ebi.biostudy.submission.rest.resources.params.SignUpParams;
import uk.ac.ebi.biostudy.submission.rest.resources.params.SubmFilterParams;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.ac.ebi.biostudy.submission.rest.data.SubmissionListItem.byMTime;

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

    public ModifiedSubmission getSubmission(final UserSession userSession, final String accno, boolean origin)
            throws BioStudiesClientException, IOException {
        logger.debug("getSubmission(userSession={}, accno={}, origin={})", userSession, accno, origin);
        if (!origin) {
            String resp = bsclient.getModifiedSubmission(accno, userSession.getSessid());
            if (resp != null) {
                return ModifiedSubmission.parse(resp);
            }
        }
        return ModifiedSubmission.wrap(bsclient.getSubmission(accno, userSession.getSessid()));
    }

    public ModifiedSubmission createSubmission(UserSession userSession, String subm) throws IOException, BioStudiesClientException {
        logger.debug("createSubmission(userSession={})", userSession);
        ModifiedSubmission modified = ModifiedSubmission.wrap(subm);
        saveSubmission(userSession, modified);
        return modified;
    }

    public void saveSubmission(UserSession userSession, String subm) throws IOException, BioStudiesClientException {
        this.saveSubmission(userSession, ModifiedSubmission.parse(subm));
    }

    public void saveSubmission(UserSession userSession, ModifiedSubmission subm) throws IOException, BioStudiesClientException {
        logger.debug("saveSubmission(userSession={}, obj={})", userSession, subm);
        bsclient.saveModifiedSubmission(subm.update().json().toString(), subm.getAccno(), userSession.getSessid());
    }

    public String editSubmission(final UserSession userSession, final String accno)
            throws BioStudiesClientException, IOException {
        logger.debug("editSubmission(userSession={}, accno={})", userSession, accno);
        String sbm = bsclient.getModifiedSubmission(accno, userSession.getSessid());
        if (sbm == null) {
            logger.debug("no temporary copy; creating one...");
            ModifiedSubmission modified = getSubmission(userSession, accno, true);
            saveSubmission(userSession, modified);
            return modified.json().toString();
        }
        return sbm;
    }

    public String submitSubmission(UserSession userSession, String subm) throws IOException, BioStudiesClientException {
        logger.debug("submitSubmission(userSession={}, obj={})", userSession, subm);
        ModifiedSubmission modified = ModifiedSubmission.parse(subm);
        String data = modified.getData().toString();
        String result = modified.isNew() ?
                bsclient.submitNew(data, userSession.getSessid()) :
                bsclient.submitUpdated(data, userSession.getSessid());

        //TODO: better way to read BioStudies API response
        JsonNode resultNode = new ObjectMapper().readTree(result);
        String status = resultNode.get("status").asText();
        if (status.equals("OK")) {
            deleteSubmission(userSession, modified.getAccno());
        }
        return result;
    }

    public String directSubmit(UserSession userSession, boolean create, String obj) throws IOException, BioStudiesClientException {
        return create ? this.bsclient.submitNew(obj, userSession.getSessid()) :
                this.bsclient.submitUpdated(obj, userSession.getSessid());
    }

    public boolean deleteSubmission(final UserSession userSession, final String acc)
            throws BioStudiesClientException, IOException {
        logger.debug("deleteSubmission(userSession={}, acc={})", userSession, acc);
        String sbm = bsclient.getModifiedSubmission(acc, userSession.getSessid());
        if (sbm != null) {
            bsclient.deleteModifiedSubmission(acc, userSession.getSessid());
            return true;
        }
        sbm = bsclient.getSubmission(acc, userSession.getSessid());
        //TODO do it better way
        if (sbm == null) {
            return true;
        }
        String resp = bsclient.deleteSubmission(acc, userSession.getSessid());
        JsonNode json = new ObjectMapper().readTree(resp);
        String level = json.get("level").asText();
        return level.equalsIgnoreCase("success");
    }

    public String getSubmittedSubmissions(UserSession userSession, int offset, int limit, Map<String, String> paramMap)
            throws BioStudiesClientException, IOException {
        String sessId = userSession.getSessid();
        return bsclient.getSubmissions(sessId, offset, limit, paramMap);
    }

    public String getModifiedSubmissions(UserSession userSession, int offset, int limit, Map<String, String> paramMap)
            throws BioStudiesClientException, IOException {
        SubmFilterParams params = SubmFilterParams.fromMap(paramMap);
        String sessId = userSession.getSessid();

        JsonNode json = new ObjectMapper().readTree(bsclient.getModifiedSubmissions(sessId));
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

        submList.sort(byMTime());
        List<SubmissionListItem> filtered =
                submList
                .stream()
                .filter(params.asPredicate())
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());

        ObjectNode obj = JsonNodeFactory.instance.objectNode();
        obj.set("submissions", new ObjectMapper().valueToTree(filtered));
        return obj.toString();
    }

    public String getProjects(UserSession userSession) throws BioStudiesClientException, IOException {
        return bsclient.getProjects(userSession.getSessid());
    }

    public String getFilesDir(UserSession userSession, String path, int depth, boolean showArchive) throws BioStudiesClientException, IOException {
        logger.debug("getFilesDir(userSession={, path={}, depth={}, showArchive={})", userSession, path, depth, showArchive);
        return bsclient.getFilesDir(path, depth, showArchive, userSession.getSessid());
    }

    public String deleteFile(UserSession userSession, String path)
            throws BioStudiesClientException, IOException {
        logger.debug("deleteFile(userSession={}, path={})", userSession, path);
        return bsclient.deleteFile(path, userSession.getSessid());
    }

    public String signOut(UserSession userSession) throws BioStudiesClientException, IOException {
        logger.debug("signOut(userSession={})", userSession);
        ObjectNode obj = JsonNodeFactory.instance.objectNode();
        obj.put("sessid", userSession.getSessid());
        return bsclient.signOut(obj.toString(), userSession.getSessid());
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

    public String resendActivationLink(EmailPathCaptchaParams params) throws BioStudiesClientException, IOException {
        logger.debug("resendActivationLink(obj={})", params);
        ObjectNode json = JsonNodeFactory.instance.objectNode();
        json.put("email", params.getEmail());
        json.put("recaptcha2-response", params.getCaptcha());
        json.put("activationURL", params.getPath());
        return bsclient.resendActivationLink(json.toString());
    }

    public String pubMedSearch(String id) {
        logger.debug("pubMedSearch(ID={})", id);
        ObjectNode result = JsonNodeFactory.instance.objectNode();

        try {
            JsonNode resp = new ObjectMapper().readTree(europePmc.pubMedSearch(id));

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
