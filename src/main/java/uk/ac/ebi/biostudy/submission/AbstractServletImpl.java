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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import uk.ac.ebi.biostudy.submission.exceptions.AuthException;

public class AbstractServletImpl extends HttpServlet {

	private static final long serialVersionUID = 1L;
	protected String BS_SERVER_URL;
	protected Properties properties = new Properties();

	@Override
	public void init() throws ServletException {
		System.out.println("Servlet started");
		super.init();
		try {
			properties.load(getServletContext().getResourceAsStream("/WEB-INF/classes/config.properties"));
			BS_SERVER_URL = properties.getProperty("BS_SERVER_URL");

		} catch (IOException e) {
			throw new ServletException("Problem to load properties from ");
		}
	}

	@Override
	public void destroy() {
		super.destroy();
	}

	protected String readRequestBody(HttpServletRequest request) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = request.getReader();
		String str;
		while ((str = br.readLine()) != null) {
			sb.append(str);
		}
		return sb.toString();
	}

	protected void isSignedIn(HttpSession session) throws AuthException {
		if (session == null) {
			throw new AuthException("Auth error");
		} else {
			UserSession userSession = getUserSession(session);
			if (userSession == null) {
				throw new AuthException("Auth error");
			}
		}
	}

	protected UserSession getUserSession(HttpSession session) {
		return (UserSession) session.getAttribute("userSession");

	}
}
