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

package uk.ac.ebi.biostudy.submission.bsclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.*;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

/**
 * @author Olga Melnichuk
 */
public class BioStudiesRestClient implements BioStudiesClient {

    private static class APITargets {
        private static final String SESSION_PARAM = "BIOSTDSESS";

        private static final String TMP_KEY_PARAM = "key";

        private static final String TMP_VALUE_PARAM = "value";

        private static final String TMP_TOPIC_PARAM = "topic";

        private static final String TMP_TOPIC_SUBMISSION = "submission";

        private final WebTarget baseTarget;

        APITargets(WebTarget baseTarget) {
            this.baseTarget = baseTarget;
        }

        WebTarget createSubmissionReq(String sessionId) {
            return baseTarget.path("/submit/create")
                    .queryParam(SESSION_PARAM, sessionId);
        }

        WebTarget updateSubmissionReq(String sessionId) {
            return baseTarget.path("/submit/update")
                    .queryParam(SESSION_PARAM, sessionId);
        }

        WebTarget getSubmissionReq(String sessionId, String acc) {
            return baseTarget.path("/submission/" + acc)
                    .queryParam(SESSION_PARAM, sessionId);
        }

        WebTarget getSubmissionsReq(String sessionId, int offset, int limit, Map<String, String> moreParams) {
            WebTarget t = baseTarget.path("/sbmlist")
                    .queryParam(SESSION_PARAM, sessionId)
                    .queryParam("offset", offset)
                    .queryParam("limit", limit);
            for (Map.Entry<String, String> entry : moreParams.entrySet()) {
                t = t.queryParam(entry.getKey(), entry.getValue());
            }
            return t;
        }

        WebTarget getProjectsReq(String sessionId) {
            return baseTarget.path("/atthost")
                    .queryParam(SESSION_PARAM, sessionId)
                    .queryParam("type", "Project")
                    .queryParam("format", "json");
        }

        WebTarget deleteSubmissionReq(String sessionId, String acc) {
            return baseTarget.path("/submit/delete")
                    .queryParam(SESSION_PARAM, sessionId)
                    .queryParam("id", acc);
        }

        WebTarget getFilesDirReq(String path, int depth, boolean showArchive, String sessionId) {
            return baseTarget.path("/dir")
                    .queryParam(SESSION_PARAM, sessionId)
                    .queryParam("path", path)
                    .queryParam("depth", depth)
                    .queryParam("showArchive", showArchive);
        }

        WebTarget deleteFileReq(String sessionId, String file) {
            return baseTarget.path("/dir")
                    .queryParam(SESSION_PARAM, sessionId)
                    .queryParam("command", "rm")
                    .queryParam("path", file);
        }

        WebTarget signInReq() {
            return baseTarget.path("/auth/signin");
        }

        WebTarget signUpReq() {
            return baseTarget.path("/auth/signup");
        }

        WebTarget signOutReq(String sessionId) {
            return baseTarget.path("/auth/signout")
                    .queryParam(SESSION_PARAM, sessionId);
        }

        WebTarget passwordResetReq() {
            return baseTarget.path("/auth/passrstreq");
        }

        WebTarget resendActivationLinkReq() {
            return baseTarget.path("/auth/retryact");
        }

        WebTarget deleteModifiedSubmissionReq(String sessionId, String acc) {
            return baseTarget.path("/userdata/del")
                    .queryParam(SESSION_PARAM, sessionId)
                    .queryParam(TMP_TOPIC_PARAM, TMP_TOPIC_SUBMISSION)
                    .queryParam(TMP_KEY_PARAM, acc);
        }

        WebTarget saveModifiedSubmissionReq(String sessionId) {
            return baseTarget.path("/userdata/set")
                    .queryParam(SESSION_PARAM, sessionId)
                    .queryParam(TMP_TOPIC_PARAM, TMP_TOPIC_SUBMISSION);
        }

        Form saveModifiedSubmissionForm(String acc, String value) {
            return new Form()
                    .param(TMP_KEY_PARAM, acc)
                    .param(TMP_VALUE_PARAM, value);
        }

