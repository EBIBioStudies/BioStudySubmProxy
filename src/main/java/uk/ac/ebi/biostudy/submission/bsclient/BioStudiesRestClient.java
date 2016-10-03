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

import org.glassfish.jersey.client.rx.rxjava.RxObservable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.*;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;

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

        WebTarget getSubmissionsReq(String sessionId) {
            return baseTarget.path("/sbmlist")
                    .queryParam(SESSION_PARAM, sessionId);
        }

        WebTarget deleteSubmissionReq(String sessionId, String acc) {
            return baseTarget.path("/submit/delete")
                    .queryParam(SESSION_PARAM, sessionId)
                    .queryParam("id", acc);
        }

        WebTarget getFilesDirReq(String sessionId) {
            return baseTarget.path("/dir")
                    .queryParam(SESSION_PARAM, sessionId);
        }

        WebTarget deleteFileReq(String sessionId, String file) {
            return baseTarget.path("/dir")
                    .queryParam(SESSION_PARAM, sessionId)
                    .queryParam("command", "delete")
                    .queryParam("file", file);
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

    public JSONObject submitNew(JSONObject obj, String sessionId)
            throws BioStudiesClientException, IOException {
        logger.debug("submitNew(obj={}, sessionId={})", obj, sessionId);
        JSONObject copy = new JSONObject(obj.toString());
        copy.put("accno", "!{S-BSST}");
        JSONArray array = new JSONArray();
        array.put(copy);
        JSONObject submissions = new JSONObject();
        submissions.put("submissions", array);
        return parseJSON(postJSON(
                targets.createSubmissionReq(sessionId), submissions));
    }

    public JSONObject submitUpdated(JSONObject obj, String sessionId)
            throws BioStudiesClientException, IOException {
        logger.debug("submitUpdated(obj={}, sessionId={})", obj, sessionId);
        JSONArray array = new JSONArray();
        array.put(obj);
        JSONObject submissions = new JSONObject();
        submissions.put("submissions", array);
        return parseJSON(postJSON(
                targets.updateSubmissionReq(sessionId), submissions));
    }

    public JSONObject getSubmission(String acc, String sessionId)
            throws BioStudiesClientException, IOException {
        logger.debug("getSubmission(acc={}, sessionId={})", acc, sessionId);
        return parseJSON(get(
                targets.getSubmissionReq(sessionId, acc)));
    }

    public JSONArray getSubmissions(String sessionId) throws BioStudiesClientException, IOException {
        logger.debug("getSubmissions(sessionId={})", sessionId);
        JSONObject obj = parseJSON(get(
                targets.getSubmissionsReq(sessionId)));
        if (obj.has("status")) {
            String status = obj.getString("status");
            logger.debug("in-json status: " + status);
            if (status.equals("OK")) {
                return obj.getJSONArray("submissions");
            }
        }
        logger.warn("not getting status in response");
        return new JSONArray();
    }

    public Observable<JSONArray> getSubmissionsRx(String sessionId) {
        logger.debug("getSubmissionsRx(sessionId={})", sessionId);
        return getRx(targets.getSubmissionsReq(sessionId))
                .map(respBody -> {
                    JSONObject obj = parseJSON(respBody);
                    if (obj.has("status")) {
                        String status = obj.getString("status");
                        if (status.equals("OK")) {
                            return obj.getJSONArray("submissions");
                        }
                    }
                    return new JSONArray();
                });
    }

    public boolean deleteSubmission(String acc, String sessionId) throws BioStudiesClientException, IOException {
        logger.debug("deleteSubmission(acc={}, sessionId={})", acc, sessionId);
        JSONObject resp = parseJSON(get(targets.deleteSubmissionReq(sessionId, acc)));
        return resp.has("level") && resp.getString("level").equalsIgnoreCase("success");
    }

    public JSONObject getFilesDir(String sessionId) throws BioStudiesClientException, IOException {
        logger.debug("getFilesDir(sessionId={})", sessionId);
        return parseJSON(get(targets.getFilesDirReq(sessionId)));
    }

    public JSONObject deleteFile(String file, String sessionId) throws BioStudiesClientException, IOException {
        logger.debug("deleteFile(file={}, sessionId={})", file, sessionId);
        return parseJSON(get(
                targets.deleteFileReq(sessionId, file)));
    }

    public JSONObject signOut(String sessionId) throws BioStudiesClientException, IOException {
        logger.debug("signOut(sessionId={})", sessionId);
        JSONObject obj = new JSONObject();
        obj.put("sessid", sessionId);
        return parseJSON(postJSON(
                targets.signOutReq(sessionId), obj));
    }

    public JSONObject signUp(JSONObject obj) throws BioStudiesClientException, IOException {
        logger.debug("signUp(obj={})", obj);
        return parseJSON(postJSON(
                targets.signUpReq(), obj));
    }

    public JSONObject passwordResetRequest(JSONObject obj) throws BioStudiesClientException, IOException {
        logger.debug("passwordResetRequest(obj={})", obj);
        return parseJSON(postJSON(
                targets.passwordResetReq(), obj));
    }

    @Override
    public JSONObject resendActivationLink(JSONObject obj) throws BioStudiesClientException, IOException {
        logger.debug("resendActivationLink(obj={})", obj);
        return parseJSON(postJSON(
                targets.resendActivationLinkReq(), obj));
    }

    public JSONObject signIn(String username, String password) throws BioStudiesClientException, IOException {
        logger.debug("signIn(username={}, password=...)", username);
        JSONObject obj = new JSONObject();
        obj.put("login", username);
        obj.put("password", password);
        return signIn(obj);
    }

    public JSONObject signIn(JSONObject obj) throws BioStudiesClientException, IOException {
        logger.debug("signIn(obj={})", obj);
        return parseJSON(postJSON(
                targets.signInReq(), obj));
    }

    public JSONObject getModifiedSubmission(String acc, String sessionId) throws IOException, BioStudiesClientException {
        logger.debug("getModifiedSubmission(acc={}, sessionId={})", acc, sessionId);
        return parseJSON(get(targets.getModifiedSubmissionReq(sessionId, acc)));
    }

    public void saveModifiedSubmission(JSONObject obj, String acc, String sessionId) throws IOException, BioStudiesClientException {
        logger.debug("saveModifiedSubmission(obj={}, acc={}, sessionId={})", obj, acc, sessionId);
        postForm(
                targets.saveModifiedSubmissionReq(sessionId),
                targets.saveModifiedSubmissionForm(acc, obj.toString()));
    }

    public void deleteModifiedSubmission(String acc, String sessionId) throws IOException, BioStudiesClientException {
        logger.debug("deleteModifiedSubmission(acc={}, sessionId={})", acc, sessionId);
        post(targets.deleteModifiedSubmissionReq(sessionId, acc));
    }

    public JSONArray getModifiedSubmissions(String sessionId) throws IOException, BioStudiesClientException {
        logger.debug("getModifiedSubmissions(sessionId={})", sessionId);
        return parseJSONArray(get(
                targets.getModifiedSubmissionsReq(sessionId)));
    }

    public Observable<JSONArray> getModifiedSubmissionsRx(String sessionId) {
        logger.debug("getModifiedSubmissionsRx(sessionId={})", sessionId);
        return getRx(targets.getModifiedSubmissionsReq(sessionId))
                .map(BioStudiesRestClient::parseJSONArray);
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

    private static Observable<String> getRx(WebTarget target) {
        return RxObservable.from(target)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .rx()
                .get()
                .onErrorReturn(throwable -> {
                    logger.error("getRx(...) error", throwable);
                    return null;
                })
                .flatMap(resp -> {
                    try {
                        return Observable.just(readResponse(resp));
                    } catch (BioStudiesClientException | IOException e) {
                        return Observable.error(e);
                    }
                });
    }

    private static String post(WebTarget target) throws BioStudiesClientException, IOException {
        return postJSON(target, null);
    }

    private static String postJSON(WebTarget target, JSONObject data) throws BioStudiesClientException, IOException {
        return post(target, Entity.json(data == null ? null : data.toString()));
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

    private static JSONObject parseJSON(String str) {
        return str == null || str.isEmpty() ? null : new JSONObject(str);
    }

    private static JSONArray parseJSONArray(String str) {
        return new JSONArray(str);
    }
}
