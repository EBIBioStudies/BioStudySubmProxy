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

package uk.ac.ebi.biostudy.submission.rest.services;

import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.biostudy.submission.bsclient.BioStudiesClientException;
import uk.ac.ebi.biostudy.submission.rest.data.UserSession;
import uk.ac.ebi.biostudy.submission.rest.resources.SubmissionService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Olga Melnichuk
 */
@Path("/")
public class RESTService {

    private static final Logger logger = LoggerFactory.getLogger(RESTService.class);

    @Context
    private HttpServletRequest request;

    @Inject
    private SubmissionService service;


    @RolesAllowed("AUTHENTICATED")
    @GET
    @Path("/submissions")
    @Produces(MediaType.APPLICATION_JSON)
    public String getSubmissions(@Context UserSession userSession) throws BioStudiesClientException, IOException {
        return service.listSubmissions(userSession).toString();
    }

    @RolesAllowed("AUTHENTICATED")
    @GET
    @Path("/submission/{acc}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getSubmission(@Context UserSession userSession, @PathParam("acc") String acc, @QueryParam("origin") boolean origin)
            throws BioStudiesClientException, IOException {
        return service.getSubmission(userSession, acc, origin).toString();
    }

    @RolesAllowed("AUTHENTICATED")
    @GET
    @Path("/files/dir")
    @Produces(MediaType.APPLICATION_JSON)
    public String getFileDir(@Context UserSession userSession) throws BioStudiesClientException, IOException {
        return service.getFilesDir(userSession).toString();
    }

    @POST
    @Path("/auth/signup")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String signup(String str) throws BioStudiesClientException, IOException {
        try {
            JSONObject obj = toJson(str);
            URI path = new URI(obj.getString("path"));
            URI activationUrl = buildAppUrl(path);

            obj.put("activationURL", activationUrl.toString() + "/{KEY}");
            obj.remove("path");
            return service.singUp(obj).toString();
        } catch (URISyntaxException e) {
            throw new IOException("Bad url syntax");
        }
    }

    @POST
    @Path("/auth/passrstreq")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String passwordResetRequest(String str) throws BioStudiesClientException, IOException {
        try {
            JSONObject obj = toJson(str);
            URI path = new URI(obj.getString("path"));
            URI passwordResetUrl = buildAppUrl(path);

            obj.put("resetURL", passwordResetUrl.toString() + "/{KEY}");
            obj.remove("path");
            return service.passwordResetRequest(obj).toString();
        } catch (URISyntaxException e) {
            throw new IOException("Bad url syntax");
        }
    }

    private URI buildAppUrl(URI path) throws URISyntaxException {
        String reqUrl = request.getHeader("origin");
        if (reqUrl == null) {
            reqUrl = request.getRequestURI();
        }
        URI uri = new URI(reqUrl);
        URIBuilder uriBuilder = new URIBuilder()
                .setScheme(uri.getScheme())
                .setHost(uri.getHost())
                .setPath(path.getPath())
                .setFragment(path.getFragment());

        int port = uri.getPort();
        if (port > 0 && port != 80 && port != 443) {
            uriBuilder.setPort(port);
        }
        return uriBuilder.build();
    }

    @RolesAllowed("AUTHENTICATED")
    @POST
    @Path("/auth/signout")
    @Produces(MediaType.APPLICATION_JSON)
    public String signout(@Context UserSession userSession) throws BioStudiesClientException, IOException {
        JSONObject obj = service.singOut(userSession);
        request.getSession(false).invalidate();
        return obj.toString();
    }

    @RolesAllowed("AUTHENTICATED")
    @POST
    @Path("/submission/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String createSubmission(@Context UserSession userSession, String str)
            throws IOException, BioStudiesClientException {
        return service.createSubmission(userSession, toJson(str)).toString();
    }

    @RolesAllowed("AUTHENTICATED")
    @POST
    @Path("/submission/edit/{acc}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String editSubmission(@Context UserSession userSession, @PathParam("acc") String acc)
            throws IOException, BioStudiesClientException {
        return service.editSubmission(userSession, acc).toString();
    }

    @RolesAllowed("AUTHENTICATED")
    @POST
    @Path("/submission/save")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void saveSubmission(@Context UserSession userSession, String str) throws IOException, BioStudiesClientException {
        service.saveSubmission(userSession, toJson(str));
    }

    @RolesAllowed("AUTHENTICATED")
    @PUT
    @Path("/submission/submit")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String updateSubmission(@Context UserSession userSession, String str)
            throws BioStudiesClientException, IOException {
        return service.submitSubmission(userSession, toJson(str)).toString();
    }

    @RolesAllowed("AUTHENTICATED")
    @DELETE
    @Path("/submission/{acc}")
    @Produces(MediaType.APPLICATION_JSON)
    public String deleteSubmission(@Context UserSession userSession, @PathParam("acc") String acc)
            throws IOException, BioStudiesClientException {
        boolean deleted = service.deleteSubmission(acc, userSession);
        return statusObj(deleted).toString();
    }

    @RolesAllowed("AUTHENTICATED")
    @DELETE
    @Path("/files/delete")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteFile(@Context UserSession userSession, @QueryParam("file") String file)
            throws BioStudiesClientException, IOException {
        service.deleteFile(userSession, file);
    }

    @RolesAllowed("AUTHENTICATED")
    @GET
    @Path("/pubMedSearch/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String pubMedSearch(@Context UserSession userSession,  @PathParam("id") String id) {
        return service.pubMedSearch(id).toString();
    }

    private static JSONObject toJson(String str) {
        return new JSONObject(str);
    }

    private static JSONObject statusObj(boolean value) {
        JSONObject obj = new JSONObject();
        obj.put("status", value ? "OK" : "FAILED");
        return obj;
    }
}
