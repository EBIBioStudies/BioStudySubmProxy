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

package uk.ac.ebi.biostudies.submissiontool.rest.resources.params;

import uk.ac.ebi.biostudies.submissiontool.rest.data.SubmissionListItem;
import uk.ac.ebi.biostudies.submissiontool.rest.data.filter.SubmissionListFilters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @author Olga Melnichuk
 */
public class SubmFilterParams {

    private List<Predicate<SubmissionListItem>> predicates = new ArrayList<>();

    private SubmFilterParams(List<Predicate<SubmissionListItem>> predicates) {
        this.predicates.addAll(predicates);
    }

    public static SubmFilterParams fromMap(Map<String, String> map) {
        List<Predicate<SubmissionListItem>> predicates = new ArrayList<>();
        map.forEach((k, v) -> {
            if (!v.isEmpty()) {
                predicates.add(SubmissionListFilters.valueOf(k).create(v));
            }
        });
        return new SubmFilterParams(predicates);
    }

    public Predicate<? super SubmissionListItem> asPredicate() {
        return (Predicate<SubmissionListItem>) item -> predicates.stream().allMatch(p -> p.test(item));
    }
}
