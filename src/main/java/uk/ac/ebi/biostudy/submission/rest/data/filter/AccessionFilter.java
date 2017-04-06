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

package uk.ac.ebi.biostudy.submission.rest.data.filter;

import uk.ac.ebi.biostudy.submission.rest.data.SubmissionListItem;

import javax.validation.constraints.NotNull;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Olga Melnichuk
 */
public class AccessionFilter implements Predicate<SubmissionListItem> {

    private Pattern pattern;

    private AccessionFilter(Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public boolean test(SubmissionListItem item) {
        String accno = item.getAccession();
        return accno != null && pattern.matcher(accno).matches();
    }

    static Predicate<SubmissionListItem> fromPattern(@NotNull String accNoPattern) {
        if (accNoPattern == null || accNoPattern.isEmpty()) {
            throw new IllegalArgumentException("Pattern should not be empty");
        }
        return new AccessionFilter(Pattern.compile(fromWildcard(accNoPattern)));
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
