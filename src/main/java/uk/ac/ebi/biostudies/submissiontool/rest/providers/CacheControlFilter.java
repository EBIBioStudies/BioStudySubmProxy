package uk.ac.ebi.biostudies.submissiontool.rest.providers;

import java.io.IOException;
import java.lang.annotation.Annotation;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;

/**
 * @author Olga Melnichuk
 */

@Provider
@CacheControl
public class CacheControlFilter
        implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext reqContext, ContainerResponseContext respContext)
            throws IOException {
        for (Annotation a : respContext.getEntityAnnotations()) {
            if (a.annotationType() == CacheControl.class) {
                String value = ((CacheControl) a).value();
                respContext.getHeaders().putSingle(HttpHeaders.CACHE_CONTROL, value);
                break;
            }
        }
    }
}
