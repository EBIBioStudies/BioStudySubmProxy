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

package uk.ac.ebi.biostudies.submissiontool.rest.providers;

import uk.ac.ebi.biostudies.submissiontool.bsclient.BioStudiesClientException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * @author Olga Melnichuk
 */
@Provider
public class BioStudiesClientExceptionMapper implements ExceptionMapper<BioStudiesClientException> {

    @Override
    public Response toResponse(BioStudiesClientException ex) {
        if (ex.getContentType().contains(MediaType.APPLICATION_JSON))
            return Response.status(ex.getStatusCode())
                    .entity(ex.getContent())
                    .type(MediaType.APPLICATION_JSON).build();

        return Response.status(ex.getStatusCode())
                .entity(ex.getContent())
                .type(MediaType.TEXT_PLAIN_TYPE).build();
    }
}
