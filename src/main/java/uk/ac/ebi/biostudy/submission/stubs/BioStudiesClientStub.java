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

package uk.ac.ebi.biostudy.submission.stubs;

import org.apache.http.entity.ContentType;
import org.json.JSONArray;
import org.json.JSONObject;
import rx.Observable;
import rx.exceptions.Exceptions;
import uk.ac.ebi.biostudy.submission.bsclient.BioStudiesClient;
import uk.ac.ebi.biostudy.submission.bsclient.BioStudiesClientException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import static uk.ac.ebi.biostudy.submission.rest.data.Submission.*;

public class BioStudiesClientStub implements BioStudiesClient {

    private final AtomicInteger counter = new AtomicInteger(0);
    private final ConcurrentMap<String, JSONObject> modified = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, JSONObject> submitted = new ConcurrentHashMap<>();
    private final UserDir userDir;

    public BioStudiesClientStub(Path path) {
        this.userDir = new UserDir(path);
    }

    @Override
    public void close() throws IOException {
        // nothing to close
    }

    @Override
    public JSONObject getModifiedSubmission(String accno, String sessid) throws IOException, BioStudiesClientException {
        checkSessionId(sessid);
        return modified.get(accno);
    }

    @Override
    public JSONObject getSubmission(String accno, String sessid) throws BioStudiesClientException, IOException {
        checkSessionId(sessid);
        return submitted.get(accno);
    }

    @Override
    public void saveModifiedSubmission(JSONObject sbm, String accno, String sessid) throws IOException, BioStudiesClientException {
        checkSessionId(sessid);
        modified.put(accno, sbm);
    }

    @Override
    public JSONObject submitNew(JSONObject sbm, String sessid) throws BioStudiesClientException, IOException {
        checkSessionId(sessid);
        String accno = "SBM-" + counter.addAndGet(1);
        sbm.put("accno", accno);
        submitted.put(accno, sbm);
        return new JSONObject()
                .put("status", "OK");
    }

    @Override
    public JSONObject submitUpdated(JSONObject sbm, String sessid) throws BioStudiesClientException, IOException {
        checkSessionId(sessid);
        submitted.put(sbm.getString("accno"), sbm);
        return new JSONObject()
                .put("status", "OK");
    }

    @Override
    public void deleteModifiedSubmission(String acc, String sessid) throws IOException, BioStudiesClientException {
        checkSessionId(sessid);
        modified.remove(acc);
    }

    @Override
    public boolean deleteSubmission(String acc, String sessid) throws BioStudiesClientException, IOException {
        checkSessionId(sessid);
        submitted.remove(acc);
        return true;
    }

    @Override
    public JSONArray getSubmissions(String sessid) throws BioStudiesClientException, IOException {
        checkSessionId(sessid);

        final JSONArray array = new JSONArray();
        submitted.values().stream().map(s ->
                new JSONObject()
                        .put("accno", accno(s))
                        .put("rtime", releaseDateAttributeInSeconds(s))
                        .put("ctime", "")
                        .put("mtime", "")
                        .put("title", titleAttribute(s))
                        .put("version", "1")
        ).forEach(array::put);
        return array;
    }

    @Override
    public JSONArray getModifiedSubmissions(String sessid) throws BioStudiesClientException, IOException {
        checkSessionId(sessid);

        final JSONArray array = new JSONArray();
        modified.values().forEach(array::put);
        return array;
    }

    @Override
    public Observable<JSONArray> getModifiedSubmissionsRx(String sessid) {
        return Observable.just(sessid)
                .map(id -> {
                    try {
                        return getModifiedSubmissions(id);
                    } catch (BioStudiesClientException | IOException e) {
                        throw Exceptions.propagate(e);
                    }
                });
    }

    @Override
    public Observable<JSONArray> getSubmissionsRx(String sessid) {
        return Observable.just(sessid)
                .map(id -> {
                    try {
                        return getSubmissions(id);
                    } catch (BioStudiesClientException | IOException e) {
                        throw Exceptions.propagate(e);
                    }
                });
    }

    @Override
    public JSONObject getFilesDir(String sessid) throws BioStudiesClientException, IOException {
        checkSessionId(sessid);

        return new JSONObject()
                .put("status", "OK")
                .put("files", userDir.list());
    }

    @Override
    public JSONObject deleteFile(String file, String sessid) throws BioStudiesClientException, IOException {
        return null;
    }

    @Override
    public JSONObject signOut(String sessid) throws BioStudiesClientException, IOException {
        return null;
    }

    @Override
    public JSONObject signUp(JSONObject obj) throws BioStudiesClientException, IOException {
        return null;
    }

    @Override
    public JSONObject signIn(JSONObject obj) throws BioStudiesClientException, IOException {
        return null;
    }

    @Override
    public JSONObject passwordResetRequest(JSONObject obj) throws BioStudiesClientException, IOException {
        return null;
    }

    private static void checkSessionId(String s) throws BioStudiesClientException {
        if (s == null || !s.equals(Session.ID)) {
            throw new BioStudiesClientException(401, ContentType.TEXT_PLAIN.toString(), "Unauthorized");
        }
    }

}
