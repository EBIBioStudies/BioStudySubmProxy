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

package uk.ac.ebi.biostudy.submission.rest.providers;

import org.glassfish.hk2.api.Factory;
import uk.ac.ebi.biostudy.submission.AppConfig;
import uk.ac.ebi.biostudy.submission.AppContext;
import uk.ac.ebi.biostudy.submission.bsclient.BioStudiesClient;
import uk.ac.ebi.biostudy.submission.bsclient.BioStudiesRestClient;
import uk.ac.ebi.biostudy.submission.rest.resources.SubmissionService;
import uk.ac.ebi.biostudy.submission.stubs.BioStudiesClientStub;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.ext.Provider;

/**
 * @author Olga Melnichuk
 */
@Provider
public class SubmissionServiceFactory implements Factory<SubmissionService> {

    private final SubmissionService service;

    @Inject
    public SubmissionServiceFactory(ServletContext context) {
        AppConfig config = AppContext.getConfig(context);
        BioStudiesClient bsclient = config.isOfflineModeOn() ?
                new BioStudiesClientStub(config.getUserDir()) :
                new BioStudiesRestClient(config.getServerUrl());
        this.service = new SubmissionService(bsclient);
    }

    @Override
    public SubmissionService provide() {
         return service;
    }

    @Override
    public void dispose(SubmissionService instance) {

    }
}
