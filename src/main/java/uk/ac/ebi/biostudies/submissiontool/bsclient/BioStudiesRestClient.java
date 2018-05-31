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

package uk.ac.ebi.biostudies.submissiontool.bsclient;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import org.glassfish.jersey.client.rx.rxjava.RxObservable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.exceptions.Exceptions;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonList;

/**
 * @author Olga Melnichuk
 */
public class BioStudiesRestClient implements BioStudiesClient {

    private static final String SESSION_TOKEN = "X-Session-Token";

    private static final String TMP_KEY_PARAM = "key";

    private static final String TMP_VALUE_PARAM = "value";

    private static final String TMP_TOPIC_PARAM = "topic";

    private static final String TMP_TOPIC_SUBMISSION = "submission";

    private static final Logger logger = LoggerFactory.getLogger(BioStudiesRestClient.class);

    private final Client rsClient;

    private final WebTarget baseTarget;

    public BioStudiesRestClient(URI baseUrl) {
        rsClient = ClientBuilder.newClient();
        baseTarget = rsClient.target(baseUrl);
    }

    public void close() {
        rsClient.close();
    }

    @Override
    public Observable<String> submitNewRx(String subm, String sessionId) {
        logger.debug("submitNew(obj={}, sessionId={})", subm, sessionId);
        return req(sessionId).postJSONRx(baseTarget.path("/submit/create"), subm);
    }

    @Override
    public Observable<String> submitUpdatedRx(String obj, String sessionId) {
        logger.debug("submitUpdated(obj={}, sessionId={})", obj, sessionId);
        return req(sessionId).postJSONRx(baseTarget.path("/submit/update"), obj);
    }

    @Override
    public Observable<String> getSubmissionRx(String acc, String sessionId) {
        logger.debug("getSubmission(acc={}, sessionId={})", acc, sessionId);
        return req(sessionId).getRx(baseTarget.path("/submission/" + acc));
    }

