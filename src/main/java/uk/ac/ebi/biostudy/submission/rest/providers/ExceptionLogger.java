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

import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionLogger implements ApplicationEventListener, RequestEventListener {

    private static final Logger log = LoggerFactory.getLogger(ExceptionLogger.class);

    @Override
    public void onEvent(final ApplicationEvent applicationEvent) {
    }

    @Override
    public RequestEventListener onRequest(final RequestEvent requestEvent) {
        return this;
    }

    @Override
    public void onEvent(RequestEvent paramRequestEvent) {
        if (paramRequestEvent.getType() == RequestEvent.Type.ON_EXCEPTION) {
            log.error("", paramRequestEvent.getException());
        }
    }
}