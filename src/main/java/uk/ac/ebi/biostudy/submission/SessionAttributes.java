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

package uk.ac.ebi.biostudy.submission;

import uk.ac.ebi.biostudy.submission.rest.data.UserSession;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author Olga Melnichuk
 */
public class SessionAttributes {

    public static final String USER_SESSION = "userSession";

    public static UserSession getUserSession(HttpServletRequest req) {
        return (UserSession) req.getSession().getAttribute(USER_SESSION);
    }

    public static void setUserSession(HttpServletRequest req, UserSession userSession) {
        HttpSession session = req.getSession(true);
        session.setAttribute("userSession", userSession);
    }
}
