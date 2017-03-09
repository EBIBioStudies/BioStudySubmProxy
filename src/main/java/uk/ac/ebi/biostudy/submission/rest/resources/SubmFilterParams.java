package uk.ac.ebi.biostudy.submission.rest.resources;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @author Olga Melnichuk
 */
public class SubmFilterParams {

    private List<Predicate<JSONObject>> predicates = new ArrayList<>();

    private SubmFilterParams(List<Predicate<JSONObject>> predicates) {
        this.predicates.addAll(predicates);
    }

    public static SubmFilterParams fromMap(Map<String, String> map) {
        List<Predicate<JSONObject>> predicates = new ArrayList<>();
        map.forEach((k, v) -> {
            if (!v.isEmpty()) {
                predicates.add(SubmFilters.valueOf(k).create(v));
            }
        });
        return new SubmFilterParams(predicates);
    }

    public Predicate<? super JSONObject> asPredicate() {
        return (Predicate<JSONObject>) jsonObject -> predicates.stream().allMatch(p -> p.test(jsonObject));
    }
}
