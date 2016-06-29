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

package uk.ac.ebi.biostudy.submission.rest;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.biostudy.submission.AppContext;
import uk.ac.ebi.biostudy.submission.rest.providers.AuthenticationFilter;
import uk.ac.ebi.biostudy.submission.rest.providers.BioStudiesClientExceptionMapper;
import uk.ac.ebi.biostudy.submission.rest.providers.SubmissionServiceFactory;
import uk.ac.ebi.biostudy.submission.rest.providers.UserSessionFactory;
import uk.ac.ebi.biostudy.submission.rest.resources.SubmissionService;
import uk.ac.ebi.biostudy.submission.rest.data.UserSession;

import javax.inject.Singleton;

/**
 * @author Olga Melnichuk
 */
public class RESTApp extends ResourceConfig {
    public RESTApp() {
        packages("uk.ac.ebi.biostudy.submission.rest");
        register(LoggingFilter.class);
        register(AuthenticationFilter.class);
        register(BioStudiesClientExceptionMapper.class);
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bindFactory(SubmissionServiceFactory.class).to(SubmissionService.class).in(Singleton.class);
                bindFactory(UserSessionFactory.class).to(UserSession.class).in(RequestScoped.class);
            }
        });
    }
}
