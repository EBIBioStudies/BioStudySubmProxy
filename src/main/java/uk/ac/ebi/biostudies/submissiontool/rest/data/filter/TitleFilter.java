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

package uk.ac.ebi.biostudies.submissiontool.rest.data.filter;

import uk.ac.ebi.biostudies.submissiontool.rest.data.SubmissionListItem;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Olga Melnichuk
 */
public class TitleFilter implements Predicate<SubmissionListItem> {

    private Predicate<String> predicate;

    TitleFilter(Predicate<String> predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean test(SubmissionListItem item) {
        String title = item.getTitle();
        return title != null && this.predicate.test(title);
    }

    static Predicate<SubmissionListItem> fromKeywords(String value) {
        final List<Predicate<String>> predicates = Arrays.stream(value.split(" "))
                .filter(k -> !k.isEmpty())
                .map((String keyword) -> (Predicate<String>) str -> str != null && str.contains(keyword))
                .collect(Collectors.toList());

        return new TitleFilter(
                s -> predicates.stream().allMatch(p -> p.test(s))
        );
    }
}
