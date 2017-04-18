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

import org.glassfish.jersey.client.rx.rxjava.RxObservable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

public class EuropePmcClient {

    private static final Logger logger = LoggerFactory.getLogger(EuropePmcClient.class);

    private final Client rsClient;

    public EuropePmcClient() {
        this.rsClient = ClientBuilder.newClient();
    }

    public String pubMedSearch(String id) throws EuropePMCClientException, IOException {
        Invocation.Builder builder = createTarget(id).request(MediaType.APPLICATION_JSON_TYPE);
        Response resp = null;
        try {
            resp = builder.get();
            return readResponse(resp);
        } catch (ProcessingException e) {
            throw new IOException(e);
        } finally {
            if (resp != null)
                resp.close();
        }
    }

    public Observable<String> pubMedSearchRx(String id) {
        return RxObservable.from(createTarget(id))
                .request()
                .rx()
                .get()
                .map(EuropePmcClient::readResponse);
    }

    private WebTarget createTarget(String id) {
        return rsClient.target("http://www.ebi.ac.uk/europepmc/webservices/rest/search")
                .queryParam("query", "ext_id:" + id)
                .queryParam("format", "json");

    }

    private static String readResponse(Response resp) {
        int statusCode = resp.getStatus();
        String body = resp.readEntity(String.class);
        if (statusCode == 200) {
            return body;
        }

        logger.error("EuropePMC failed with status: " + statusCode);
        throw new EuropePMCClientException("EropwPMC request failed with status: " + statusCode);
    }
}