    /* @Override
     public String getSubmissions(String sessionId, Map<String, String> paramMap) throws  IOException {
         logger.debug("getSubmissions(sessionId={})", sessionId);
         return req(sessionId).get(targets.getSubmissionsReq(paramMap));
     }
 */
    @Override
    public Observable<String> getSubmissionsRx(String sessionId, Map<String, String> params) {
        WebTarget t = baseTarget.path("/sbmlist");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            t = t.queryParam(entry.getKey(), entry.getValue());
        }
        return req(sessionId).getRx(t);
    }

    @Override
    public Observable<String> getProjectsRx(String sessionId) {
        logger.debug("getProjects(sessionId={})", sessionId);
        return req(sessionId).getRx(
                baseTarget.path("/atthost")
                        .queryParam("type", "Project")
                        .queryParam("format", "json"));
    }

    @Override
    public Observable<String> deleteSubmissionRx(String acc, String sessionId) {
        logger.debug("deleteSubmission(acc={}, sessionId={})", acc, sessionId);
        // WTF: why it is GET?
        return req(sessionId).getRx(
                baseTarget.path("/submit/delete")
                        .queryParam("id", acc));
    }

    @Override
    public Observable<String> getFilesDirRx(String path, int depth, boolean showArchive, String sessionId) {
        logger.debug("getFilesDir(sessionId={}, path={}, depth={}, showArchive={})", sessionId, path, depth, showArchive);
        return req(sessionId).getRx(
                baseTarget.path("/dir")
                        .queryParam("path", path)
                        .queryParam("depth", depth)
                        .queryParam("showArchive", showArchive));
    }

    @Override
    public Observable<String> deleteFileRx(String file, String sessionId) {
        logger.debug("deleteFile(file={}, sessionId={})", file, sessionId);
        // WTF: why it is GET?
        return req(sessionId).getRx(
                baseTarget.path("/dir")
                        .queryParam("command", "rm")
                        .queryParam("path", file)
        );
    }

    @Override
    public Observable<String> signOutRx(String obj, String sessionId) {
        logger.debug("signOut(sessionId={})", sessionId);
        return req(sessionId).postJSONRx(baseTarget.path("/auth/signout"), obj);
    }

    @Override
    public Observable<String> signUpRx(String obj) {
        logger.debug("signUp(obj={})", obj);
        return req().postJSONRx(baseTarget.path("/auth/signup"), obj);
    }

    @Override
    public Observable<String> passwordResetRequestRx(String obj) {
        logger.debug("passwordResetRequest(obj={})", obj);
        return req().postJSONRx(baseTarget.path("/auth/passrstreq"), obj);
    }

    @Override
    public Observable<String> passwordResetRx(String obj) {
        logger.debug("passwordReset(obj={})", obj);
        return req().postJSONRx(baseTarget.path("/auth/passreset"), obj);
    }

    @Override
    public Observable<String> resendActivationLinkRx(String obj) {
        logger.debug("resendActivationLink(obj={})", obj);
        return req().postJSONRx(baseTarget.path("/auth/retryact"), obj);
    }

    @Override
    public Observable<String> activateRx(String key) {
        logger.debug("activate(obj={})", key);
        return req().postJSONRx(baseTarget.path("/auth/activate/" + key), "{}");
    }

    @Override
    public String signIn(String obj) throws IOException {
        logger.debug("signIn(obj={})", obj);
        return req().postJSON(baseTarget.path("/auth/signin"), obj);
    }

    @Override
    public Observable<String> getModifiedSubmissionRx(String acc, String sessionId) {
        logger.debug("getModifiedSubmission(acc={}, sessionId={})", acc, sessionId);
        return req(sessionId).getRx(
                baseTarget.path("/userdata/get")
                        .queryParam(TMP_TOPIC_PARAM, TMP_TOPIC_SUBMISSION)
                        .queryParam(TMP_KEY_PARAM, acc));
    }

    @Override
    public Observable<String> saveModifiedSubmissionRx(String obj, String acc, String sessionId) {
        logger.debug("saveModifiedSubmission(obj={}, acc={}, sessionId={})", obj, acc, sessionId);
        return req(sessionId).postFormRx(
                baseTarget.path("/userdata/set").queryParam(TMP_TOPIC_PARAM, TMP_TOPIC_SUBMISSION),
                new Form()
                        .param(TMP_KEY_PARAM, acc)
                        .param(TMP_VALUE_PARAM, obj));
    }

    @Override
    public Observable<String> deleteModifiedSubmissionRx(String acc, String sessionId) {
        logger.debug("deleteModifiedSubmission(acc={}, sessionId={})", acc, sessionId);
        // Note: adding empty object as data here to make POSt request body not empty, otherwise Content-Length: 0 header is required
        return req(sessionId).postJSONRx(
                baseTarget.path("/userdata/del")
                        .queryParam(TMP_TOPIC_PARAM, TMP_TOPIC_SUBMISSION)
                        .queryParam(TMP_KEY_PARAM, acc),
                "{}");
    }

    @Override
    public Observable<String> getModifiedSubmissionsRx(String sessionId) {
        logger.debug("getModifiedSubmissions(sessionId={})", sessionId);
        return req(sessionId).getRx(
                baseTarget
                        .path("/userdata/listjson")
                        .queryParam(TMP_TOPIC_PARAM, TMP_TOPIC_SUBMISSION));
    }

    private static BioStudiesRequest req(String sessionId) {
        return new BioStudiesRequest(sessionId);
    }

    private static BioStudiesRequest req() {
        return new BioStudiesRequest();
    }

    private static class BioStudiesRequest {
        private final String sessionId;

        private BioStudiesRequest() {
            this.sessionId = null;
        }

        private BioStudiesRequest(String sessionId) {
            this.sessionId = sessionId;
        }

        private MultivaluedHashMap<String, Object> headers() {
            MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
            headers.put(HttpHeaders.ACCEPT, singletonList(MediaType.APPLICATION_JSON));
            Optional.ofNullable(sessionId).ifPresent(s -> headers.put(SESSION_TOKEN, singletonList(s)));
            return headers;
        }

        private String postJSON(WebTarget target, String data) throws IOException {
            return post(target, Entity.json(data));
        }

        private String post(WebTarget target, Entity entity) throws IOException {
            Invocation.Builder builder = target
                    .request()
                    .headers(headers());

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

        private String get(WebTarget target) throws IOException {
            Invocation.Builder builder = target
                    .request()
                    .headers(headers());

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

        private Observable<String> postJSONRx(WebTarget target, String data) {
            return postRx(target, Entity.json(data));
        }

        private Observable<String> postFormRx(WebTarget target, Form data) {
            return postRx(target, Entity.entity(data, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
        }

        private Observable<String> postRx(WebTarget target, Entity entity) {
            return RxObservable.from(target)
                    .request()
                    .headers(headers())
                    .rx()
                    .post(entity)
                    .map(resp -> {
                                try {
                                    return readResponse(resp);
                                } catch (IOException e) {
                                    throw Exceptions.propagate(e);
                                }
                            }
                    );
        }

        private Observable<String> getRx(WebTarget target) {
            return RxObservable.from(target)
                    .request()
                    .headers(headers())
                    .rx()
                    .get()
                    .map(resp -> {
                                try {
                                    return readResponse(resp);
                                } catch (IOException e) {
                                    throw Exceptions.propagate(e);
                                }
                            }
                    );
        }

        private static String readResponse(Response resp) throws IOException {
            int statusCode = resp.getStatus();

            String body = resp.readEntity(String.class);

            MediaType mediaType = resp.getMediaType();
            if (mediaType == null) {
                logger.warn("Server responded with NULL content-type: " + resp.getLocation());
            }

            if (statusCode != 200) {
                throw new BioStudiesRxClientException(statusCode, mediaType == null ? MediaType.TEXT_PLAIN : mediaType.getType(), body);
            }

            if (!getStatus(body).equalsIgnoreCase("ok")) {
                throw new BioStudiesRxClientException(422, mediaType == null ? MediaType.TEXT_PLAIN : mediaType.getType(), body);
            }
            return body;
        }

        private static String getStatus(String body) throws IOException {
            JsonFactory f = new MappingJsonFactory();
            try (JsonParser jp = f.createParser(body)) {
                while (hasNextJsonToken(jp.nextToken())) {
                    String fieldName = jp.getCurrentName();
                    if (fieldName != null && fieldName.equalsIgnoreCase("status")) {
                        jp.nextToken();
                        String value = jp.getText();
                        return value == null ? "" : value.toLowerCase();
                    }
                }
                // no field status
                return "ok";
            }
        }

        private static boolean hasNextJsonToken(JsonToken token) {
            return token != null && token != JsonToken.END_OBJECT;
        }
    }

}
