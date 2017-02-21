package uk.ac.ebi.biostudy.submission.rest.resources;

import org.json.JSONObject;

import javax.validation.constraints.NotNull;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Olga Melnichuk
 */
public class AccNoFilter implements Predicate<JSONObject> {

    private Pattern pattern;

    private AccNoFilter(Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public boolean test(JSONObject jsonObject) {
        String accno = jsonObject.getString("accno");
        return accno != null && pattern.matcher(accno).matches();
    }

    static Predicate<JSONObject> fromPattern(@NotNull String accNoPattern) {
        if (accNoPattern == null || accNoPattern.isEmpty()) {
            throw new IllegalArgumentException("Pattern should not be empty");
        }
        return new AccNoFilter(Pattern.compile(fromWildcard(accNoPattern)));
    }

    static String fromWildcard(String wildcard) {
        Pattern regex = Pattern.compile("[^*]+|(\\*)|(\\?)");
        Matcher m = regex.matcher(wildcard);
        StringBuffer b= new StringBuffer();
        while (m.find()) {
            if(m.group(1) != null) m.appendReplacement(b, ".*");
            else if(m.group(2) != null) m.appendReplacement(b, ".");
            else m.appendReplacement(b, "\\\\Q" + m.group(0) + "\\\\E");
        }
        m.appendTail(b);
        return b.toString();
    }
}
