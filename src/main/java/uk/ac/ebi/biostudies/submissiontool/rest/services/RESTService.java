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
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import uk.ac.ebi.biostudies.submissiontool.rest.resources.SubmissionService;

/**
 * @author Olga Melnichuk
 */
@Path("/")
public class RESTService {

    @Inject
    private SubmissionService service;

    //@RolesAllowed("AUTHENTICATED")
    @GET
    @Path("/hello/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void signup(@PathParam("name") String name, @Suspended AsyncResponse async) throws IOException {
        service.helloRx(name).subscribe(async::resume, async::resume);
    }
}
