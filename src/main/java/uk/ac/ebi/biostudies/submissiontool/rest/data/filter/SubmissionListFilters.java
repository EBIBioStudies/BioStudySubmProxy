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
import java.util.function.Predicate;

/**
 * @author Olga Melnichuk
 */
public enum SubmissionListFilters implements SubmissionListFilter {
    accNo {
        @Override
        public Predicate<SubmissionListItem> create(String value) {
            return AccessionFilter.fromPattern(value);
        }
    },
    rTimeFrom {
        @Override
        public Predicate<SubmissionListItem> create(String value) {
            return ReleaseDateFilter.from(Long.parseLong(value));
        }
    },
    rTimeTo {
        @Override
        public Predicate<SubmissionListItem> create(String value) {
            return ReleaseDateFilter.to(Long.parseLong(value));
        }
    },
    keywords {
        @Override
        public Predicate<SubmissionListItem> create(String value) {
            return TitleFilter.fromKeywords(value);
        }
    };

    static boolean contains(String value) {
        return Arrays.stream(SubmissionListFilters.values()).anyMatch((v) -> v.name().equals(value));
    }
}