        WebTarget getModifiedSubmissionReq(String sessionId, String acc) {
            return baseTarget.path("/userdata/get")
                    .queryParam(SESSION_PARAM, sessionId)
                    .queryParam(TMP_TOPIC_PARAM, TMP_TOPIC_SUBMISSION)
                    .queryParam(TMP_KEY_PARAM, acc);
        }

        WebTarget getModifiedSubmissionsReq(String sessionId) {
            return baseTarget
                    .path("/userdata/listjson")
                    .queryParam(SESSION_PARAM, sessionId)
                    .queryParam(TMP_TOPIC_PARAM, TMP_TOPIC_SUBMISSION);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(BioStudiesRestClient.class);

    private final APITargets targets;

    private final Client rsClient;

    public BioStudiesRestClient(URI baseUrl) {
        rsClient = ClientBuilder.newClient();
        targets = new APITargets(rsClient.target(baseUrl));
    }

    public void close() {
        rsClient.close();
    }

    public String submitNew(String subm, String sessionId) throws BioStudiesClientException, IOException {
        logger.debug("submitNew(obj={}, sessionId={})", subm, sessionId);
        return postJSON(targets.createSubmissionReq(sessionId), subm);
    }

   /*
    private Optional<String> projectAccNoTemplate(String accnoAttr, String sessionId) throws IOException, BioStudiesClientException {
        MyJSONObject proj = new MyJSONObject(getSubmission(accnoAttr, sessionId));
        Optional<MyJSONArray> projAttrs = proj.getJSONArray("attributes");
        if (!projAttrs.isPresent()) {
            return Optional.empty();
        }

        List<Optional<String>> templates = projAttrs.get()
                .getMyJSONObjects()
                .filter(obj -> {
                    Optional<String> name = obj.getString("name");
                    return name.isPresent() && name.get().equalsIgnoreCase("accnotemplate");
                })
                .map(obj -> obj.getString("value"))
                .collect(Collectors.toList());
        return templates.isEmpty() ? Optional.empty() : templates.get(0);
    }
*/
    public String submitUpdated(String obj, String sessionId) throws BioStudiesClientException, IOException {
        logger.debug("submitUpdated(obj={}, sessionId={})", obj, sessionId);
        return postJSON(targets.updateSubmissionReq(sessionId), obj);
    }

    public String getSubmission(String acc, String sessionId) throws BioStudiesClientException, IOException {
        logger.debug("getSubmission(acc={}, sessionId={})", acc, sessionId);
        return get(targets.getSubmissionReq(sessionId, acc));
    }

    public String getSubmissions(String sessionId, int offset, int limit, Map<String, String> paramMap) throws BioStudiesClientException, IOException {
        logger.debug("getSubmissions(sessionId={})", sessionId);
        return get(targets.getSubmissionsReq(sessionId, offset, limit, paramMap));
    }

    public String getProjects(String sessionId) throws BioStudiesClientException, IOException {
        logger.debug("getProjects(sessionId={})", sessionId);
        return get(targets.getProjectsReq(sessionId));
    }

    public String deleteSubmission(String acc, String sessionId) throws BioStudiesClientException, IOException {
        logger.debug("deleteSubmission(acc={}, sessionId={})", acc, sessionId);
        return get(targets.deleteSubmissionReq(sessionId, acc));
    }

    public String getFilesDir(String path, int depth, boolean showArchive, String sessionId) throws BioStudiesClientException, IOException {
        logger.debug("getFilesDir(sessionId={}, path={}, depth={}, showArchive={})", sessionId, path, depth, showArchive);
        return get(targets.getFilesDirReq(path, depth, showArchive, sessionId));
    }

    public String deleteFile(String file, String sessionId) throws BioStudiesClientException, IOException {
        logger.debug("deleteFile(file={}, sessionId={})", file, sessionId);
        return get(targets.deleteFileReq(sessionId, file));
    }

    public String signOut(String obj, String sessionId) throws BioStudiesClientException, IOException {
        logger.debug("signOut(sessionId={})", sessionId);
        return postJSON(targets.signOutReq(sessionId), obj);
    }

    public String signUp(String obj) throws BioStudiesClientException, IOException {
        logger.debug("signUp(obj={})", obj);
        return postJSON(targets.signUpReq(), obj);
    }

    public String passwordResetRequest(String obj) throws BioStudiesClientException, IOException {
        logger.debug("passwordResetRequest(obj={})", obj);
        return postJSON(targets.passwordResetReq(), obj);
    }

    @Override
    public String resendActivationLink(String obj) throws BioStudiesClientException, IOException {
        logger.debug("resendActivationLink(obj={})", obj);
        return postJSON(targets.resendActivationLinkReq(), obj);
    }

    public String signIn(String username, String password) throws BioStudiesClientException, IOException {
        logger.debug("signIn(username={}, password=...)", username);
        ObjectNode obj = JsonNodeFactory.instance.objectNode();
        obj.put("login", username);
        obj.put("password", password);
        return signIn(obj.toString());
    }

    public String signIn(String obj) throws BioStudiesClientException, IOException {
        logger.debug("signIn(obj={})", obj);
        return postJSON(targets.signInReq(), obj);
    }

    public String getModifiedSubmission(String acc, String sessionId) throws IOException, BioStudiesClientException {
        logger.debug("getModifiedSubmission(acc={}, sessionId={})", acc, sessionId);
        return get(targets.getModifiedSubmissionReq(sessionId, acc));
    }

    public String saveModifiedSubmission(String obj, String acc, String sessionId) throws IOException, BioStudiesClientException {
        logger.debug("saveModifiedSubmission(obj={}, acc={}, sessionId={})", obj, acc, sessionId);
        return postForm(
                targets.saveModifiedSubmissionReq(sessionId),
                targets.saveModifiedSubmissionForm(acc, obj));
    }

    public String deleteModifiedSubmission(String acc, String sessionId) throws IOException, BioStudiesClientException {
        logger.debug("deleteModifiedSubmission(acc={}, sessionId={})", acc, sessionId);
        return postJSON(targets.deleteModifiedSubmissionReq(sessionId, acc), null);
    }

    public String getModifiedSubmissions(String sessionId) throws IOException, BioStudiesClientException {
        logger.debug("getModifiedSubmissions(sessionId={})", sessionId);
        return get(targets.getModifiedSubmissionsReq(sessionId));
    }

    private static String postJSON(WebTarget target, String data) throws BioStudiesClientException, IOException {
        return post(target, Entity.json(data));
    }

    private static String postForm(WebTarget target, Form data) throws BioStudiesClientException, IOException {
        return post(target, Entity.entity(data, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
    }

    private static String post(WebTarget target, Entity entity) throws BioStudiesClientException, IOException {
        Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON_TYPE);
        Response resp = null;
        try {
            resp = builder.post(entity);
            return readResponse(resp);
        } catch (ProcessingException e) {
            if (resp != null)
                resp.close();
            throw new IOException(e);
        }
    }

    private static String get(WebTarget target) throws BioStudiesClientException, IOException {
        Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON_TYPE);
        Response resp = null;
        try {
            resp = builder.get();
            return readResponse(resp);
        } catch (ProcessingException e) {
            throw new IOException(e);
        } finally {
            if (resp != null)
                resp.close();
        }
    }

    private static String readResponse(Response resp) throws BioStudiesClientException, IOException {
        if (resp == null) {
            throw new IOException("null response; see logs for details");
        }
        String body = resp.readEntity(String.class);
        MediaType mediaType = resp.getMediaType();
        if (mediaType == null) {
            logger.warn("Server responded with NULL content-type: " + resp.getLocation());
        }
        int statusCode = resp.getStatus();
        if (statusCode == 200) {
            return body;
        }
        throw new BioStudiesClientException(statusCode, mediaType == null ? MediaType.TEXT_PLAIN : mediaType.getType(), body);
    }
}
