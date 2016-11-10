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
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
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
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

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
    public void getSubmissions(@Context UserSession userSession, @Suspended final AsyncResponse async) {
        logger.debug("getSubmissions(userSession={})", userSession);
        service.getSubmissionsRx(userSession)
                .subscribe(submissions -> {
                    JSONObject obj = new JSONObject();
                    obj.put("submissions", submissions);
                    async.resume(obj.toString());
                }, async::resume);
    }

    @RolesAllowed("AUTHENTICATED")
    @GET
    @Path("/submission/{acc}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getSubmission(@Context UserSession userSession, @PathParam("acc") String acc, @QueryParam("origin") boolean origin)
            throws BioStudiesClientException, IOException {
        logger.debug("getSubmission(userSession={}, acc={}, origin={})", userSession, acc, origin);
        return service.getSubmission(userSession, acc, origin).toString();
    }

    @RolesAllowed("AUTHENTICATED")
    @GET
    @Path("/files/dir")
    @Produces(MediaType.APPLICATION_JSON)
    public String getFileDir(@Context UserSession userSession) throws BioStudiesClientException, IOException {
        logger.debug("getFileDir(userSession={})", userSession);
        return service.getFilesDir(userSession).toString();
    }

    @POST
    @Path("/auth/signup")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String signup(String str) throws BioStudiesClientException, IOException {
        logger.debug("signup(str={})", str);
        try {
            JSONObject obj = updateApplPath(str, "activationURL");
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
        logger.debug("passwordResetRequest(str={})", str);
        try {
            JSONObject obj = updateApplPath(str, "resetURL");
            return service.passwordResetRequest(obj).toString();
        } catch (URISyntaxException e) {
            throw new IOException("Bad url syntax");
        }
    }

    @POST
    @Path("/auth/resendActLink")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String resendActivationLink(String str) throws BioStudiesClientException, IOException {
        logger.debug("passwordResetRequest(str={})", str);
        try {
            JSONObject obj = updateApplPath(str, "activationURL");
            return service.resendActivationLink(obj).toString();
        } catch (URISyntaxException e) {
            throw new IOException("Bad url syntax");
        }
    }

    private JSONObject updateApplPath(String json, String paramName) throws URISyntaxException {
        JSONObject obj = toJson(json);
        URI path = new URI(obj.getString("path"));
        URI actUrl = buildAppUrl(path);

        obj.put(paramName, actUrl.toString() + "/{KEY}");
        obj.remove("path");
        return obj;
    }

    private URI buildAppUrl(URI path) throws URISyntaxException {
        logger.debug("buildAppUrl(path={})", path);
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
        logger.info("signout(userSession={})", userSession);
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
        logger.debug("createSubmission(userSession={}, str={})", userSession, str);
        return service.createSubmission(userSession, toJson(str)).toString();
    }

    @RolesAllowed("AUTHENTICATED")
    @POST
    @Path("/submission/edit/{acc}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String editSubmission(@Context UserSession userSession, @PathParam("acc") String acc)
            throws IOException, BioStudiesClientException {
        logger.debug("editSubmission(userSession={}, acc={})", userSession, acc);
        return service.editSubmission(userSession, acc).toString();
    }

    @RolesAllowed("AUTHENTICATED")
    @POST
    @Path("/submission/save")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void saveSubmission(@Context UserSession userSession, String str) throws IOException, BioStudiesClientException {
        logger.debug("saveSubmission(userSession={}, str={})", userSession, str);
        service.saveSubmission(userSession, toJson(str));
    }

    @RolesAllowed("AUTHENTICATED")
    @PUT
    @Path("/submission/submit")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String submitSubmission(@Context UserSession userSession, String str)
            throws BioStudiesClientException, IOException {
        logger.debug("submitSubmission(userSession={}, str={})", userSession, str);
        return service.submitSubmission(userSession, toJson(str)).toString();
    }

    @RolesAllowed("AUTHENTICATED")
    @DELETE
    @Path("/submission/{acc}")
    @Produces(MediaType.APPLICATION_JSON)
    public String deleteSubmission(@Context UserSession userSession, @PathParam("acc") String acc)
            throws IOException, BioStudiesClientException {
        logger.debug("deleteSubmission(userSession={}, acc={})", userSession, acc);
        boolean deleted = service.deleteSubmission(userSession, acc);
        logger.debug("deleteSubmission(): {}", deleted);
        return statusObj(deleted).toString();
    }

    @RolesAllowed("AUTHENTICATED")
    @DELETE
    @Path("/files/delete")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteFile(@Context UserSession userSession, @QueryParam("file") String file)
            throws BioStudiesClientException, IOException {
        logger.debug("deleteFile(userSession={}, file={})", userSession, file);
        service.deleteFile(userSession, file);
    }

    @RolesAllowed("AUTHENTICATED")
    @GET
    @Path("/pubMedSearch/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String pubMedSearch(@Context UserSession userSession, @PathParam("id") String id) {
        logger.debug("pubMedSearch(userSession={}, ID={})", userSession, id);
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

    @RolesAllowed("AUTHENTICATED")
    @POST
    @Path("/fileUpload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(FormDataMultiPart formParams) {
        List<FormDataBodyPart> parts = formParams.getFields("file");
        for (FormDataBodyPart part : parts) {
            FormDataContentDisposition file = part.getFormDataContentDisposition();
            String uploadedFileLocation = "/tmp/" + file.getFileName();
            writeToFile(part.getEntityAs(InputStream.class), uploadedFileLocation);
            System.out.println("File uploaded to : " + uploadedFileLocation);
        }
        return Response.status(200).entity("all done").build();
    }

    // save uploaded file to new location
    private void writeToFile(InputStream uploadedInputStream, String uploadedFileLocation) {
        OutputStream out = null;
        try {
            int read = 0;
            byte[] bytes = new byte[1024];

            out = new FileOutputStream(new File(uploadedFileLocation));
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeSilently(out);
            closeSilently(uploadedInputStream);
        }
    }

    private void closeSilently(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            // do nothing
        }
    }

}
