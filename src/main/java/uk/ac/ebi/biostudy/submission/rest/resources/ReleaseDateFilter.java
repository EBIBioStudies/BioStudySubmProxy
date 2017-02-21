package uk.ac.ebi.biostudy.submission.rest.resources;

import org.json.JSONObject;

import javax.validation.constraints.NotNull;
import java.util.function.Predicate;

/**
 * @author Olga Melnichuk
 */
public class ReleaseDateFilter implements Predicate<JSONObject> {

    private Predicate<Long> predicate;

    private ReleaseDateFilter(Predicate<Long> predicate) {
        this.predicate = predicate;
    }

    static Predicate<JSONObject> from(@NotNull Long seconds) {
        if (seconds == null) {
            throw new IllegalArgumentException("Number of seconds should not be null");
        }
        return new ReleaseDateFilter(aLong -> aLong.compareTo(seconds) >= 0);
    }

    static Predicate<JSONObject> to(Long seconds) {
        if (seconds == null) {
            throw new IllegalArgumentException("Number of seconds should not be null");
        }
        return new ReleaseDateFilter(aLong -> aLong.compareTo(seconds) < 0);
    }

    @Override
    public boolean test(JSONObject jsonObject) {
        Long rtime = jsonObject.getLong("rtime");
        return predicate.test(rtime);
    }
}
