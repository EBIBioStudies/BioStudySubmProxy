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

import org.json.JSONArray;
import org.json.JSONObject;
import rx.Observable;

import java.io.Closeable;
import java.io.IOException;

public interface BioStudiesClient extends Closeable {
    JSONObject getModifiedSubmission(String accno, String sessid) throws IOException, BioStudiesClientException;

    JSONObject getSubmission(String accno, String sessid) throws BioStudiesClientException, IOException;

    void saveModifiedSubmission(JSONObject modified, String accno, String sessid) throws IOException, BioStudiesClientException;

    JSONObject submitNew(JSONObject sbm, String sessid) throws BioStudiesClientException, IOException;

    JSONObject submitUpdated(JSONObject sbm, String sessid) throws BioStudiesClientException, IOException;

    void deleteModifiedSubmission(String acc, String sessid) throws IOException, BioStudiesClientException;

    boolean deleteSubmission(String acc, String sessid) throws BioStudiesClientException, IOException;

    String getSubmissions(String sessid, int offset, int limit) throws BioStudiesClientException, IOException;

    JSONArray getModifiedSubmissions(String sessid) throws IOException, BioStudiesClientException;

    JSONObject getFilesDir(String sessid) throws BioStudiesClientException, IOException;

    JSONObject deleteFile(String file, String sessid) throws BioStudiesClientException, IOException;

    JSONObject signOut(String sessid) throws BioStudiesClientException, IOException;

    JSONObject signUp(JSONObject obj) throws BioStudiesClientException, IOException;

    JSONObject signIn(JSONObject obj) throws BioStudiesClientException, IOException;

    JSONObject passwordResetRequest(JSONObject obj) throws BioStudiesClientException, IOException;

    JSONObject resendActivationLink(JSONObject obj) throws BioStudiesClientException, IOException;
}
