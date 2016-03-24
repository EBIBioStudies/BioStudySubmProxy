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
import org.mapdb.DB;
import uk.ac.ebi.biostudy.submission.bsclient.BioStudiesClient;
import uk.ac.ebi.biostudy.submission.bsclient.BioStudiesClientException;
import uk.ac.ebi.biostudy.submission.rest.user.UserSession;

import java.io.IOException;
import java.net.URI;

public class SubmissionService {

    private final TemporaryData temporary;
    private final BioStudiesClient bsclient;

    public SubmissionService(URI bsServerUrl, DB db) {
        this.bsclient = new BioStudiesClient(bsServerUrl);
        this.temporary = new TemporaryData(db);
    }

    public void saveSubmission(UserSession userSession, JSONObject obj) {
        temporary.saveSubmission(userSession.getUsername(), obj);
    }

    public void deleteSubmission(final String acc, final UserSession userSession) {
        temporary.deleteSubmission(acc, userSession.getUsername());
    }

    public void deleteSubmittedSubmission(final String acc, final UserSession userSession)
            throws BioStudiesClientException, IOException {
        bsclient.deleteSubmission(acc, userSession.getSessid());
    }

    public JSONArray listSubmissions(UserSession userSession) throws BioStudiesClientException, IOException {
        JSONArray submitted = bsclient.getSubmissions(userSession.getSessid());
        JSONArray temporary = this.temporary.listSubmissions(userSession.getUsername());
        return join(submitted, temporary);
    }

    public JSONObject getSubmission(final UserSession userSession, final String acc)
            throws BioStudiesClientException, IOException {
        return bsclient.getSubmission(acc, userSession.getSessid());
    }

    public JSONObject createSubmission(UserSession userSession, JSONObject obj)
            throws IOException, BioStudiesClientException {
        String acc = obj.getJSONArray("submissions").getJSONObject(0).getString("accno");
        JSONObject result = bsclient.createSubmission(obj, userSession.getSessid());
        temporary.deleteSubmission(acc, userSession.getUsername());
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

    public JSONObject singUp(JSONObject signUpReq) throws BioStudiesClientException, IOException {
        return bsclient.signUp(signUpReq);
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
