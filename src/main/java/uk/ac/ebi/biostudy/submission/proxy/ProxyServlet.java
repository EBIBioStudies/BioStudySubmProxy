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

package uk.ac.ebi.biostudy.submission.proxy;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.biostudy.submission.MyConfig;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/api/*")
public class ProxyServlet extends HttpServlet {

    private static class Pair<K, V> {
        private K key;
        private V value;

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Pair pair = (Pair) o;

            if (key != null ? !key.equals(pair.key) : pair.key != null) return false;
            if (value != null ? !value.equals(pair.value) : pair.value != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = key != null ? key.hashCode() : 0;
            result = 31 * result + (value != null ? value.hashCode() : 0);
            return result;
        }
    }

    private static final long serialVersionUID = 1L;

    private static final String STRING_HOST_HEADER_NAME = "Host";

    private static final String STRING_LOCATION_HEADER = "Location";

    private static final String STRING_CONTENT_LENGTH_HEADER_NAME = "Content-Length";

    private static final String STRING_CONTENT_TYPE_HEADER_NAME = "Content-Type";

    /**
     * The maximum size for uploaded files in bytes. Default value is 5MB.
     */
    private static final int MAX_FILE_UPLOAD_SIZE = 5 * 1024 * 1024;

    private static final File FILE_UPLOAD_TEMP_DIRECTORY = new File(System.getProperty("java.io.tmpdir"));

    private static final Logger logger = LoggerFactory.getLogger(ProxyServlet.class);

    private String destHost = "10.0.15.10"; // e.g. server.com
    private int destPort = 8089; // e.g. 8080
    private String destContext = ""; // e.g. /backend
    private String destProtocol = "http";

    @Override
    public void init() throws ServletException {
        super.init();

        try {
            URL serverUrl = MyConfig.get().getServerUrl();
            destProtocol = serverUrl.getProtocol();
            destHost = serverUrl.getHost();
            destPort = serverUrl.getPort();
            destContext = serverUrl.getPath();

        } catch (IOException e) {
            throw new ServletException(e);
        }
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String url = getDestUrl(req);
        logger.debug("destination url: " + url);

        GetMethod getMethodProxyRequest = new GetMethod(url);
        forwardRequestHeaders(req, getMethodProxyRequest);
        executeProxyRequest(getMethodProxyRequest, req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String url = getDestUrl(req);
        logger.debug("destination url: " + url);

        PostMethod postMethod = new PostMethod(url);
        forwardRequestHeaders(req, postMethod);

        if (ServletFileUpload.isMultipartContent(req)) {
            handleMultipartPost(postMethod, req);
        } else {
            postMethod.setRequestEntity(new InputStreamRequestEntity(req.getInputStream()));
        }
        executeProxyRequest(postMethod, req, resp);
    }

    private void handleMultipartPost(PostMethod postMethod, HttpServletRequest req) throws ServletException {
        DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
        diskFileItemFactory.setSizeThreshold(MAX_FILE_UPLOAD_SIZE);
        diskFileItemFactory.setRepository(FILE_UPLOAD_TEMP_DIRECTORY);

        ServletFileUpload servletFileUpload = new ServletFileUpload(diskFileItemFactory);
        try {
            List<Part> listParts = servletFileUpload.parseRequest(req).stream()
                    .map(fileItem ->
                            fileItem.isFormField() ?
                                    new StringPart(
                                            fileItem.getFieldName(),
                                            fileItem.getString()
                                    ) :
                                    new FilePart(
                                            fileItem.getFieldName(),
                                            new ByteArrayPartSource(
                                                    fileItem.getName(),
                                                    fileItem.get()
                                            )
                                    ))
                    .collect(Collectors.toList());

            MultipartRequestEntity multipartRequestEntity = new MultipartRequestEntity(
                    listParts.toArray(new Part[listParts.size()]),
                    postMethod.getParams()
            );
            postMethod.setRequestEntity(multipartRequestEntity);
            // The current content-type header (received from the client) IS of
            // type "multipart/form-data", but the content-type header also
            // contains the chunk boundary string of the chunks. Currently, this
            // header is using the boundary of the client request, since we
            // blindly copied all headers from the client request to the proxy
            // request. However, we are creating a new request with a new chunk
            // boundary string, so it is necessary that we re-set the
            // content-type string to reflect the new chunk boundary string
            postMethod.setRequestHeader(STRING_CONTENT_TYPE_HEADER_NAME, multipartRequestEntity.getContentType());
        } catch (FileUploadException fileUploadException) {
            throw new ServletException(fileUploadException);
        }
    }


    private void forwardRequestHeaders(HttpServletRequest req, HttpMethod httpMethod) {
        getHeaders(req).forEach(p ->
                httpMethod.setRequestHeader(new Header(p.getKey(), p.getValue())));
    }

    private List<Pair<String, String>> getHeaders(HttpServletRequest req) {
        List<Pair<String, String>> list = Collections.list(req.getHeaderNames()).stream()
                .filter(name -> !name.equalsIgnoreCase(STRING_CONTENT_LENGTH_HEADER_NAME))
                .map(name ->
                        Collections.list(req.getHeaders(name)).stream()
                                .map(v -> name.equalsIgnoreCase(STRING_HOST_HEADER_NAME) ? getDestHostAndPort() : v)
                                .map(v -> new Pair<>(name, v)))
                .flatMap(l -> l)
                .collect(Collectors.toList());

        logger.debug("request headers are: " + list);
        return list;
    }

    private void executeProxyRequest(
            HttpMethod httpMethod,
            HttpServletRequest req,
            HttpServletResponse resp)
            throws IOException, ServletException {

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
        InputStream inputStreamProxyResponse = httpMethod.getResponseBodyAsStream();
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStreamProxyResponse);
        OutputStream outputStreamClientResponse = resp.getOutputStream();
        int intNextByte;
        while ((intNextByte = bufferedInputStream.read()) != -1) {
            outputStreamClientResponse.write(intNextByte);
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
        return getContextUrl(destProtocol, destHost, destPort, destContext);
    }

    private String getDestHostAndPort() {
        return getHostAndPort(destHost, destPort);
    }

    private String getContextUrl(String protocol, String host, int port, String context) {
        return protocol.toLowerCase() + "://" + getHostAndPort(host, port) + context;
    }

    private String getHostAndPort(String host, int port) {
        return host + (port == 80 ? "" : ":" + port);
    }

    private String getDestUrl(HttpServletRequest req) {
        StringBuilder url = new StringBuilder(getDestContextUrl());
        url.append(req.getServletPath());

        if (req.getPathInfo() != null)
            url.append(req.getPathInfo());
        if (req.getQueryString() != null) {
            url.append("?").append(req.getQueryString());
        }
        return url.toString();
    }

}
