/*
 * Copyright (c) 2017 European Molecular Biology Laboratory
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

package uk.ac.ebi.biostudies.submissiontool.proxy;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

/**
 * @author Olga Melnichuk
 */
class RemoteProxy  implements Proxy {

    private static class Pair<K, V> {
        private K key;
        private V value;

        Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        K getKey() {
            return key;
        }

        V getValue() {
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

    private static final Logger logger = LoggerFactory.getLogger(RemoteProxy.class);

    private static final String STRING_HOST_HEADER_NAME = "Host";

    private static final String STRING_LOCATION_HEADER = "Location";

    private static final String STRING_CONTENT_LENGTH_HEADER_NAME = "Content-Length";

    private static final String STRING_CONTENT_TYPE_HEADER_NAME = "Content-Type";

    /**
     * The maximum size for uploaded files in bytes. Default value is 5MB.
     */
    private static final int MAX_FILE_UPLOAD_SIZE = 5 * 1024 * 1024;

    private static final File FILE_UPLOAD_TEMP_DIRECTORY = new File(System.getProperty("java.io.tmpdir"));

    private final URI dest;
    private final Function<String, String> pathFilter;

    RemoteProxy(URI dest, Function<String, String> pathFilter) {
        this.dest = dest;
        this.pathFilter = pathFilter;
    }

    @Override
    public void proxyGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.debug("proxyGet()");
        executeMethod(this::createProxyGetReq, req, resp);
    }

    @Override
    public void proxyPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.debug("proxyPost()");
        executeMethod(this::createProxyPostReq, req, resp);
    }

