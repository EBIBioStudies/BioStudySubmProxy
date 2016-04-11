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

import org.json.JSONObject;
import uk.ac.ebi.biostudy.submission.MyRequest;
import uk.ac.ebi.biostudy.submission.bsclient.BioStudiesClientException;
import uk.ac.ebi.biostudy.submission.rest.resources.SubmissionService;
import uk.ac.ebi.biostudy.submission.rest.user.UserSession;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;

/**
 * @author Olga Melnichuk
 */
@Path("/")
public class RESTService {

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
    public String getSubmission(@Context UserSession userSession, @PathParam("acc") String acc)
            throws BioStudiesClientException, IOException {
        return service.getSubmission(userSession, acc).toString();
    }

    @RolesAllowed("AUTHENTICATED")
    @GET
    @Path("/files/dir")
    @Produces(MediaType.APPLICATION_JSON)
    public String getFileDir(@Context UserSession userSession) throws BioStudiesClientException, IOException {
        return service.getFilesDir(userSession).toString();
    }

    @POST
    @Path("/auth/signin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String signin(String str) throws BioStudiesClientException, IOException {
        return service.singIn(toJson(str)).toString();
    }

    @POST
    @Path("/auth/signup")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String signup(String str) throws BioStudiesClientException, IOException {
        JSONObject obj = toJson(str);

        String activationUrl = request.getServerName() + "/biostudies/submissions/index.html#activate/{ACTIVATION:KEY}";
        obj.put("activationURL", activationUrl);
        return service.singUp(obj).toString();
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
    @Path("/submission/save")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void saveSubmission(@Context UserSession userSession, String str) throws IOException, BioStudiesClientException {
        service.saveSubmission(userSession, toJson(str));
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
    @PUT
    @Path("/submission/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String updateSubmission(@Context UserSession userSession, String str)
            throws BioStudiesClientException, IOException {
        return service.updateSubmission(userSession, toJson(str)).toString();
    }

    @RolesAllowed("AUTHENTICATED")
    @DELETE
    @Path("/submission/{acc}")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteSubmission(@Context UserSession userSession, @PathParam("acc") String acc)
            throws IOException, BioStudiesClientException {
        service.deleteTmpSubmission(acc, userSession);
    }

    @RolesAllowed("AUTHENTICATED")
    @DELETE
    @Path("/submission/submitted/{acc}")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteSubmittedSubmission(@Context UserSession userSession, @PathParam("acc") String acc)
            throws BioStudiesClientException, IOException {
        service.deleteSubmission(acc, userSession);
    }

    @RolesAllowed("AUTHENTICATED")
    @DELETE
    @Path("/files/delete")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteFile(@Context UserSession userSession, @QueryParam("file") String file)
            throws BioStudiesClientException, IOException {
        service.deleteFile(userSession, file);
    }

    private static JSONObject toJson(String str) {
        return new JSONObject(str);
    }
}
