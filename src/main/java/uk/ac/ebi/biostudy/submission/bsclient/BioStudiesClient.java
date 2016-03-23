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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;

/**
 * @author Olga Melnichuk
 */
public class BioStudiesClient {

    private static final Logger logger = LoggerFactory.getLogger(BioStudiesClient.class);

    private static final String SESSION_PARAM = "BIOSTDSESS";

    private final URL baseUrl;

    public BioStudiesClient(String baseUrl) throws MalformedURLException {
        this(new URL(baseUrl));
    }

    public BioStudiesClient(URL baseUrl) {
        this.baseUrl = baseUrl;
    }

    public JSONObject createSubmission(JSONObject obj, String sessionId)
            throws BioStudiesClientException, IOException {
        JSONObject copy = new JSONObject(obj.toString());
        copy.getJSONArray("submissions").getJSONObject(0).put("accno", "!{S-STA}");
        return parseJSON(post("/submit/create", sessionId, copy));
    }

    public JSONObject updateSubmission(JSONObject obj, String sessionId)
            throws BioStudiesClientException, IOException {
        return parseJSON(post("/submit/update", sessionId, obj));
    }

    public JSONObject getSubmission(String acc, String sessionId)
            throws BioStudiesClientException, IOException {
        return parseJSON(get("/submission/" + acc, sessionId));
    }

    public JSONArray getSubmissions(String sessionId) throws BioStudiesClientException, IOException {
        JSONObject obj = parseJSON(get("/sbmlist", sessionId));
        if (obj.has("status")) {
            String status = obj.getString("status");
            logger.debug("in response status: " + status);
            if (status.equals("OK")) {
                return obj.getJSONArray("submissions");
            }
        }
        logger.warn("not getting status in getSubmissions(..) response");
        return new JSONArray();
    }

    public void deleteSubmission(String acc, String sessionId) throws BioStudiesClientException, IOException {
        get("/submit/delete", asList("id=", acc), sessionId);
    }

    public JSONObject getFilesDir(String sessionId) throws BioStudiesClientException, IOException {
        return parseJSON(get("/dir", sessionId));
    }

    public JSONObject deleteFile(String file, String sessionId) throws BioStudiesClientException, IOException {
        return parseJSON(get("/dir", asList("command", "delete", "file", file), sessionId));
    }

    public JSONObject signOut(String sessionId, String username) throws BioStudiesClientException, IOException {
        JSONObject obj = new JSONObject();
        obj.put("username", username);
        obj.put("sessid", sessionId);
        return parseJSON(post("/auth/signout", sessionId, obj));
    }

    public JSONObject signUp(JSONObject obj) throws BioStudiesClientException, IOException {
        return parseJSON(post("/auth/signup", null, obj));
    }

    public JSONObject signIn(String username, String password) throws BioStudiesClientException, IOException {
        JSONObject obj = new JSONObject();
        obj.append("login", username);
        obj.append("password", password);
        return parseJSON(post("auth/signin", null, obj));
    }

    private String get(String uri, String sessionId) throws BioStudiesClientException, IOException {
        return get(uri, Collections.emptyList(), sessionId);
    }

    private String get(String uri, List<String> params, String sessionId)
            throws BioStudiesClientException, IOException {

        //TODO user uribuilder
        String url = composeUrl(uri);
        logger.debug("GET::" + url);

        HttpGet httpmethod = new HttpGet(url);
        List<String> queryParams = new ArrayList<>();
        queryParams.addAll(params);
        if (sessionId != null) {
            queryParams.add(SESSION_PARAM);
            queryParams.add(sessionId);
        }
        if (!queryParams.isEmpty()) {
            String str = queryString(queryParams);
            logger.debug("queryString='" + str + "'");
            httpmethod.setQueryString(str);
        }
        return executeMethod(httpmethod);
    }

    private String post(String uri, String sessionId, JSONObject data) throws BioStudiesClientException, IOException {

        //TODO user uribuilder
        String url = composeUrl(uri);
        logger.debug("GET::" + url);

        HttpPost httpmethod = new HttpPost(composeUrl(uri));
        if (sessionId != null) {
            httpmethod.setQueryString(SESSION_PARAM + "=" + sessionId);
        }
        StringRequestEntity requestEntity = new StringRequestEntity(data.toString(), "application/json", "UTF-8");
        httpmethod.setRequestEntity(requestEntity);
        return executeMethod(httpmethod);
    }

    private String executeMethod(HttpRequestBase httpmethod) throws BioStudiesClientException, IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse response  = client.execute(httpmethod);
        try {
            String body = response.getEntity().toString();

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                return body;
            }
            throw new BioStudiesClientException(statusCode, response.getEntity().getContentType(), body);
        } finally {
            response.close();
        }
    }

    private JSONObject parseJSON(String str) {
        return new JSONObject(str);
    }

    private String queryString(List<String> params) {
        if (params.isEmpty()) {
            return "";
        }
        return IntStream.range(0, params.size())
                .filter(n -> n % 2 == 0)
                .mapToObj(n -> params.get(n - 1) + "=" + params.get(n))
                .collect(Collectors.joining("&"));
    }

    private String composeUrl(String relativePath) throws MalformedURLException {
        return new URL(baseUrl, relativePath).toString();
    }

}
