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
import org.junit.Test;

import org.junit.Assume;

import uk.ac.ebi.biostudy.submission.MyConfig;
import uk.ac.ebi.biostudy.submission.TestEnvironment;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * TODO: make it a proper test
 * @author Olga Melnichuk
 */
public class BioStudiesClientTest {

    @Test
    public void test() throws URISyntaxException, IOException, BioStudiesClientException {
        Assume.assumeTrue(TestEnvironment.hasValidServerUrl());

        BioStudiesClient bsclient = new BioStudiesClient(getServerUrl());
        JSONObject obj = bsclient.signIn("demo", "demo");
        System.out.println(obj);

        String sessionId = obj.getString("sessid");
        JSONArray submissions = bsclient.getSubmissions(sessionId);
        System.out.println(submissions);


        JSONObject submission = bsclient.getSubmission("S-STA2", sessionId);
        System.out.println(submission);

        JSONObject filesDir = bsclient.getFilesDir(sessionId);
        System.out.println(filesDir);
    }

    private static URI getServerUrl() throws IOException {
        return TestEnvironment.getServerUrl();
    }
}
