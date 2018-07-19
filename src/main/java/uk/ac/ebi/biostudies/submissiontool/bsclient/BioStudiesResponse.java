package uk.ac.ebi.biostudies.submissiontool.bsclient;


import java.util.Optional;
import javax.ws.rs.core.MediaType;
import rx.Observable;

/**
 * @author Olga Melnichuk
 */
public class BioStudiesResponse {

    private final String body;
    private final int statusCode;
    private final MediaType mediaType;

    public BioStudiesResponse(String body, int statusCode, MediaType mediaType) {
        this.body = body;
        this.statusCode = statusCode;
        this.mediaType = Optional.ofNullable(mediaType).orElse(MediaType.TEXT_HTML_TYPE);
    }

    public String getBody() {
        return body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public Observable<String> asObservable() {
        return this.statusCode == 200 ? Observable.just(this.body) :
                Observable.error(new BioStudiesRxClientException(this.statusCode, this.getMediaType().getType(), this.body));
    }
}
