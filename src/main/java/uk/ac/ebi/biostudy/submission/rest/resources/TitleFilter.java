package uk.ac.ebi.biostudy.submission.rest.resources;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Olga Melnichuk
 */
public class TitleFilter implements Predicate<JSONObject> {

    private Predicate<String> predicate;

    TitleFilter(Predicate<String> predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean test(JSONObject jsonObject) {
        String title = jsonObject.getString("title");
        return title != null && this.predicate.test(title);
    }

    static Predicate<JSONObject> fromKeywords(String value) {
        final List<Predicate<String>> predicates = Arrays.stream(value.split(" "))
                .filter(k -> !k.isEmpty())
                .map((String keyword) -> (Predicate<String>) str -> str != null && str.contains(keyword))
                .collect(Collectors.toList());

        return new TitleFilter(
                s -> predicates.stream().allMatch(p -> p.test(s))
        );
    }
}
