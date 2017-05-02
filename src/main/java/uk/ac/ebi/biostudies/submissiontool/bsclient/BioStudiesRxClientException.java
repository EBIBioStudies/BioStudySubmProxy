package uk.ac.ebi.biostudies.submissiontool.bsclient;

/**
 * @author Olga Melnichuk
 */
public class BioStudiesRxClientException extends RuntimeException {

    private final int statusCode;

    private final String contentType;

    private final String content;

    public BioStudiesRxClientException(int statusCode, String contentType, String content) {
        super(statusCode + "::" + contentType + "::" + content);
        this.statusCode = statusCode;
        this.content = content;
        this.contentType = contentType;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getContentType() {
        return contentType;
    }

    public String getContent() {
        return content;
    }
}
