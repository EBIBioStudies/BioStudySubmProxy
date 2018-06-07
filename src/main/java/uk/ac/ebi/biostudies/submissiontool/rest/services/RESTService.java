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

package uk.ac.ebi.biostudies.submissiontool.rest.services;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.biostudies.submissiontool.rest.data.ModifiedSubmission;
import uk.ac.ebi.biostudies.submissiontool.rest.data.UserSession;
import uk.ac.ebi.biostudies.submissiontool.rest.resources.params.SubmissionListFilterParams;
import uk.ac.ebi.biostudies.submissiontool.rest.providers.CacheControl;
import uk.ac.ebi.biostudies.submissiontool.rest.resources.SubmissionService;
import uk.ac.ebi.biostudies.submissiontool.rest.resources.params.EmailPathCaptchaParams;
import uk.ac.ebi.biostudies.submissiontool.rest.resources.params.KeyPasswordCaptchaParams;
import uk.ac.ebi.biostudies.submissiontool.rest.resources.params.SignUpParams;

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
    @CacheControl("no-cache")
    public void getSubmissions(@QueryParam("submitted") boolean submitted,
                               @BeanParam SubmissionListFilterParams filterParams,
                               @Context UserSession session,
                               @Suspended AsyncResponse async) {

        logger.debug("getSubmissions(session={}, filterParams={})", session, filterParams);
        (submitted ?
                service.getSubmittedSubmissionsRx(filterParams, session) :
                service.getPendingSubmissionsRx(filterParams, session))
                .subscribe(async::resume, async::resume);
    }

    @RolesAllowed("AUTHENTICATED")
    @GET
    @Path("/submissions/{accno}")
    @Produces(MediaType.APPLICATION_JSON)
    @CacheControl("no-cache")
    public void getSubmission(@Context UserSession session,
                              @PathParam("accno") String accno,
                              @Suspended AsyncResponse async) {
        logger.debug("getSubmission(session={}, acc={})", session, accno);
        service.findSubmissionRx(accno, session)
                .subscribe(async::resume, async::resume);
    }

    @RolesAllowed("AUTHENTICATED")
    @GET
    @Path("/submissions/origin/{accno}")
    @Produces(MediaType.APPLICATION_JSON)
    @CacheControl("no-cache")
    public void getOriginalSubmission(@Context UserSession session,
                                        @PathParam("accno") String accno,
                                        @Suspended AsyncResponse async) {
        logger.debug("getOriginalSubmission(session={}, acc={})", session, accno);
        service.getOriginalSubmissionRx(accno, session)
                .subscribe(async::resume, async::resume);
    }

    @RolesAllowed("AUTHENTICATED")
    @DELETE
    @Path("/submissions/{accno}")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteSubmission(@Context UserSession session,
                                 @PathParam("accno") String accno,
                                 @Suspended AsyncResponse async) {
        logger.debug("deleteSubmission(session={}, acc={})", session, accno);
        service.deleteSubmissionRx(accno, session)
                .map(resp -> "{}")
                .subscribe(async::resume, async::resume);
    }

    @RolesAllowed("AUTHENTICATED")
    @POST
    @Path("/submissions/tmp/save")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Deprecated
    public void saveSubmissionOld(@Context UserSession session,
                               String str,
                               @Suspended AsyncResponse async) throws IOException {
        logger.debug("saveSubmission(session={}, str={})", session, str);
        ModifiedSubmission subm = ModifiedSubmission.parse(str);
        service.savePendingSubmissionRx(str, subm.getAccno(), session)
                .subscribe(async::resume, async::resume);
    }

    @RolesAllowed("AUTHENTICATED")
    @POST
    @Path("/submissions/pending/{accno}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void saveSubmission(@Context UserSession session,
            @PathParam("accno") String accno,
            String str,
            @Suspended AsyncResponse async) {
        logger.debug("saveSubmission(session={}, str={})", session, str);
        service.savePendingSubmissionRx(str, accno, session)
                .subscribe(async::resume, async::resume);
    }

    @RolesAllowed("AUTHENTICATED")
    @POST
    @Path("/submissions/tmp/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void createSubmission(@Context UserSession session,
                                 String str,
                                 @Suspended AsyncResponse async) throws IOException {
        logger.debug("createSubmission(session={}, str={})", session, str);
        service.createPendingSubmissionRx(str, session)
                .subscribe(async::resume, async::resume);
    }

    @RolesAllowed("AUTHENTICATED")
    @POST
    @Path("/submissions/tmp/submit")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void submitSubmission(@Context UserSession session,
                                 String str,
                                 @Suspended AsyncResponse async) throws IOException {
        logger.debug("submitSubmission(session={}, str={})", session, str);
        service.submitPendingSubmissionRx(str, session)
                .subscribe(async::resume, async::resume);
    }

    @RolesAllowed("AUTHENTICATED")
    @POST
    @Path("/submissions/origin/submit")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void directSubmit(@Context UserSession session,
                             @QueryParam("create") Boolean create,
                             String subm,
                             @Suspended AsyncResponse async) throws IOException {
        logger.debug("directSubmit(session={}, create={}, subm={})", session, create, subm);
        service.submitPlainRx(create != null && create, subm, session)
                .subscribe(async::resume, async::resume);
    }

    @RolesAllowed("AUTHENTICATED")
    @GET
    @Path("/projects")
    @Produces(MediaType.APPLICATION_JSON)
    @CacheControl("no-cache")
    public void getProjects(@Context UserSession session,
                            @Suspended AsyncResponse async) {
        logger.debug("getProjects(session={})", session);
        service.getProjectsRx(session)
                .subscribe(async::resume, async::resume);
    }

    @RolesAllowed("AUTHENTICATED")
    @GET
    @Path("/files")
    @Produces(MediaType.APPLICATION_JSON)
    @CacheControl("no-cache")
    public void getFiles(@QueryParam("path") String path,
                         @QueryParam("depth") int depth,
                         @QueryParam("showArchive") boolean showArchive,
                         @Context UserSession session,
                         @Suspended AsyncResponse async) {
        logger.debug("getFileDir(session={}, path={}, depth={}, showArchive={})", session, path, depth, showArchive);
        service.getFilesRx(path, depth, showArchive, session)
                .subscribe(async::resume, async::resume);
    }

    @RolesAllowed("AUTHENTICATED")
    @DELETE
    @Path("/files")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteFile(@Context UserSession session,
                           @QueryParam("path") String path,
                           @Suspended AsyncResponse async) {
        logger.debug("deleteFile(session={}, path={})", session, path);
        service.deleteFileRx(path, session)
                .subscribe(async::resume, async::resume);
    }

    @RolesAllowed("AUTHENTICATED")
    @POST
    @Path("/auth/signout")
    @Produces(MediaType.APPLICATION_JSON)
    public void signout(@Context UserSession session,
                        @Suspended AsyncResponse async) {
        logger.info("signout(session={})", session);
        service.signOutRx(session)
                .map(resp -> {
                    request.getSession(false).invalidate();
                    return resp;
                })
                .subscribe(async::resume, async::resume);
    }

    @POST
    @Path("/auth/signup")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void signup(SignUpParams par,
                       @Suspended AsyncResponse async) throws IOException {
        logger.debug("signup(form={})", par);
        try {
            service.signUpRx(par.withPath(getApplUrl(par.getPath())))
                    .subscribe(async::resume, async::resume);
        } catch (URISyntaxException e) {
            throw new IOException("Bad url syntax");
        }
    }

    @POST
    @Path("/auth/password/reset_request")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void passwordResetRequest(EmailPathCaptchaParams par,
                                     @Suspended AsyncResponse async) throws IOException {
        logger.debug("passwordResetRequest(str={})", par);
        try {
            service.passwordResetRequestRx(par.withPath(getApplUrl(par.getPath())))
                    .subscribe(async::resume, async::resume);
        } catch (URISyntaxException e) {
            throw new IOException("Bad url syntax");
        }
    }

    @POST
    @Path("/auth/password/reset")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void passwordReset(KeyPasswordCaptchaParams par,
                              @Suspended AsyncResponse async) {
        logger.debug("passwordReset(str={})", par);
        service.passwordResetRx(par)
                .subscribe(async::resume, async::resume);
    }

    @POST
    @Path("/auth/activation/link")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void resendActivationLink(EmailPathCaptchaParams par,
                                     @Suspended AsyncResponse async) throws IOException {
        logger.debug("resendActivationLink(str={})", par);
        try {
            service.resendActivationLinkRx(par.withPath(getApplUrl(par.getPath())))
                    .subscribe(async::resume, async::resume);
        } catch (URISyntaxException e) {
            throw new IOException("Bad url syntax");
        }
    }

    @POST
    @Path("/auth/activation/check/{key}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void activate(@PathParam("key") String key,
                         @Suspended AsyncResponse async) {
        logger.debug("activate(key={})", key);
        service.activateRx(key)
                .subscribe(async::resume, async::resume);
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
    @GET
    @Path("/pubMedSearch/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public void pubMedSearch(@Context UserSession session,
                             @PathParam("id") String id,
                             @Suspended AsyncResponse async) {
        logger.debug("pubMedSearch(session={}, ID={})", session, id);
        service.pubMedSearchRx(id)
                .subscribe(async::resume, async::resume);
    }
}
