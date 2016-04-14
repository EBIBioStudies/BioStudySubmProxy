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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.biostudy.submission.SessionAttributes;
import uk.ac.ebi.biostudy.submission.rest.user.UserSession;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Olga Melnichuk
 */
@Provider
public class AuthenticationFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    @Context
    private HttpServletRequest request;

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    private static final Response ACCESS_DENIED = Response.status(Response.Status.UNAUTHORIZED)
            .entity("You cannot access this resource").build();
    //private static final Response ACCESS_FORBIDDEN = Response.status(Response.Status.FORBIDDEN)
    //        .entity("Access blocked for all users !!").build();

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        Method method = resourceInfo.getResourceMethod();

        if (method.isAnnotationPresent(RolesAllowed.class)) {
            RolesAllowed rolesAnnotation = method.getAnnotation(RolesAllowed.class);
            Set<String> rolesSet = new HashSet<String>(Arrays.asList(rolesAnnotation.value()));

            UserSession session = getUserSession();

            if (!isUserAllowed(session, rolesSet)) {
                requestContext.abortWith(ACCESS_DENIED);
            }
        }
    }

    private UserSession getUserSession() {
        String sessid = getSessionId();
        logger.debug("sessionId=" + sessid);
        if (sessid == null) {
            return null;
        }

        UserSession session = SessionAttributes.getUserSession(request);
        if (session == null) {
            session = new UserSession(sessid);
            SessionAttributes.setUserSession(request, session);
        }
        return session;
    }

    private String getSessionId() {
        return request.getHeader("X-Session-Token");
    }

    private boolean isUserAllowed(final UserSession userSession, final Set<String> rolesSet) {
        return userSession != null;
    }

}
