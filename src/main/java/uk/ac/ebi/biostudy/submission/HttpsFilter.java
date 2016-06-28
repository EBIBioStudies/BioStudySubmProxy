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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static java.lang.Boolean.parseBoolean;

@WebFilter("/*")
public class HttpsFilter implements Filter {

    private static final String FILTER_DISABLED = "uk.ac.ebi.biostudy.submission.HTTPS_FILTER_DISABLED";

    private static final Logger logger = LoggerFactory.getLogger(HttpsFilter.class);

    private boolean enabled = true;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String disabled = System.getProperty(FILTER_DISABLED);
        if (disabled != null) {
            logger.info(FILTER_DISABLED + ": " + disabled);
            enabled = !parseBoolean(disabled);
        }
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (!enabled) {
            chain.doFilter(request, response);
            return;
        }

        String xfp = httpRequest.getHeader("X-Forwarded-Proto");
        if ("https".equals(xfp)) {
            httpResponse.setHeader("Strict-Transport-Security", "max-age=60");
            chain.doFilter(request, response);
        } else {
            String url = getUrl(httpRequest);
            logger.info("http url: " + url);
            httpResponse.sendRedirect(url.replace("http", "https"));
        }
    }

    private static String getUrl(HttpServletRequest request) {
        StringBuffer requestURL = request.getRequestURL();
        String queryString = request.getQueryString();

        if (queryString != null) {
            requestURL.append('?').append(queryString);
        }
        return requestURL.toString();
    }
}