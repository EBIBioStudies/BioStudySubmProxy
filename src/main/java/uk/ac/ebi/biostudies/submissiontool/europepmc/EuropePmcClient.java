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

package uk.ac.ebi.biostudies.submissiontool.europepmc;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class EuropePmcClient {

    private static final Logger logger = LoggerFactory.getLogger(EuropePmcClient.class);

    public String pubMedSearch(String id) throws IOException {
        HttpUriRequest req = RequestBuilder.get().setUri("http://www.ebi.ac.uk/europepmc/webservices/rest/search")
                .addParameter("query", "ext_id:" + id)
                .addParameter("format", "json")
                .build();

        CloseableHttpClient client = HttpClients.createDefault();
        try (CloseableHttpResponse response = client.execute(req)) {
            String body = EntityUtils.toString(response.getEntity(), "UTF-8");
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                return body;
            }

            logger.error("EuropePMC request(" + req.getURI() + ") failed with status: " + statusCode);
            throw new IOException("EropwPMC request failed with status: " + statusCode);
        }

    }

}
