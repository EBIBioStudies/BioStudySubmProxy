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

/**
 * @author Olga Melnichuk
 */
public class ReleaseDateFilter implements Predicate<SubmissionListItem> {

    private Predicate<Long> predicate;

    private ReleaseDateFilter(Predicate<Long> predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean test(SubmissionListItem item) {
        Long rtime = item.getRtimeInSeconds();
        return rtime != null && predicate.test(rtime);
    }

    static Predicate<SubmissionListItem> from(@NotNull Long seconds) {
        if (seconds == null) {
            throw new IllegalArgumentException("Number of seconds should not be null");
        }
        return new ReleaseDateFilter(aLong -> aLong.compareTo(seconds) >= 0);
    }

    static Predicate<SubmissionListItem> to(Long seconds) {
        if (seconds == null) {
            throw new IllegalArgumentException("Number of seconds should not be null");
        }
        return new ReleaseDateFilter(aLong -> aLong.compareTo(seconds) < 0);
    }
}
