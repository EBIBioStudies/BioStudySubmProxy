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

import org.json.JSONObject;
import uk.ac.ebi.biostudy.submission.bsclient.BioStudiesClient;
import uk.ac.ebi.biostudy.submission.bsclient.BioStudiesClientException;
import uk.ac.ebi.biostudy.submission.rest.data.UserSession;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import static uk.ac.ebi.biostudy.submission.rest.data.Submission.*;
import static uk.ac.ebi.biostudy.submission.rest.data.SubmissionList.*;

public class SubmissionService {

    private final BioStudiesClient bsclient;

    public SubmissionService(URI bsServerUrl) {
        this.bsclient = new BioStudiesClient(bsServerUrl);
    }

    public JSONObject getSubmission(final UserSession userSession, final String accno)
            throws BioStudiesClientException, IOException {
        JSONObject obj = bsclient.getTmpSubmission(accno, userSession.getSessid());
        if (obj != null) {
            return obj;
        }
        return wrap(bsclient.getSubmission(accno, userSession.getSessid()));
    }

    public JSONObject createSubmission(UserSession userSession, JSONObject obj) throws IOException, BioStudiesClientException {
        JSONObject sbm = wrap(obj);
        saveSubmission(userSession, sbm);
        return sbm;
    }

    public JSONObject editSubmission(final UserSession userSession, final String accno)
            throws BioStudiesClientException, IOException {
        JSONObject sbm = getSubmission(userSession, accno);
        JSONObject tmp = bsclient.getTmpSubmission(sbm.getString("accno"), userSession.getSessid());
        if (tmp == null) {
            saveSubmission(userSession, sbm);
        }
        return sbm;
    }

    public void saveSubmission(UserSession userSession, JSONObject obj) throws IOException, BioStudiesClientException {
        data(obj); // data exist?
        bsclient.saveTmpSubmission(modified(obj), accno(obj), userSession.getSessid());
    }

    public JSONObject submitSubmission(UserSession userSession, JSONObject obj) throws IOException, BioStudiesClientException {
        String acc = accno(obj);
        JSONObject sbm = data(obj);
        JSONObject result = isGeneratedAccession(acc) ?
                bsclient.submitNew(sbm, userSession.getSessid()) :
                bsclient.submitUpdated(sbm, userSession.getSessid());

        String status = result.getString("status");
        if (status.equals("OK")) {
            deleteSubmission(acc, userSession);
        }
        return result;
    }

    public boolean deleteSubmission(final String acc, final UserSession userSession)
            throws BioStudiesClientException, IOException {
        JSONObject sbm = bsclient.getTmpSubmission(acc, userSession.getSessid());
        if (sbm != null) {
            bsclient.deleteTmpSubmission(acc, userSession.getSessid());
            return true;
        }
        sbm = bsclient.getSubmission(acc, userSession.getSessid());
        return sbm == null || bsclient.deleteSubmission(acc, userSession.getSessid());
    }

    public JSONObject listSubmissions(UserSession userSession) throws BioStudiesClientException, IOException {
        List<JSONObject> submitted = transformSubmitted(bsclient.getSubmissions(userSession.getSessid()));
        List<JSONObject> temporary = transformTemporary(bsclient.listTmpSubmissions(userSession.getSessid()));
        JSONObject obj = new JSONObject();
        obj.put("submissions", merge(temporary, submitted));
        return obj;
    }

    public JSONObject getFilesDir(UserSession userSession) throws BioStudiesClientException, IOException {
        return bsclient.getFilesDir(userSession.getSessid());
    }

    public JSONObject deleteFile(UserSession userSession, String file)
            throws BioStudiesClientException, IOException {
        return bsclient.deleteFile(file, userSession.getSessid());
    }

    public JSONObject singOut(UserSession userSession) throws BioStudiesClientException, IOException {
        return bsclient.signOut(userSession.getSessid());
    }

    public JSONObject singUp(JSONObject obj) throws BioStudiesClientException, IOException {
        return bsclient.signUp(obj);
    }

    public JSONObject singIn(JSONObject obj) throws BioStudiesClientException, IOException {
        return bsclient.signIn(obj);
    }
}
