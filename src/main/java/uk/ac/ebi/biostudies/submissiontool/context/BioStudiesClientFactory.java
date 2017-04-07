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

package uk.ac.ebi.biostudies.submissiontool.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.biostudies.submissiontool.bsclient.BioStudiesRestClient;
import uk.ac.ebi.biostudies.submissiontool.bsclient.BioStudiesClient;

import javax.servlet.ServletContext;
import java.io.IOException;

class BioStudiesClientFactory implements Factory<BioStudiesClient> {

    private static final Logger logger = LoggerFactory.getLogger(BioStudiesClientFactory.class);

    private BioStudiesClient bsClient;

    @Override
    public BioStudiesClient create(ServletContext context) {
        AppConfig config = AppContext.getConfig(context);
        //bsClient = config.isOfflineModeOn() ?
        //        new BioStudiesClientStub(config.getUserDir()) :
        return new BioStudiesRestClient(config.getServerUrl());
    }

    @Override
    public void destroy() {
        if (bsClient == null) {
            return;
        }
        try {
            logger.info("destroy()");
            bsClient.close();
        } catch (IOException e) {
            logger.error("Can't properly close biostudies client; see logs for details", e);
        }
    }
}
