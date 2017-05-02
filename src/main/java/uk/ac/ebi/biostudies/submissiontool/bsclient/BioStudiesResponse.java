package uk.ac.ebi.biostudies.submissiontool.bsclient;

/**
 * @author Olga Melnichuk
 */
public class BioStudiesResponse {
    public BioStudiesResponse(int statusCode, String mediaType, String body) {

    }

    public static BioStudiesResponse empty() {
        return null;
    }
}
