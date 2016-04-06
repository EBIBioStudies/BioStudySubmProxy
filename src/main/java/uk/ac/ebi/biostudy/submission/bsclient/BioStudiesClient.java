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

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Arrays.stream;

/**
 * @author Olga Melnichuk
 */
public class BioStudiesClient {

    private static final Logger logger = LoggerFactory.getLogger(BioStudiesClient.class);

    private static final String SESSION_PARAM = "BIOSTDSESS";

    private final URI baseUrl;

    public BioStudiesClient(URI baseUrl) {
        this.baseUrl = baseUrl;
    }

    public JSONObject createSubmission(JSONObject obj, String sessionId)
            throws BioStudiesClientException, IOException {
        JSONObject copy = new JSONObject(obj.toString());
        copy.getJSONArray("submissions").getJSONObject(0).put("accno", "!{S-STA}");
        return parseJSON(post(composeUrl("/submit/create", SESSION_PARAM, sessionId), copy));
    }

    public JSONObject updateSubmission(JSONObject obj, String sessionId)
            throws BioStudiesClientException, IOException {
        return parseJSON(post(composeUrl("/submit/update", SESSION_PARAM, sessionId), obj));
    }

    public JSONObject getSubmission(String acc, String sessionId)
            throws BioStudiesClientException, IOException {
        return parseJSON(get(composeUrl("/submission/" + acc, SESSION_PARAM, sessionId)));
    }

    public JSONArray getSubmissions(String sessionId) throws BioStudiesClientException, IOException {
        JSONObject obj = parseJSON(get(composeUrl("/sbmlist", SESSION_PARAM, sessionId)));
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

    public void deleteSubmission(String acc, String sessionId) throws BioStudiesClientException, IOException {
        get(composeUrl("/submit/delete", SESSION_PARAM, sessionId, "id", acc));
    }

    public JSONObject getFilesDir(String sessionId) throws BioStudiesClientException, IOException {
        return parseJSON(get(composeUrl("/dir", SESSION_PARAM, sessionId)));
    }

    public JSONObject deleteFile(String file, String sessionId) throws BioStudiesClientException, IOException {
        return parseJSON(get(composeUrl("/dir", SESSION_PARAM, sessionId, "command", "delete", "file", file)));
    }

    public JSONObject signOut(String sessionId, String username) throws BioStudiesClientException, IOException {
        JSONObject obj = new JSONObject();
        obj.put("username", username);
        obj.put("sessid", sessionId);
        return parseJSON(post(composeUrl("/auth/signout", SESSION_PARAM, sessionId), obj));
    }

    public JSONObject signUp(JSONObject obj) throws BioStudiesClientException, IOException {
        return parseJSON(post(composeUrl("/auth/signup"), obj));
    }

    public JSONObject signIn(String username, String password) throws BioStudiesClientException, IOException {
        JSONObject obj = new JSONObject();
        obj.put("login", username);
        obj.put("password", password);
        return signIn(obj);
    }

    public JSONObject signIn(JSONObject obj) throws BioStudiesClientException, IOException {
        return parseJSON(post(composeUrl("/auth/signin"), obj));
    }

    private String get(URI url) throws BioStudiesClientException, IOException {
        return executeMethod(new HttpGet(url));
    }

    private String post(URI url, JSONObject data) throws BioStudiesClientException, IOException {
        HttpPost post = new HttpPost(url);
        post.setEntity(new StringEntity(data.toString()));
        post.setHeader("Accept", "application/json");
        post.setHeader("Content-Type", MediaType.APPLICATION_JSON);
        return executeMethod(post);
    }

    private String executeMethod(HttpRequestBase req) throws BioStudiesClientException, IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        try (CloseableHttpResponse response = client.execute(req)) {
            String body = EntityUtils.toString(response.getEntity(), "UTF-8");

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                return body;
            }
            throw new BioStudiesClientException(statusCode, response.getEntity().getContentType().getValue(), body);
        }
    }

    private JSONObject parseJSON(String str) {
        return new JSONObject(str);
    }

    private URI composeUrl(String relativePath, String... params) throws IOException {
        try {
            URIBuilder builder = new URIBuilder()
                    .setScheme(baseUrl.getScheme())
                    .setHost(baseUrl.getHost())
                    .setPort(baseUrl.getPort())
                    .setPath(asPath(baseUrl.getPath(), relativePath));

            IntStream.range(0, params.length)
                    .filter(n -> n % 2 == 0)
                    .forEach(n -> builder.setParameter(params[n], params[n + 1]));

            return builder.build();
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    private static String asPath(String... parts) {
        return "/" + stream(parts).flatMap(p -> stream(p.split("/"))).filter(string -> !string.isEmpty()).collect(Collectors.joining("/"));
    }
}
