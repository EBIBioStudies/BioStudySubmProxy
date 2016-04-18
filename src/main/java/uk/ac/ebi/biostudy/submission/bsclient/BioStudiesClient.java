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
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

/**
 * @author Olga Melnichuk
 */
public class BioStudiesClient {

    private static final Logger logger = LoggerFactory.getLogger(BioStudiesClient.class);

    private static final String SESSION_PARAM = "BIOSTDSESS";

    private static final String TMP_KEY_PARAM = "key";

    private static final String TMP_VALUE_PARAM = "value";

    private final URI baseUrl;

    public BioStudiesClient(URI baseUrl) {
        this.baseUrl = baseUrl;
    }

    public JSONObject createSubmission(JSONObject obj, String sessionId)
            throws BioStudiesClientException, IOException {
        JSONObject copy = new JSONObject(obj.toString());
        copy.getJSONArray("submissions").getJSONObject(0).put("accno", "!{S-STA}");
        return parseJSON(post(composeUrl("/submit/create"), copy, SESSION_PARAM, sessionId));
    }

    public JSONObject updateSubmission(JSONObject obj, String sessionId)
            throws BioStudiesClientException, IOException {
        return parseJSON(post(composeUrl("/submit/update"), obj, SESSION_PARAM, sessionId));
    }

    public JSONObject getSubmission(String acc, String sessionId)
            throws BioStudiesClientException, IOException {
        return parseJSON(get(composeUrl("/submission/" + acc), SESSION_PARAM, sessionId));
    }

    public JSONArray getSubmissions(String sessionId) throws BioStudiesClientException, IOException {
        JSONObject obj = parseJSON(get(composeUrl("/sbmlist"), SESSION_PARAM, sessionId));
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
        get(composeUrl("/submit/delete"), SESSION_PARAM, sessionId, "id", acc);
    }

    public JSONObject getFilesDir(String sessionId) throws BioStudiesClientException, IOException {
        return parseJSON(get(composeUrl("/dir"), SESSION_PARAM, sessionId));
    }

    public JSONObject deleteFile(String file, String sessionId) throws BioStudiesClientException, IOException {
        return parseJSON(get(composeUrl("/dir"), SESSION_PARAM, sessionId, "command", "delete", "file", file));
    }

    public JSONObject signOut(String sessionId) throws BioStudiesClientException, IOException {
        JSONObject obj = new JSONObject();
        obj.put("sessid", sessionId);
        return parseJSON(post(composeUrl("/auth/signout"), obj, SESSION_PARAM, sessionId));
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

    public void saveTmpSubmission(JSONObject obj, String accno, String sessionId) throws IOException, BioStudiesClientException {
        post(composeUrl("/userdata/set"), SESSION_PARAM, sessionId, TMP_KEY_PARAM, accno, TMP_VALUE_PARAM, obj.toString());
    }

    public void deleteTmpSubmission(String accno, String sessionId) throws IOException, BioStudiesClientException {
        post(composeUrl("/userdata/set"), SESSION_PARAM, sessionId, TMP_KEY_PARAM, accno);
    }

    public JSONArray listTmpSubmissions(String sessionId) throws IOException, BioStudiesClientException {
        return parseJSONArray(get(composeUrl("/userdata/listjson"), SESSION_PARAM, sessionId));
    }

    private String get(URI url, String... params) throws BioStudiesClientException, IOException {
        RequestBuilder builder = RequestBuilder.get().setUri(url);
        forEachParam(asList(params), builder::addParameter);
        return executeMethod(builder.build());
    }

    private String post(URI url, String... params) throws BioStudiesClientException, IOException {
        return post(url, null, params);
    }

    private String post(URI url, JSONObject data, String... params) throws BioStudiesClientException, IOException {
        RequestBuilder builder = RequestBuilder.post().setUri(url);
        forEachParam(asList(params), builder::addParameter);
        if (data != null) {
            builder.setEntity(new StringEntity(data.toString(), ContentType.APPLICATION_JSON));
        }
        return executeMethod(builder.build());
    }

    private String executeMethod(HttpUriRequest req) throws BioStudiesClientException, IOException {
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

    private JSONArray parseJSONArray(String str) {
        return new JSONArray(str);
    }

    private URI composeUrl(String relativePath) throws IOException {
        try {
            return new URIBuilder()
                    .setScheme(baseUrl.getScheme())
                    .setHost(baseUrl.getHost())
                    .setPort(baseUrl.getPort())
                    .setPath(asPath(baseUrl.getPath(), relativePath))
                    .build();
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    private <T> void forEachParam(List<String> params, BiFunction<String, String, T> func2) {
        IntStream.range(0, params.size())
                .filter(n -> n % 2 == 0)
                .forEach(n -> func2.apply(params.get(n), params.get(n + 1)));
    }

    private static String asPath(String... parts) {
        return "/" + stream(parts).flatMap(p -> stream(p.split("/"))).filter(string -> !string.isEmpty()).collect(Collectors.joining("/"));
    }

}
