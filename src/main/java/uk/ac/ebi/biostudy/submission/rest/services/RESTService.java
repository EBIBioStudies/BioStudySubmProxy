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

import org.json.JSONArray;
import org.json.JSONObject;
import uk.ac.ebi.biostudy.submission.UserSession;
import uk.ac.ebi.biostudy.submission.services.SubmissionService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.File;

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
    public JSONArray getSubmissions(@Context UserSession userSession) {
        return service.listSubmissions(userSession);
    }

    @RolesAllowed("AUTHENTICATED")
    @GET
    @Path("/submission/{acc}")
    @Produces(MediaType.APPLICATION_JSON)
    public JSONObject getSubmission(@Context UserSession userSession, @PathParam("acc") String acc) {
        return service.getSubmission(userSession, acc);
    }

    @RolesAllowed("AUTHENTICATED")
    @GET
    @Path("/files/dir")
    @Produces(MediaType.APPLICATION_JSON)
    public JSONObject getFileDir(@Context UserSession userSession) {
        return service.getFilesDir(userSession);
    }

    @POST
    @Path("/auth/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JSONObject register(JSONObject obj) {
        UserSession userSession = new UserSession();
        if (obj.has("username")) {
            userSession.setUsername(obj.getString("username"));
        }
        if (obj.has("sessid")) {
            userSession.setSessid(obj.getString("sessid"));
        }
        File f = new File("submission" + userSession.getUsername() + ".json");
        userSession.setSubmissionFile(f);
        HttpSession session = request.getSession(true);
        //TODO
        session.setAttribute("userSession", userSession);

        JSONObject result = new JSONObject();
        result.put("token", "token");
        return result;
    }

    @POST
    @Path("/auth/signup")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JSONObject signup(JSONObject obj) {
        String activationUrl = request.getServerName() + "/biostudies/submissions/index.html#activate/{ACTIVATION:KEY}";
        return service.singUp(obj, activationUrl);
    }

    @RolesAllowed("AUTHENTICATED")
    @POST
    @Path("/auth/signout")
    @Produces(MediaType.APPLICATION_JSON)
    public JSONObject signout(@Context UserSession userSession) {
        JSONObject obj = service.singOut(userSession);
        request.getSession(false).invalidate();
        return obj;
    }


    @RolesAllowed("AUTHENTICATED")
    @POST
    @Path("/submission/save")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void saveSubmission(@Context UserSession userSession, JSONObject obj) {
        service.saveSubmission(userSession, obj);
    }

    @RolesAllowed("AUTHENTICATED")
    @POST
    @Path("/submission/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JSONObject createSubmission(@Context UserSession userSession, JSONObject obj) {
        return service.createSubmission(userSession, obj);
    }

    @RolesAllowed("AUTHENTICATED")
    @PUT
    @Path("/submission/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JSONObject updateSubmission(@Context UserSession userSession, JSONObject obj) {
        return service.updateSubmission(userSession, obj);
    }

    @RolesAllowed("AUTHENTICATED")
    @DELETE
    @Path("/submission/{acc}")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteSubmission(@Context UserSession userSession, @PathParam("acc") String acc) {
        service.deleteSubmission(acc, userSession);
    }

    @RolesAllowed("AUTHENTICATED")
    @DELETE
    @Path("/submission/submitted/{acc}")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteSubmission(@Context UserSession userSession, @PathParam("acc") String acc) {
        service.deleteSubmittedSubmission(acc, userSession);
    }

    @RolesAllowed("AUTHENTICATED")
    @DELETE
    @Path("/files/delete")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteFile(@Context UserSession userSession) {
        service.deleteFile(userSession, request.getQueryString());
    }
}
