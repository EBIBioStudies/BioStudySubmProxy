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

package uk.ac.ebi.biostudy.submission.services;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.biostudy.submission.Res;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static java.util.Arrays.asList;

/**
 * @author Olga Melnichuk
 */
public class BioStudiesHttpClient {

    private static final Logger logger = LoggerFactory.getLogger(BioStudiesHttpClient.class);

    private static final String SESSION_PARAM = "BIOSTDSESS";

    private final URL baseUrl;

    public BioStudiesHttpClient(URL baseUrl) {
        this.baseUrl = baseUrl;
    }

    public JSONObject createSubmission(JSONObject obj, String sessionId) {
        // replace accno by
        obj.getJSONArray("submissions").getJSONObject(0).put("accno", "!{S-STA}");
        return parseJSON(post("/submit/create", sessionId, obj));
    }

    public JSONObject updateSubmission(JSONObject obj, String sessionId) {
        return parseJSON(post("/submit/update", sessionId, obj));
    }

    public JSONObject getSubmission(final String acc, String sessionId) throws IOException {
        return parseJSON(get("/submission/" + acc, sessionId));
    }

    public JSONArray getSubmissions(String sessionId) {
        JSONArray array = new JSONArray();
        JSONObject obj = parseJSON(get("/sbmlist", sessionId));
        if (obj.getString("status").equals("OK")) {
            array = obj.getJSONArray("submissions");
        }
        return array;
    }

    public void deleteSubmission(String acc, String sessionId) {
        get("/submit/delete", asList("id=", acc), sessionId);
    }

    public JSONObject getFilesDir(String sessionId) throws IOException, ServiceException {
        return parseJSON(get("/dir", sessionId));
    }

    public JSONObject deleteFile(String queryFile, String sessionId) throws IOException, ServiceException {
        //TODO
        post("/dir", asList("command", "delete", ))
        PostMethod httpmethod = new PostMethod(this.bsServerUrl + "/dir");
        httpmethod.setQueryString(
                SESSION_PARAM + "=" + sessionId + "&command=delete&" + queryFile);
        HttpClient client = new HttpClient();
        client.executeMethod(httpmethod);
        byte[] body = httpmethod.getResponseBody();
        httpmethod.releaseConnection();
        System.out.println(new String(body));
        JSONObject result = new JSONObject(new String(body));

        System.out.println("httpmethod.getStatusCode()" + httpmethod.getStatusCode());
        System.out.println("httpmethod.getStatusCode()" + result.toString());

        if (httpmethod.getStatusCode() != 200) {
            throw new ServiceException(httpmethod.getStatusCode(), result);
        }
        return result;

    }

    public JSONObject singOut(String sessionId) throws IOException {
        PostMethod httpmethod = new PostMethod(this.bsServerUrl + "/auth/signout");
        httpmethod.setQueryString(SESSION_PARAM + "=" + sessionId);
        HttpClient client = new HttpClient();
        JSONObject requestObj = new JSONObject();
        requestObj.put("username", userSession.getUsername());
        requestObj.put("sessid", userSession.getSessid());
        StringRequestEntity requestEntity = new StringRequestEntity(requestObj.toString(), "application/json", "UTF-8");
        httpmethod.setRequestEntity(requestEntity);

        client.executeMethod(httpmethod);
        byte[] body = httpmethod.getResponseBody();
        httpmethod.releaseConnection();
        JSONObject array = new JSONObject();
        System.out.println("httpmethod.getStatusCode()" + httpmethod.getStatusCode());
        JSONObject object = new JSONObject(new String(body));
        return object;
    }

    public JSONObject singUp(JSONObject signUpReq, String activationUrl) throws IOException {
        PostMethod httpmethod = new PostMethod(this.bsServerUrl + "/auth/signup");
        HttpClient client = new HttpClient();
        signUpReq.put("activationURL", activationUrl);
        StringRequestEntity requestEntity = new StringRequestEntity(signUpReq.toString(), "application/json", "UTF-8");
        httpmethod.setRequestEntity(requestEntity);

        client.executeMethod(httpmethod);
        byte[] body = httpmethod.getResponseBody();
        httpmethod.releaseConnection();
        JSONObject array = new JSONObject();
        System.out.println("httpmethod.getStatusCode()" + httpmethod.getStatusCode());
        JSONObject object = new JSONObject(new String(body));
        return object;
    }


    private String get(String uri, String sessionId) {
        return get(uri, Collections.emptyList(), sessionId);
    }

    private String get(String uri, List<String> params, String sessionId) {
        HttpMethod httpmethod = new GetMethod(composeUrl(uri));
        List<String> queryParams = new ArrayList<>();
        queryParams.addAll(params);
        if (sessionId != null) {
            queryParams.add(SESSION_PARAM);
            queryParams.add(sessionId);
        }
        if (!params.isEmpty()) {
            httpmethod.setQueryString(queryString(params));
        }
        HttpClient client = new HttpClient();
        client.executeMethod(httpmethod);
        String body = httpmethod.getResponseBodyAsString();
        httpmethod.releaseConnection();

        if (httpmethod.getStatusCode() == 200) {
            return body;
        }
        throw new ServiceException(httpmethod.getStatusCode(), body);
    }

    private String post(String uri, String sessionId, JSONObject data) {
        PostMethod httpmethod = new PostMethod(composeUrl(uri));
        if (sessionId != null) {
            httpmethod.setQueryString(SESSION_PARAM + "=" + sessionId);
        }
        StringRequestEntity requestEntity = new StringRequestEntity(data.toString(), "application/json", "UTF-8");
        httpmethod.setRequestEntity(requestEntity);
        HttpClient client = new HttpClient();
        client.executeMethod(httpmethod);
        byte[] body = httpmethod.getResponseBody();
        httpmethod.releaseConnection();
        if (httpmethod.getStatusCode() == 200) {
            return body;
        }
        throw new ServiceException(httpmethod.getStatusCode(), new String(body));
    }


    private JSONObject parseJSON(String str) {
       return new JSONObject(str);
    }

    private String queryString(List<String> params) {

    }

    private String composeUrl(String relativePath) throws MalformedURLException {
        return new URL(baseUrl, relativePath).toString();
    }

}
