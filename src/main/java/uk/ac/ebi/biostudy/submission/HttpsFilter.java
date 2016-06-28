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

@WebFilter("/*")
public class HttpsFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(HttpsFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        logger.info("javax.servlet.forward.request_uri='" + request.getAttribute("javax.servlet.forward.request_uri") + "'");

        logger.info("X-Forwarded-Proto='" + httpRequest.getHeader("X-Forwarded-Proto") + "'");

        chain.doFilter(request, response);

        /*if ("https".equals(xfp)) {
            httpResponse.setHeader("Strict-Transport-Security", "max-age=60");

            chain.doFilter(request, response);
        } else if ("http".equals(xfp)) {
            try {
                URI uri1 = new URI(httpRequest.getRequestURL().toString());

                if (uri1.getPort() >= 0) {
                    throw new ServletException(format("Only standard ports are supported (given %s)", uri1.getPort()));
                }

                URI uri2 = new URI("https",
                        uri1.getUserInfo(),
                        uri1.getHost(),
                                   *//* port: *//* -1,
                        uri1.getPath(),
                        httpRequest.getQueryString(),
                                   *//* fragment: *//* null);

                httpResponse.sendRedirect(uri2.toString());
            } catch (URISyntaxException e) {
                throw new ServletException("Something went wrong with the URIs", e);
            }
        } else {
            throw new ServletException(format("Unsupported value for X-Forwarded-Proto: %s", xfp));
        }*/
    }
}