    @Override
    public void proxyDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.debug("proxyDelete()");
        executeMethod(this::createProxyDeleteReq, req, resp);
    }

    @Override
    public void proxyPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.debug("proxyPut()");
        executeMethod(this::createProxyPutReq, req, resp);
    }

    private HttpGet createProxyGetReq(HttpServletRequest req) throws IOException {
        logger.debug("createProxyGetReq()");
        HttpGet get =  new HttpGet(getRequestUri(req));
        logger.debug("get: {}", get);
        forwardRequestHeaders(req, get);
        return get;
    }

    private HttpPost createProxyPostReq(HttpServletRequest req) throws ServletException, IOException {
        logger.debug("createProxyPostReq()");
        HttpPost post = new HttpPost(getRequestUri(req));
        logger.debug("post: {}", post);
        forwardRequestHeaders(req, post);

        if (ServletFileUpload.isMultipartContent(req)) {
            handleMultipartPost(post, req);
        } else {
            post.setEntity(new InputStreamEntity(req.getInputStream()));
        }
        return post;
    }

    private HttpPut createProxyPutReq(HttpServletRequest req) throws IOException {
        logger.debug("createProxyPutReq()");
        HttpPut put = new HttpPut(getRequestUri(req));
        logger.debug("put: {}", put);
        forwardRequestHeaders(req, put);

        put.setEntity(new InputStreamEntity(req.getInputStream()));
        return put;
    }

    private HttpDelete createProxyDeleteReq(HttpServletRequest req) throws IOException {
        logger.debug("createProxyDeleteReq()");
        HttpDelete del = new HttpDelete(getRequestUri(req));
        logger.debug("delete: {}", del);
        forwardRequestHeaders(req, del);
        return del;
    }

    private void handleMultipartPost(HttpPost post, HttpServletRequest req) throws ServletException {
        logger.debug("handleMultipartPost()");
        DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
        diskFileItemFactory.setSizeThreshold(MAX_FILE_UPLOAD_SIZE);
        diskFileItemFactory.setRepository(FILE_UPLOAD_TEMP_DIRECTORY);

        ServletFileUpload servletFileUpload = new ServletFileUpload(diskFileItemFactory);
        try {
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            List<FileItem> items = servletFileUpload.parseRequest(req);
            logger.debug("file items length: {}", items.size());
            items.stream().forEach(
                    fileItem -> {
                        if (fileItem.isFormField()) {
                            builder.addTextBody(
                                    fileItem.getFieldName(),
                                    fileItem.getString(),
                                    ContentType.create(fileItem.getContentType())
                            );
                        } else {
                            try {
                                builder.addBinaryBody(
                                        fileItem.getFieldName(),
                                        fileItem.getInputStream(),
                                        ContentType.create(fileItem.getContentType()),
                                        fileItem.getName());
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        }
                    }
            );

            HttpEntity entity = builder.build();
            post.setEntity(entity);
            // The current content-type header (received from the client) IS of
            // type "multipart/form-data", but the content-type header also
            // contains the chunk boundary string of the chunks. Currently, this
            // header is using the boundary of the client request, since we
            // blindly copied all headers from the client request to the proxy
            // request. However, we are creating a new request with a new chunk
            // boundary string, so it is necessary that we re-set the
            // content-type string to reflect the new chunk boundary string
            post.setHeader(STRING_CONTENT_TYPE_HEADER_NAME, entity.getContentType().getValue());
        } catch (FileUploadException fileUploadException) {
            throw new ServletException(fileUploadException);
        }
    }

    private void executeMethod(RequestTransform transform, HttpServletRequest req,
                               HttpServletResponse resp) throws ServletException, IOException {
        logger.debug("executeMethod()");
        HttpRequestBase reqBase;
        try {
            reqBase = transform.apply(req);
        } catch (BadRequestException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        reqBase.setURI(proxyUrl(reqBase.getURI()));
        logger.debug("proxied url: " + reqBase.getURI());

        CloseableHttpClient client = HttpClients.createDefault();
        try (CloseableHttpResponse response = client.execute(reqBase)) {
            int retCode = response.getStatusLine().getStatusCode();
            logger.debug("received retCode: " + retCode);


            if (retCode >= HttpServletResponse.SC_MULTIPLE_CHOICES /* 300 */
                    && retCode < HttpServletResponse.SC_NOT_MODIFIED /* 304 */) {
                redirect(req, resp, getLocationOrFail(response));
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
            Header[] headerArrayResponse = response.getAllHeaders();
            for (Header header : headerArrayResponse) {
                if (header.getName().equals("Transfer-Encoding") &&
                        header.getValue().equals("chunked"))
                    continue;
                resp.setHeader(header.getName(), header.getValue());
            }

            copyResponseContent(resp, response.getEntity().getContent());
        }
    }

    private static void copyResponseContent(HttpServletResponse resp, InputStream input) throws IOException {
        BufferedInputStream bufferedInput = new BufferedInputStream(input);
        OutputStream output = resp.getOutputStream();
        int intNextByte;
        while ((intNextByte = bufferedInput.read()) != -1) {
            output.write(intNextByte);
        }
    }

    private void forwardRequestHeaders(HttpServletRequest req, HttpRequestBase reqBase) {
        logger.debug("forwardRequestHeaders()");
        getHeaders(req).forEach(p ->
                reqBase.setHeader(p.getKey(), p.getValue()));
        logger.debug("reqBase: {}", reqBase);
    }

    private List<Pair<String, String>> getHeaders(HttpServletRequest req) {
        List<Pair<String, String>> list = Collections.list(req.getHeaderNames()).stream()
                .filter(name -> !name.equalsIgnoreCase(STRING_CONTENT_LENGTH_HEADER_NAME))
                .map(name ->
                        Collections.list(req.getHeaders(name)).stream()
                                .map(v -> name.equalsIgnoreCase(STRING_HOST_HEADER_NAME) ? getHostAndPort(dest) : v)
                                .map(v -> new Pair<>(name, v)))
                .flatMap(l -> l)
                .collect(Collectors.toList());

        logger.debug("request headers are: " + list);
        return list;
    }

    private void redirect(HttpServletRequest req, HttpServletResponse resp, String location) throws IOException {
        resp.sendRedirect(location.replace(dest.toString(), getContextUrl(req).toString()));
    }

    private String getLocationOrFail(HttpResponse response) throws ServletException {
        String location = response.getFirstHeader(STRING_LOCATION_HEADER).getValue();
        if (location == null) {
            throw new ServletException("No " + STRING_LOCATION_HEADER + " header was found in the response");
        }
        return location;
    }

    private String getHostAndPort(URI uri) {
        return uri.getHost() + (dest.getPort() < 0 ? "" : ":" + dest.getPort());
    }

    private URI getContextUrl(HttpServletRequest req) throws IOException {
        try {
            return new URIBuilder()
                    .setScheme(req.getScheme())
                    .setHost(req.getServerName())
                    .setPort(req.getServerPort())
                    .setPath(req.getContextPath())
                    .build();
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }


    private URI getRequestUri(HttpServletRequest req) throws IOException {
        String pathInfo = req.getPathInfo();
        pathInfo = pathInfo == null ? "" : pathInfo;

        try {
            return new URIBuilder()
                    .setPath(req.getServletPath() + pathInfo)
                    .setCustomQuery(req.getQueryString())
                    .build();
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    private URI proxyUrl(URI uri) throws IOException {
    try {
            return new URIBuilder()
                    .setScheme(dest.getScheme())
                    .setHost(dest.getHost())
                    .setPort(dest.getPort())
                    .setPath(asPath(dest.getPath(), pathFilter.apply(uri.getPath())))
                    .setCustomQuery(uri.getQuery())
                    .setFragment(uri.getFragment())
                    .build();
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    private static String asPath(String... parts) {
        return "/" + stream(parts).flatMap(p -> stream(p.split("/"))).filter(string -> !string.isEmpty()).collect(Collectors.joining("/"));
    }
}
