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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

        ObjectMapper mapper = new ObjectMapper();

        URI uri = getServerUrl();
        if (! isReachable(uri.toURL())) {
            return;
        }
        BioStudiesClient bsclient = new BioStudiesRestClient(uri);
        String resp = bsclient.signIn(mapper.readTree("{\"login\":\"demo\", \"password\": \"demo\"}").toString());
        assertNotNull(resp);

        JsonNode respNode = mapper.readTree(resp);
        String sessionId = respNode.get("sessid").asText();
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
