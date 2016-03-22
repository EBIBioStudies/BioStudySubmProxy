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

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

/**
 * @author Olga Melnichuk
 */
public class MyProxy {

    private static final Logger logger = LoggerFactory.getLogger(MyProxy.class);

    private static final String STRING_LOCATION_HEADER = "Location";

    private static final String STRING_CONTENT_LENGTH_HEADER_NAME = "Content-Length";


    private final String host;
    private final int port;
    private final String context;
    private final String protocol;

    public MyProxy(URL serverUrl) {
        protocol = serverUrl.getProtocol();
        host = serverUrl.getHost();
        port = serverUrl.getPort();
        context = serverUrl.getPath();
    }

    public void executeMethod(RequestTransform transform, HttpServletRequest req,
                              HttpServletResponse resp) throws ServletException, IOException {
        HttpMethod httpMethod;
        try {
            httpMethod = transform.apply(req);
        } catch (BadRequestException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        httpMethod.setURI(getRequestUrl(httpMethod.getURI()));

        HttpClient httpClient = new HttpClient();
        httpMethod.setFollowRedirects(httpMethod instanceof GetMethod);

        int retCode = httpClient.executeMethod(httpMethod);
        logger.debug("received retCode: " + retCode);

        if (retCode >= HttpServletResponse.SC_MULTIPLE_CHOICES /* 300 */
                && retCode < HttpServletResponse.SC_NOT_MODIFIED /* 304 */) {
            redirect(req, resp, getLocationOrFail(httpMethod));
            return;
        } else if (retCode == HttpServletResponse.SC_NOT_MODIFIED) {
            // 304 needs special handling.  See:
            // http://www.ics.uci.edu/pub/ietf/http/rfc1945.html#Code304
            // We get a 304 whenever passed an 'If-Modified-Since'
            // header and the data on disk has not changed; server
            // responds w/ a 304 saying I'm not going to send the
            // body because the file has not changed.
            resp.setIntHeader(STRING_CONTENT_LENGTH_HEADER_NAME, 0);
            resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }

        resp.setStatus(retCode);

        // Pass response headers back to the client
        Header[] headerArrayResponse = httpMethod.getResponseHeaders();
        for (Header header : headerArrayResponse) {
            if (header.getName().equals("Transfer-Encoding") &&
                    header.getValue().equals("chunked"))
                continue;
            resp.setHeader(header.getName(), header.getValue());
        }

        // Send the content to the client
        copyResponseContent(resp, httpMethod);
        httpMethod.releaseConnection();
    }

    private static void copyResponseContent(HttpServletResponse resp, HttpMethod httpMethod) throws IOException {
        BufferedInputStream bufferedInput = new BufferedInputStream(httpMethod.getResponseBodyAsStream());
        OutputStream output = resp.getOutputStream();
        int intNextByte;
        while ((intNextByte = bufferedInput.read()) != -1) {
            output.write(intNextByte);
        }
    }

    private void redirect(HttpServletRequest req, HttpServletResponse resp, String location) throws IOException {
        resp.sendRedirect(location.replace(getDestContextUrl(), getReqContextUrl(req)));
    }

    private String getLocationOrFail(HttpMethod httpMethod) throws ServletException {
        String location = httpMethod.getResponseHeader(STRING_LOCATION_HEADER).getValue();
        if (location == null) {
            throw new ServletException("No " + STRING_LOCATION_HEADER + " header was found in the response");
        }
        return location;
    }

    private String getReqContextUrl(HttpServletRequest req) {
        return getContextUrl(req.getProtocol(), req.getServerName(), req.getServerPort(), req.getContextPath());
    }

    private String getDestContextUrl() {
        return getContextUrl(protocol, host, port, context);
    }

    private String getContextUrl(String protocol, String host, int port, String context) {
        return protocol.toLowerCase() + "://" + getHostAndPort(host, port) + context;
    }

    private String getHostAndPort(String host, int port) {
        return host + (port == 80 ? "" : ":" + port);
    }

    private URI getRequestUrl(URI uri) throws URIException {
        return new URI(protocol, "", host, port, uri.getPathQuery(), uri.getFragment());
    }

}
