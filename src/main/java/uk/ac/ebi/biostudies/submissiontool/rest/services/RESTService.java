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
import uk.ac.ebi.biostudies.submissiontool.rest.data.PendingSubmission;
import uk.ac.ebi.biostudies.submissiontool.rest.data.UserSession;
import uk.ac.ebi.biostudies.submissiontool.rest.providers.CacheControl;
import uk.ac.ebi.biostudies.submissiontool.rest.resources.SubmissionService;
import uk.ac.ebi.biostudies.submissiontool.rest.resources.params.EmailPathCaptchaParams;
import uk.ac.ebi.biostudies.submissiontool.rest.resources.params.KeyPasswordCaptchaParams;
import uk.ac.ebi.biostudies.submissiontool.rest.resources.params.SignUpParams;
import uk.ac.ebi.biostudies.submissiontool.rest.resources.params.SubmissionListFilterParams;

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
    @CacheControl("no-cache")
    public void getSubmissions(@QueryParam("submitted") boolean submitted,
            @BeanParam SubmissionListFilterParams filterParams,
            @Context UserSession session,
            @Suspended AsyncResponse async) {
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
    public void getSubmission(@Context UserSession session, @PathParam("accno") String accno,
            @Suspended AsyncResponse async) {
        service.getPendingSubmissionRx(accno, session)
                .onErrorResumeNext(service.getOriginalSubmissionRx(accno, session))
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
        service.getOriginalSubmissionRx(accno, session).subscribe(async::resume, async::resume);
    }

    @RolesAllowed("AUTHENTICATED")
    @DELETE
    @Path("/submissions/{accno}")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteSubmission(@Context UserSession session,
            @PathParam("accno") String accno,
            @Suspended AsyncResponse async) {
        service.deleteSubmissionRx(accno, session).map(resp -> "{}")
                .subscribe(async::resume, async::resume);
    }

    @RolesAllowed("AUTHENTICATED")
    @POST
    @Path("/submissions/tmp/save")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Deprecated
    public void savePendingSubmissionOld(@Context UserSession session, String subm,
            @Suspended AsyncResponse async) throws IOException {
        PendingSubmission pending = PendingSubmission.parse(subm);
        service.savePendingSubmissionRx(pending.getData().toString(), pending.getAccno(), session)
                .subscribe(async::resume, async::resume);
    }

    @RolesAllowed("AUTHENTICATED")
    @POST
    @Path("/submissions/pending/{accno}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void savePendingSubmission(@Context UserSession session, @PathParam("accno") String accno,
            String pageTab, @Suspended AsyncResponse async) {
        service.savePendingSubmissionRx(pageTab, accno, session).subscribe(async::resume, async::resume);
    }

    @RolesAllowed("AUTHENTICATED")
    @POST
    @Path("/submissions/tmp/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void createPendingSubmission(@Context UserSession session, String pageTab, @Suspended AsyncResponse async) {
        service.createPendingSubmissionRx(pageTab, session).subscribe(async::resume, async::resume);
    }

    @RolesAllowed("AUTHENTICATED")
    @POST
    @Path("/submissions/tmp/submit")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void submitPendingSubmissionOld(@Context UserSession session, String subm,
            @Suspended AsyncResponse async) throws IOException {
        PendingSubmission pending = PendingSubmission.parse(subm);
        service.submitPendingSubmissionRx(pending.getData().toString(), pending.getAccno(), session)
                .subscribe(async::resume, async::resume);
    }

    @RolesAllowed("AUTHENTICATED")
    @POST
    @Path("/submissions/pending/{accno}/submit")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void submitPendingSubmission(@Context UserSession session, @PathParam("accno") String accno,
            String pageTab, @Suspended AsyncResponse async) {
        service.submitPendingSubmissionRx(pageTab, accno, session).subscribe(async::resume, async::resume);
    }

    @RolesAllowed("AUTHENTICATED")
    @POST
    @Path("/submissions/origin/submit")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void directSubmitOld(@Context UserSession session,
            @QueryParam("create") Boolean create,
            String pageTab,
            @Suspended AsyncResponse async) {
        service.directSubmitRx(create != null && create, pageTab, session)
                .subscribe(async::resume, async::resume);
    }

    @RolesAllowed("AUTHENTICATED")
    @GET
    @Path("/projects")
    @Produces(MediaType.APPLICATION_JSON)
    @CacheControl("no-cache")
    public void getProjects(@Context UserSession session,
            @Suspended AsyncResponse async) {
        service.getProjectsRx(session).subscribe(async::resume, async::resume);
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
        service.getFileDirRx(path, depth, showArchive, session).subscribe(async::resume, async::resume);
    }

    @RolesAllowed("AUTHENTICATED")
    @DELETE
    @Path("/files")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteFile(@Context UserSession session,
            @QueryParam("path") String path,
            @Suspended AsyncResponse async) {
        service.deleteFileRx(path, session).subscribe(async::resume, async::resume);
    }

    @RolesAllowed("AUTHENTICATED")
    @POST
    @Path("/auth/signout")
    @Produces(MediaType.APPLICATION_JSON)
    public void signout(@Context UserSession session,
            @Suspended AsyncResponse async) {
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
        service.signUpRx(par.withPath(getApplUrl(par.getPath()))).subscribe(async::resume, async::resume);
    }

    @POST
    @Path("/auth/password/reset_request")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void passwordResetRequest(EmailPathCaptchaParams par,
            @Suspended AsyncResponse async) throws IOException {
        service.passwordResetRequestRx(par.withPath(getApplUrl(par.getPath()))).subscribe(async::resume, async::resume);
    }

    @POST
    @Path("/auth/password/reset")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void passwordReset(KeyPasswordCaptchaParams par,
            @Suspended AsyncResponse async) {
        service.passwordResetRx(par).subscribe(async::resume, async::resume);
    }

    @POST
    @Path("/auth/activation/link")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void resendActivationLink(EmailPathCaptchaParams par,
            @Suspended AsyncResponse async) throws IOException {
        service.resendActivationLinkRx(par.withPath(getApplUrl(par.getPath()))).subscribe(async::resume, async::resume);
    }

    @POST
    @Path("/auth/activation/check/{key}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void activate(@PathParam("key") String key,
            @Suspended AsyncResponse async) {
        service.activateRx(key).subscribe(async::resume, async::resume);
    }

    private String getApplUrl(String path) throws IOException {
        return buildApplUrl(path) + "/{KEY}";
    }

    private String buildApplUrl(String stringPath) throws IOException {
        try {
            URI path = new URI(stringPath);
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
            return uriBuilder.build().toString();
        } catch (URISyntaxException e) {
            throw new IOException("Bad url syntax");
        }
    }


    @RolesAllowed("AUTHENTICATED")
    @GET
    @Path("/pubMedSearch/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public void pubMedSearch(@Context UserSession session,
            @PathParam("id") String id,
            @Suspended AsyncResponse async) {
        service.pubMedSearchRx(id).subscribe(async::resume, async::resume);
    }
}
