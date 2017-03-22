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

import org.json.JSONObject;
import org.junit.Assume;
import org.junit.Test;
import uk.ac.ebi.biostudy.submission.TestEnvironment;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;

import static org.junit.Assert.assertNotNull;

/**
 * @author Olga Melnichuk
 */
public class BioStudiesClientTest {

    @Test
    public void authTest() throws URISyntaxException, IOException, BioStudiesClientException {
        if (!TestEnvironment.hasValidServerUrl()) {
            return;
        }

        URI uri = getServerUrl();
        if (! isReachable(uri.toURL())) {
            return;
        }
        BioStudiesClient bsclient = new BioStudiesRestClient(uri);
        JSONObject obj = bsclient.signIn(new JSONObject().put("login", "demo").put("password", "demo"));
        assertNotNull(obj);

        String sessionId = obj.getString("sessid");
        String submissions = bsclient.getSubmissions(sessionId, 0, 10, new HashMap<>());
        assertNotNull(submissions);
    }

    private static URI getServerUrl() throws IOException {
        return TestEnvironment.getServerUrl();
    }

    private static boolean isReachable(URL url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int respCode = connection.getResponseCode();
            return respCode >= 200 && respCode < 400;
        } catch (IOException e) {
            //e.printStackTrace();
            return false;
        }
    }
}
