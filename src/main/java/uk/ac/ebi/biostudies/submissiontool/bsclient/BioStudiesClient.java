/*
 * Copyright (c) 2017 European Molecular Biology Laboratory
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

package uk.ac.ebi.biostudies.submissiontool.bsclient;

import rx.Observable;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

public interface BioStudiesClient extends Closeable {
    String getModifiedSubmission(String accno, String sessid) throws IOException, BioStudiesClientException;

    String getSubmission(String accno, String sessid) throws BioStudiesClientException, IOException;

    String saveModifiedSubmission(String modified, String accno, String sessid) throws IOException, BioStudiesClientException;

    String submitNew(String sbm, String sessid) throws BioStudiesClientException, IOException;

    String submitUpdated(String sbm, String sessid) throws BioStudiesClientException, IOException;

    String deleteModifiedSubmission(String acc, String sessid) throws IOException, BioStudiesClientException;

    String deleteSubmission(String acc, String sessid) throws BioStudiesClientException, IOException;

    String getSubmissions(String sessid, int offset, int limit, Map<String, String> paramMap) throws BioStudiesClientException, IOException;

    String getModifiedSubmissions(String sessid) throws IOException, BioStudiesClientException;

    String getProjects(String sessid) throws BioStudiesClientException, IOException;

    String getFilesDir(String path, int depth, boolean showArchive, String sessid) throws BioStudiesClientException, IOException;

    String deleteFile(String file, String sessid) throws BioStudiesClientException, IOException;

    String signOut(String obj, String sessid) throws BioStudiesClientException, IOException;

    String signUp(String obj) throws BioStudiesClientException, IOException;

    String signIn(String obj) throws BioStudiesClientException, IOException;

    String passwordResetRequest(String obj) throws BioStudiesClientException, IOException;

    String passwordReset(String obj) throws BioStudiesClientException, IOException;

    String resendActivationLink(String obj) throws BioStudiesClientException, IOException;

    String activate(String key) throws BioStudiesClientException, IOException;

    Observable<String> getSubmissionsRx(String sessid, int offset, int limit, Map<String, String> paramMap);

    Observable<String> getSubmissionRx(String accno, String sessid);

    Observable<String> deleteSubmissionRx(String acc, String sessid);

    Observable<String> getModifiedSubmissionsRx(String sessid);

    Observable<String> getModifiedSubmissionRx(String accno, String sessid);

    Observable<String> deleteModifiedSubmissionRx(String acc, String sessid);

    Observable<String> saveModifiedSubmissionRx(String modified, String accno, String sessid);

    Observable<String> getProjectsRx(String sessid);

    Observable<String> submitNewRx(String sbm, String sessid);

    Observable<String> submitUpdatedRx(String sbm, String sessid);

    Observable<String> getFilesDirRx(String path, int depth, boolean showArchive, String sessid);

    Observable<String> deleteFileRx(String file, String sessid);

    Observable<String> signUpRx(String obj);

    Observable<String> passwordResetRequestRx(String obj);

    Observable<String> passwordResetRx(String obj);

    Observable<String> resendActivationLinkRx(String obj);

    Observable<String> activateRx(String key);

    Observable<String> signOutRx(String obj, String sessid);

}
