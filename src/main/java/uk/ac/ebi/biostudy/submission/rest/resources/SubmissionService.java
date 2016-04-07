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

import org.json.JSONArray;
import org.json.JSONObject;
import uk.ac.ebi.biostudy.submission.bsclient.BioStudiesClient;
import uk.ac.ebi.biostudy.submission.bsclient.BioStudiesClientException;
import uk.ac.ebi.biostudy.submission.rest.user.UserSession;

import java.io.IOException;
import java.net.URI;
import java.util.Date;

public class SubmissionService {

    private final BioStudiesClient bsclient;

    public SubmissionService(URI bsServerUrl) {
        this.bsclient = new BioStudiesClient(bsServerUrl);
    }

    public void saveSubmission(UserSession userSession, JSONObject obj) throws IOException, BioStudiesClientException {
        String accno = "";
        if (obj.has("accno")) {
            accno = obj.getString("accno");
        }
        if (accno.equals("!{S-STA}") || accno.isEmpty()) {
            accno = "TEMP-" + (new Date().getTime());
            obj.put("accno", accno);
        }
        bsclient.saveTmpSubmission(obj, accno, userSession.getSessid());
    }

    public void deleteTmpSubmission(final String acc, final UserSession userSession) throws IOException, BioStudiesClientException {
        bsclient.deleteTmpSubmission(acc, userSession.getSessid());
    }

    public void deleteSubmission(final String acc, final UserSession userSession)
            throws BioStudiesClientException, IOException {
        bsclient.deleteSubmission(acc, userSession.getSessid());
    }

    public JSONObject listSubmissions(UserSession userSession) throws BioStudiesClientException, IOException {
        JSONArray submitted = bsclient.getSubmissions(userSession.getSessid());
        JSONArray temporary = listTmpSubmissions(userSession);
        JSONObject obj = new JSONObject();
        obj.put("submissions", join(submitted, temporary));
        return obj;
    }

    private JSONArray listTmpSubmissions(UserSession userSession) throws IOException, BioStudiesClientException {
        JSONArray submissions = bsclient.listTmpSubmissions(userSession.getSessid());
        JSONArray array = new JSONArray();
        for (int j = 0; j < submissions.length(); j++) {
            JSONObject o = (JSONObject) submissions.get(j);
            JSONArray attrs = o.getJSONArray("attributes");
            for (int i = 0; i < attrs.length(); i++) {
                JSONObject attr = attrs.getJSONObject(i);
                if (attr.getString("name").equals("Title")) {
                    o.put("title", attr.getString("value"));
                }
                if (attr.getString("name").equals("ReleaseDate")) {
                    String sdate = attr.getString("value");

                    // formatDate.parse(sdate).getTime();
                    // Date drdate = new Date(new Long(sdate));

                    // o.put("rtime", attr.getString("value"));
                }
            }
            array.put(o);
        }
        return array;
    }

    public JSONObject getSubmission(final UserSession userSession, final String acc)
            throws BioStudiesClientException, IOException {
        return bsclient.getSubmission(acc, userSession.getSessid());
    }

    public JSONObject createSubmission(UserSession userSession, JSONObject obj)
            throws IOException, BioStudiesClientException {
        String acc = obj.getJSONArray("submissions").getJSONObject(0).getString("accno");
        JSONObject result = bsclient.createSubmission(obj, userSession.getSessid());
        deleteTmpSubmission(acc, userSession);
        return result;
    }

    public JSONObject updateSubmission(UserSession userSession, JSONObject obj)
            throws BioStudiesClientException, IOException {
        return bsclient.updateSubmission(obj, userSession.getSessid());
    }

    public JSONObject getFilesDir(UserSession userSession) throws BioStudiesClientException, IOException {
        return bsclient.getFilesDir(userSession.getSessid());
    }

    public JSONObject deleteFile(UserSession userSession, String file)
            throws BioStudiesClientException, IOException {
        return bsclient.deleteFile(file, userSession.getSessid());
    }

    public JSONObject singOut(UserSession userSession) throws BioStudiesClientException, IOException {
        return bsclient.signOut(userSession.getSessid(), userSession.getUsername());
    }

    public JSONObject singUp(JSONObject obj) throws BioStudiesClientException, IOException {
        return bsclient.signUp(obj);
    }

    public JSONObject singIn(JSONObject obj) throws BioStudiesClientException, IOException {
        return bsclient.signIn(obj);
    }

    private static JSONArray join(JSONArray... arrs) {
        JSONArray result = new JSONArray();
        for (JSONArray arr : arrs) {
            for (int i = 0; i < arr.length(); i++) {
                result.put(arr.get(i));
            }
        }
        return result;
    }
}
