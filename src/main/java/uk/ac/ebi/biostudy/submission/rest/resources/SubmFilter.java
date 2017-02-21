package uk.ac.ebi.biostudy.submission.rest.resources;

import org.json.JSONObject;

import java.util.function.Predicate;

/**
 * @author Olga Melnichuk
 */
public interface SubmFilter {
    Predicate<JSONObject> create (String value);
}
