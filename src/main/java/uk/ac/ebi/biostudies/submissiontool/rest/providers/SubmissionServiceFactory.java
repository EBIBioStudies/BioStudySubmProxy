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

package uk.ac.ebi.biostudies.submissiontool.rest.providers;

import org.glassfish.hk2.api.Factory;
import uk.ac.ebi.biostudies.submissiontool.context.AppContext;
import uk.ac.ebi.biostudies.submissiontool.rest.resources.SubmissionService;

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
        this.service = new SubmissionService(AppContext.getBioStudiesClient(context));
    }

    @Override
    public SubmissionService provide() {
         return service;
    }

    @Override
    public void dispose(SubmissionService instance) {
    }
}
