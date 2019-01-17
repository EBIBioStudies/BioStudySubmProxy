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

    String signIn(String obj) throws BioStudiesClientException, IOException;

    Observable<String> getSubmissionRx(String accno, String sessid);

    Observable<String> deleteSubmissionRx(String acc, String sessid);

    Observable<String> createPendingSubmissionRx(String pageTab, String sessid);

    Observable<String> getPendingSubmissionRx(String accno, String sessid);

    Observable<String> deletePendingSubmissionRx(String acc, String sessid);

    Observable<String> savePendingSubmissionRx(String pending, String accno, String sessid);

    Observable<String> submitPendingSubmissionRx(String pending, String accno, String sessid);

    Observable<String> directSubmitRx(boolean create, String pageTab, String id);

    Observable<String> signUpRx(String obj);

    Observable<String> passwordResetRequestRx(String obj);

    Observable<String> resendActivationLinkRx(String obj);

    Observable<String> signOutRx(String obj, String sessid);
}
