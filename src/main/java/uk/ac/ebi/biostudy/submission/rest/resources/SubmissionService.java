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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.biostudy.submission.bsclient.BioStudiesClient;
import uk.ac.ebi.biostudy.submission.bsclient.BioStudiesClientException;
import uk.ac.ebi.biostudy.submission.europepmc.EuropePmcClient;
import uk.ac.ebi.biostudy.submission.rest.data.UserSession;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.ac.ebi.biostudy.submission.rest.data.Submission.*;
import static uk.ac.ebi.biostudy.submission.rest.data.SubmissionList.*;

public class SubmissionService {

    private static final Logger logger = LoggerFactory.getLogger(SubmissionService.class);

    private final BioStudiesClient bsclient;
    private final EuropePmcClient europePmc;

    private static final Map<String, String> europePmcAttributes = new HashMap<String, String>() {
        {
            put("title", "title");
            put("authorString", "authors");
            put("pubType", "type");
            put("issue", "issue");
            put("journalIssn", "issn");
            put("pubYear", "year");
            put("journalVolume", "volume");
        }
    };

    public SubmissionService(URI bsServerUrl) {
        this.bsclient = new BioStudiesClient(bsServerUrl);
        this.europePmc = new EuropePmcClient();
    }

    public JSONObject getSubmission(final UserSession userSession, final String accno, boolean origin)
            throws BioStudiesClientException, IOException {
        logger.debug("getSubmission(userSession={}, accno={}, origin={})", userSession, accno, origin);
        if (!origin) {
            JSONObject obj = bsclient.getTmpSubmission(accno, userSession.getSessid());
            if (obj != null) {
                return obj;
            }
        }
        return wrap(bsclient.getSubmission(accno, userSession.getSessid()));
    }

    public JSONObject createSubmission(UserSession userSession, JSONObject obj) throws IOException, BioStudiesClientException {
        logger.debug("createSubmission(userSession={}, obj={})", userSession, obj);
        JSONObject sbm = wrap(obj);
        saveSubmission(userSession, sbm);
        return sbm;
    }

    public JSONObject editSubmission(final UserSession userSession, final String accno)
            throws BioStudiesClientException, IOException {
        logger.debug("editSubmission(userSession={}, accno={})", userSession, accno);
        JSONObject sbm = bsclient.getTmpSubmission(accno, userSession.getSessid());
        if (sbm == null) {
            logger.debug("no temporary copy; creating one...");
            sbm = getSubmission(userSession, accno, true);
            saveSubmission(userSession, sbm);
        }
        return sbm;
    }

    public void saveSubmission(UserSession userSession, JSONObject obj) throws IOException, BioStudiesClientException {
        logger.debug("saveSubmission(userSession={}, obj={})", userSession, obj);
        data(obj); // data exist?
        bsclient.saveTmpSubmission(modified(obj), accno(obj), userSession.getSessid());
    }

    public JSONObject submitSubmission(UserSession userSession, JSONObject obj) throws IOException, BioStudiesClientException {
        logger.debug("submitSubmission(userSession={}, obj={})", userSession, obj);
        String acc = accno(obj);
        JSONObject sbm = data(obj);
        JSONObject result = isGeneratedAccession(acc) ?
                bsclient.submitNew(sbm, userSession.getSessid()) :
                bsclient.submitUpdated(sbm, userSession.getSessid());

        String status = result.getString("status");
        if (status.equals("OK")) {
            deleteSubmission(userSession, acc);
        }
        return result;
    }

    public boolean deleteSubmission(final UserSession userSession, final String acc)
            throws BioStudiesClientException, IOException {
        logger.debug("deleteSubmission(userSession={}, acc={})", userSession, acc);
        JSONObject sbm = bsclient.getTmpSubmission(acc, userSession.getSessid());
        if (sbm != null) {
            bsclient.deleteTmpSubmission(acc, userSession.getSessid());
            return true;
        }
        sbm = bsclient.getSubmission(acc, userSession.getSessid());
        return sbm == null || bsclient.deleteSubmission(acc, userSession.getSessid());
    }

    public JSONObject listSubmissions(UserSession userSession) throws BioStudiesClientException, IOException {
        logger.debug("listSubmissions(userSession={})", userSession);
        List<JSONObject> submitted = transformSubmitted(bsclient.getSubmissions(userSession.getSessid()));
        logger.debug("transformed submitted: {}", submitted);
        List<JSONObject> temporary = transformTemporary(bsclient.listTmpSubmissions(userSession.getSessid()));
        logger.debug("transformed temporary: {}", temporary);
        JSONObject obj = new JSONObject();
        obj.put("submissions", merge(temporary, submitted));
        logger.debug("listSubmissions(): result={}", obj);
        return obj;
    }

    public JSONObject getFilesDir(UserSession userSession) throws BioStudiesClientException, IOException {
        logger.debug("getFilesDir(userSession={})", userSession);
        return bsclient.getFilesDir(userSession.getSessid());
    }

    public JSONObject deleteFile(UserSession userSession, String file)
            throws BioStudiesClientException, IOException {
        logger.debug("deleteFile(userSession={}, file={})", userSession, file);
        return bsclient.deleteFile(file, userSession.getSessid());
    }

    public JSONObject singOut(UserSession userSession) throws BioStudiesClientException, IOException {
        logger.debug("signOut(userSession={})", userSession);
        return bsclient.signOut(userSession.getSessid());
    }

    public JSONObject singUp(JSONObject obj) throws BioStudiesClientException, IOException {
        logger.debug("signUp(obj={})", obj);
        return bsclient.signUp(obj);
    }

    public JSONObject singIn(JSONObject obj) throws BioStudiesClientException, IOException {
        return bsclient.signIn(obj);
    }

    public JSONObject passwordResetRequest(JSONObject obj) throws BioStudiesClientException, IOException {
        logger.debug("passwordResetRequest(obj={})", obj);
        return bsclient.passwordResetRequest(obj);
    }

    public JSONObject pubMedSearch(String id) {
        logger.debug("pubMedSearch(id={})", id);
        try {
            JSONObject res = europePmc.pubMedSearch(id);

            int hitCount = res.getInt("hitCount");
            JSONObject data = new JSONObject();

            if (hitCount >= 1) {
                final JSONObject publ = res.getJSONObject("resultList").getJSONArray("result").getJSONObject(0);
                europePmcAttributes.entrySet().stream().forEach(entry -> {
                            if (publ.has(entry.getKey())) {
                                data.put(entry.getValue(), publ.getString(entry.getKey()));
                            }
                        }
                );
            }
            JSONObject obj = new JSONObject();
            obj.put("status", "OK");
            obj.put("data", data);
            return obj;
        } catch (IOException e) {
            logger.warn("EuropePMC call for id={} failed: {}", id, e);
            return fail("EuropePMC error");
        }
    }

    private static JSONObject fail(String message) {
        return new JSONObject().put("status", "FAIL").put("message", message);
    }
}
