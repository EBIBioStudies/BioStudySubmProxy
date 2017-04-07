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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.biostudy.submission.bsclient.BioStudiesClientException;
import uk.ac.ebi.biostudy.submission.rest.data.UserSession;
import uk.ac.ebi.biostudy.submission.rest.resources.SubmissionService;
import uk.ac.ebi.biostudy.submission.rest.resources.params.EmailPathCaptchaParams;
import uk.ac.ebi.biostudy.submission.rest.resources.params.SignUpParams;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

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
    public String getSubmissions(@QueryParam("offset") int offset,
                                 @QueryParam("limit") int limit,
                                 @QueryParam("submitted") boolean submitted,
                                 @QueryParam("accNo") String accNoFilter,
                                 @QueryParam("rTimeFrom") Long rTimeFromFilter,
                                 @QueryParam("rTimeTo") Long rTimeToFilter,
                                 @QueryParam("keywords") String titleFilter,
                                 @Context UserSession session)
            throws BioStudiesClientException, IOException {

        Map<String, String> params = new HashMap<>();
        if (accNoFilter != null) {
            params.put("accNo", accNoFilter);
        }
        if (rTimeFromFilter != null) {
            params.put("rTimeFrom", rTimeFromFilter.toString());
        }
        if (rTimeToFilter != null) {
            params.put("rTimeTo", rTimeToFilter.toString());
        }
        if (titleFilter != null) {
            params.put("keywords", titleFilter);
        }

        logger.debug("getSubmissions(session={}, offset={}, limit={})", session, offset, limit);
        return submitted ?
                service.getSubmittedSubmissions(offset, limit, params, session) :
                service.getModifiedSubmissions(offset, limit, params, session);
    }

    @RolesAllowed("AUTHENTICATED")
    @GET
    @Path("/projects")
    @Produces(MediaType.APPLICATION_JSON)
    public String getProjects(@Context UserSession session)
            throws BioStudiesClientException, IOException {
        return service.getProjects(session);
    }

    @RolesAllowed("AUTHENTICATED")
    @GET
    @Path("/submission/{acc}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getSubmission(@Context UserSession session, @PathParam("acc") String acc, @QueryParam("origin") boolean origin)
            throws BioStudiesClientException, IOException {
        logger.debug("getSubmission(session={}, acc={}, origin={})", session, acc, origin);
        return service.getSubmission(acc, origin, session).json().toString();
    }

    @RolesAllowed("AUTHENTICATED")
    @GET
    @Path("/files/dir")
    @Produces(MediaType.APPLICATION_JSON)
    public String getFileDir(@QueryParam("path") String path,
                             @QueryParam("depth") int depth,
                             @QueryParam("showArchive") boolean showArchive,
                             @Context UserSession session) throws BioStudiesClientException, IOException {
        logger.debug("getFileDir(session={}, path={}, depth={}, showArchive={})", session, path, depth, showArchive);
        return service.getFilesDir(path, depth, showArchive, session);
    }

    @POST
    @Path("/auth/signup")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String signup(SignUpParams par) throws BioStudiesClientException, IOException {
        logger.debug("signup(form={})", par);
        try {
            return service.signUp(par.setPath(getApplUrl(par.getPath())));
        } catch (URISyntaxException e) {
            throw new IOException("Bad url syntax");
        }
    }

    @POST
    @Path("/auth/passrstreq")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String passwordResetRequest(EmailPathCaptchaParams par) throws BioStudiesClientException, IOException {
        logger.debug("passwordResetRequest(str={})", par);
        try {
            return service.passwordResetRequest(par.setPath(getApplUrl(par.getPath())));
        } catch (URISyntaxException e) {
            throw new IOException("Bad url syntax");
        }
    }

    @POST
    @Path("/auth/resendActLink")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String resendActivationLink(EmailPathCaptchaParams par) throws BioStudiesClientException, IOException {
        logger.debug("passwordResetRequest(str={})", par);
        try {
            return service.resendActivationLink(par.setPath(getApplUrl(par.getPath())));
        } catch (URISyntaxException e) {
            throw new IOException("Bad url syntax");
        }
    }

    private String getApplUrl(String path) throws URISyntaxException {
        URI actUrl = buildApplUrl(new URI(path));
        return actUrl.toString() + "/{KEY}";
    }

    private URI buildApplUrl(URI path) throws URISyntaxException {
        logger.debug("buildAppUrl(path={})", path);
        URI uri = new URI(request.getRequestURL().toString());
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
    public String signout(@Context UserSession session) throws BioStudiesClientException, IOException {
        logger.info("signout(session={})", session);
        String resp = service.signOut(session);
        request.getSession(false).invalidate();
        return resp;
    }

    @RolesAllowed("AUTHENTICATED")
    @POST
    @Path("/submission/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String createSubmission(@Context UserSession session, String str)
            throws IOException, BioStudiesClientException {
        logger.debug("createSubmission(session={}, str={})", session, str);
        return service.createSubmission(str, session).json().toString();
    }

    @RolesAllowed("AUTHENTICATED")
    @GET
    @Path("/submission/edit/{acc}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String editSubmission(@Context UserSession session, @PathParam("acc") String acc)
            throws IOException, BioStudiesClientException {
        logger.debug("editSubmission(session={}, acc={})", session, acc);
        return service.editSubmission(acc, session);
    }

    @RolesAllowed("AUTHENTICATED")
    @POST
    @Path("/submission/save")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void saveSubmission(@Context UserSession session, String str) throws IOException, BioStudiesClientException {
        logger.debug("saveSubmission(session={}, str={})", session, str);
        service.saveSubmission(str, session);
    }

    @RolesAllowed("AUTHENTICATED")
    @POST
    @Path("/submission/submit")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String submitSubmission(@Context UserSession session, String str)
            throws BioStudiesClientException, IOException {
        logger.debug("submitSubmission(session={}, str={})", session, str);
        return service.submitModified(str, session);
    }

    @RolesAllowed("AUTHENTICATED")
    @POST
    @Path("/submission/direct")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String directSubmit(@Context UserSession session, @QueryParam("create") Boolean create, String str)
            throws BioStudiesClientException, IOException {
        logger.debug("directSubmit(session={}, create={}, str={})", session, create, str);
        return service.submitPlain(create != null && create, str, session);
    }

    @RolesAllowed("AUTHENTICATED")
    @DELETE
    @Path("/submission/{acc}")
    @Produces(MediaType.APPLICATION_JSON)
    public String deleteSubmission(@Context UserSession session, @PathParam("acc") String acc)
            throws IOException, BioStudiesClientException {
        logger.debug("deleteSubmission(session={}, acc={})", session, acc);
        boolean deleted = service.deleteSubmission(acc, session);
        logger.debug("deleteSubmission(): {}", deleted);
        return statusObj(deleted).toString();
    }

    @RolesAllowed("AUTHENTICATED")
    @DELETE
    @Path("/files/delete")
    @Produces(MediaType.APPLICATION_JSON)
    public String deleteFile(@Context UserSession session, @QueryParam("path") String path)
            throws BioStudiesClientException, IOException {
        logger.debug("deleteFile(session={}, path={})", session, path);
        return service.deleteFile(path, session);
    }

    @RolesAllowed("AUTHENTICATED")
    @GET
    @Path("/pubMedSearch/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String pubMedSearch(@Context UserSession session, @PathParam("id") String id) {
        logger.debug("pubMedSearch(session={}, ID={})", session, id);
        return service.pubMedSearch(id);
    }

    private static JsonNode statusObj(boolean value) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("status", value ? "OK" : "FAILED");
        return node;
    }
}
