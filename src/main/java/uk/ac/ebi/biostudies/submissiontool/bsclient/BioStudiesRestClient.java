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

import static java.util.Collections.singletonList;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.rx.rxjava.RxObservable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

/**
 * @author Olga Melnichuk
 */
public class BioStudiesRestClient implements BioStudiesClient {

    private static final String SESSION_TOKEN = "X-Session-Token";

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
    public Observable<String> getSubmissionRx(String acc, String sessionId) {
        return req(sessionId).getRx(baseTarget.path("/submission/" + acc));
    }

    @Override
    public Observable<String> deleteSubmissionRx(String acc, String sessionId) {
        // WTF: why it is GET?
        return req(sessionId).getRx(
                baseTarget.path("/submit/delete")
                        .queryParam("id", acc));
    }

    @Override
    public Observable<String> signUpRx(String obj) {
        return req().postJSONRx(baseTarget.path("/auth/signup"), obj);
    }

    @Override
    public Observable<String> passwordResetRequestRx(String obj) {
        return req().postJSONRx(baseTarget.path("/auth/passrstreq"), obj);
    }

    @Override
    public Observable<String> resendActivationLinkRx(String obj) {
        return req().postJSONRx(baseTarget.path("/auth/retryact"), obj);
    }

    @Override
    public String signIn(String obj) throws IOException {
        return req().postJSON(baseTarget.path("/auth/signin"), obj);
    }

    @Override
    public Observable<String> getPendingSubmissionRx(String acc, String sessionId) {
        return req(sessionId).getRx(baseTarget.path("/submissions/pending/" + acc));
    }

    @Override
    public Observable<String> deletePendingSubmissionRx(String acc, String sessionId) {
        return req(sessionId).deleteRx(baseTarget.path("/submissions/pending/" + acc));
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
                return readResponsePlain(resp).getBody();
            } catch (ProcessingException e) {
                if (resp != null)
                    resp.close();
                throw new IOException(e);
            }
        }

        private Observable<String> postJSONRx(WebTarget target, String data) {
            return postRx(target, Entity.json(data));
        }

        private Observable<String> putJSONRx(WebTarget target, String data) {
            return putRx(target, Entity.json(data));
        }

        private Observable<String> postRx(WebTarget target, Entity entity) {
            return RxObservable.from(target)
                    .request()
                    .headers(headers())
                    .rx()
                    .post(entity)
                    .switchMap(BioStudiesRequest::readResponse);
        }

        private Observable<String> putRx(WebTarget target, Entity entity) {
            return RxObservable.from(target)
                    .request()
                    .headers(headers())
                    .rx()
                    .put(entity)
                    .switchMap(BioStudiesRequest::readResponse);
        }

        private Observable<String> getRx(WebTarget target) {
            return RxObservable.from(target)
                    .request()
                    .headers(headers())
                    .rx()
                    .get()
                    .switchMap(BioStudiesRequest::readResponse);
        }

        private Observable<String> deleteRx(WebTarget target) {
            return RxObservable.from(target)
                    .request()
                    .headers(headers())
                    .rx()
                    .delete()
                    .switchMap(BioStudiesRequest::readResponse);
        }

        private static BioStudiesResponse readResponsePlain(Response resp) {
            int statusCode = resp.getStatus();

            String body = resp.readEntity(String.class);

            MediaType mediaType = resp.getMediaType();
            if (mediaType == null) {
                logger.warn("Server responded with NULL content-type: " + resp.getLocation());
            }

            // should not be needed any more (but fix direct submit in UI first)
            //if (statusCode == 200 && !getStatus(body).equalsIgnoreCase("ok")) {
            //    statusCode = 422;
            //}
            return new BioStudiesResponse(body, statusCode, mediaType);
        }

        private static Observable<String> readResponse(Response resp) {
            return readResponsePlain(resp).asObservable();
        }

        private static String getStatus(String body) {
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
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        private static boolean hasNextJsonToken(JsonToken token) {
            return token != null && token != JsonToken.END_OBJECT;
        }
    }

}
