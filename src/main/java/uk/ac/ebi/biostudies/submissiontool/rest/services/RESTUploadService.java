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

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import uk.ac.ebi.biostudies.submissiontool.context.AppContext;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.List;

/**
 * @author Olga Melnichuk
 */
@Path("/fileUpload")
public class RESTUploadService {

    @Inject
    private ServletContext context;

    @RolesAllowed("AUTHENTICATED")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(FormDataMultiPart formParams) {
        if (this.isOfflineModeOn()) {
            FormDataBodyPart p = formParams.getField("path");
            String path = p.getValue();
            List<FormDataBodyPart> parts = formParams.getFields("file");
            for (FormDataBodyPart part : parts) {
                FormDataContentDisposition file = part.getFormDataContentDisposition();
                String uploadedFileLocation = getUserDir(path).resolve(file.getFileName()).toString();
                writeToFile(part.getEntityAs(InputStream.class), uploadedFileLocation);
                System.out.println("File uploaded to : " + uploadedFileLocation);
            }
        }
        return Response.status(200).entity("all done").build();
    }

    private boolean isOfflineModeOn() {
        return AppContext.getConfig(this.context).isOfflineModeOn();
    }

    private java.nio.file.Path getUserDir(String relPath) {
        return AppContext.getConfig(this.context).getUserDir().resolve(relPath.replaceAll("^/", ""));
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
