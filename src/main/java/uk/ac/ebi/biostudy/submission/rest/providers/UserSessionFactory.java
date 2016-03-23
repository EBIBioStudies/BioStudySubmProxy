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
import uk.ac.ebi.biostudy.submission.rest.user.UserSession;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.ext.Provider;

/**
 * @author Olga Melnichuk
 */
@Provider
public class UserSessionFactory implements Factory<UserSession> {

    private final HttpServletRequest request;

    @Inject
    public UserSessionFactory(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public UserSession provide() {
        //TODO
        return (UserSession) request.getSession().getAttribute("userSession");
    }

    @Override
    public void dispose(UserSession instance) {

    }
}
