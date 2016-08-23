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

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;

/**
 * @author Olga Melnichuk
 */
public class BioStudiesClient {

    private static final Logger logger = LoggerFactory.getLogger(BioStudiesClient.class);

    private static final String SESSION_PARAM = "BIOSTDSESS";

    private static final String TMP_KEY_PARAM = "key";

    private static final String TMP_VALUE_PARAM = "value";

    private static final String TMP_TOPIC_PARAM = "topic";

    private static final String TMP_TOPIC_SUBMISSION = "submission";

    private final WebTarget webTarget;

    public BioStudiesClient(URI baseUrl) {
        Client rsClient = ClientBuilder.newClient();
        webTarget = rsClient.target(baseUrl);
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
        return parseJSON(post(
                webTarget.path("/submit/create")
                        .queryParam(SESSION_PARAM, sessionId), submissions));
    }

    public JSONObject submitUpdated(JSONObject obj, String sessionId)
            throws BioStudiesClientException, IOException {
        logger.debug("submitUpdated(obj={}, sessionId={})", obj, sessionId);
        JSONArray array = new JSONArray();
        array.put(obj);
        JSONObject submissions = new JSONObject();
        submissions.put("submissions", array);
        return parseJSON(post(
                webTarget.path("/submit/update")
                        .queryParam(SESSION_PARAM, sessionId), submissions));
    }

    public JSONObject getSubmission(String acc, String sessionId)
            throws BioStudiesClientException, IOException {
        logger.debug("getSubmission(acc={}, sessionId={})", acc, sessionId);
        return parseJSON(get(
                webTarget.path("/submission/" + acc)
                        .queryParam(SESSION_PARAM, sessionId)));
    }

    public JSONArray getSubmissions(String sessionId) throws BioStudiesClientException, IOException {
        logger.debug("getSubmissions(sessionId={})", sessionId);
        JSONObject obj = parseJSON(get(
                webTarget.path("/sbmlist")
                        .queryParam(SESSION_PARAM, sessionId)));
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

    public boolean deleteSubmission(String acc, String sessionId) throws BioStudiesClientException, IOException {
        logger.debug("deleteSubmission(acc={}, sessionId={})", acc, sessionId);
        JSONObject resp = parseJSON(get(
                webTarget.path("/submit/delete")
                        .queryParam(SESSION_PARAM, sessionId)
                        .queryParam("id", acc)));
        return resp.has("level") && resp.getString("level").equalsIgnoreCase("success");
    }

    public JSONObject getFilesDir(String sessionId) throws BioStudiesClientException, IOException {
        logger.debug("getFilesDir(sessionId={})", sessionId);
        return parseJSON(get(
                webTarget.path("/dir")
                        .queryParam(SESSION_PARAM, sessionId)));
    }

    public JSONObject deleteFile(String file, String sessionId) throws BioStudiesClientException, IOException {
        logger.debug("deleteFile(file={}, sessionId={})", file, sessionId);
        return parseJSON(get(
                webTarget.path("/dir")
                        .queryParam(SESSION_PARAM, sessionId)
                        .queryParam("command", "delete")
                        .queryParam("file", file)));
    }

    public JSONObject signOut(String sessionId) throws BioStudiesClientException, IOException {
        logger.debug("signOut(sessionId={})", sessionId);
        JSONObject obj = new JSONObject();
        obj.put("sessid", sessionId);
        return parseJSON(post(
                webTarget.path("/auth/signout")
                        .queryParam(SESSION_PARAM, sessionId), obj));
    }

    public JSONObject signUp(JSONObject obj) throws BioStudiesClientException, IOException {
        logger.debug("signUp(obj={})", obj);
        return parseJSON(post(
                webTarget.path("/auth/signup"), obj));
    }

    public JSONObject passwordResetRequest(JSONObject obj) throws BioStudiesClientException, IOException {
        logger.debug("passwordResetRequest(obj={})", obj);
        return parseJSON(post(
                webTarget.path("/auth/passrstreq"), obj));
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
        return parseJSON(post(
                webTarget.path("/auth/signin"), obj));
    }

    public JSONObject getTmpSubmission(String accno, String sessionId) throws IOException, BioStudiesClientException {
        logger.debug("getTmpSubmission(accno={}, sessionId={})", accno, sessionId);
        return parseJSON(get(webTarget.path("/userdata/get")
                .queryParam(SESSION_PARAM, sessionId)
                .queryParam(TMP_TOPIC_PARAM, TMP_TOPIC_SUBMISSION)
                .queryParam(TMP_KEY_PARAM, accno)));
    }

    public void saveTmpSubmission(JSONObject obj, String accno, String sessionId) throws IOException, BioStudiesClientException {
        logger.debug("saveTmpSubmission(obj={}, accno={}, sessionId={})", obj, accno, sessionId);
        post(webTarget.path("/userdata/set")
                .queryParam(SESSION_PARAM, sessionId)
                .queryParam(TMP_TOPIC_PARAM, TMP_TOPIC_SUBMISSION)
                .queryParam(TMP_KEY_PARAM, accno, TMP_VALUE_PARAM), obj);
    }

    public void deleteTmpSubmission(String accno, String sessionId) throws IOException, BioStudiesClientException {
        logger.debug("deleteTmpSubmission(accno={}, sessionId={})", accno, sessionId);
        post(webTarget.path("/userdata/set")
                .queryParam(SESSION_PARAM, sessionId)
                .queryParam(TMP_TOPIC_PARAM, TMP_TOPIC_SUBMISSION)
                .queryParam(TMP_KEY_PARAM, accno));
    }

    public JSONArray listTmpSubmissions(String sessionId) throws IOException, BioStudiesClientException {
        logger.debug("listTmpSubmissions(sessionId={})", sessionId);
        return parseJSONArray(get(
                webTarget.path("/userdata/listjson")
                        .queryParam(SESSION_PARAM, sessionId)
                        .queryParam(TMP_TOPIC_PARAM, TMP_TOPIC_SUBMISSION)));
    }

    private String get(WebTarget target) throws BioStudiesClientException, IOException {
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

    private String post(WebTarget target) throws BioStudiesClientException, IOException {
        return post(target, null);
    }

    private String post(WebTarget target, JSONObject data) throws BioStudiesClientException, IOException {
        Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON_TYPE);
        Response resp = null;
        try {
            resp = builder.post(Entity.json(data == null ? null : data.toString()));
            return readResponse(resp);
        } catch (ProcessingException e) {
            if (resp != null)
                resp.close();
            throw new IOException(e);
        }
    }

    private String readResponse(Response resp) throws BioStudiesClientException, IOException {
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

    private JSONObject parseJSON(String str) {
        return str == null || str.isEmpty() ? null : new JSONObject(str);
    }

    private JSONArray parseJSONArray(String str) {
        return new JSONArray(str);
    }
}
