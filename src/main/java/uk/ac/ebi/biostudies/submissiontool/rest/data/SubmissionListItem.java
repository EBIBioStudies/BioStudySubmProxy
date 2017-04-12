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

package uk.ac.ebi.biostudies.submissiontool.rest.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Comparator;

/**
 * @author olkin
 */
public class SubmissionListItem {

    enum Status {
        NEW,
        MODIFIED
    }

    @JsonProperty("accno")
    private String accession;

    @JsonProperty("title")
    private String title;

    @JsonProperty("rtime")
    private Long rtimeInSeconds;

    @JsonProperty("mtime")
    private Long mtimeInSeconds;

    @JsonProperty("status")
    private Status status;

    @SuppressWarnings("unused")
    public SubmissionListItem() {
    }

    public String getAccession() {
        return accession;
    }

    public Long getRtimeInSeconds() {
        return rtimeInSeconds;
    }

    public String getTitle() {
        return title;
    }

    public static SubmissionListItem from(ModifiedSubmission subm) {
         SubmissionListItem item = new SubmissionListItem();
         item.accession = subm.getAccno();
         item.title = subm.getTitle();
         item.rtimeInSeconds = subm.getRTimeInSeconds();
         item.mtimeInSeconds = subm.getMTimeInSeconds();
         item.status = subm.isNew() ? Status.NEW : Status.MODIFIED;
         return item;
    }

    public static Comparator<SubmissionListItem> sortByMTime() {
        return (o1, o2) -> {
            Long l1 = o1.mtimeInSeconds;
            Long l2 = o2.mtimeInSeconds;
            if (l1 == null && l2 == null) {
                return 0;
            } else if (l1 == null) {
                return 1;
            } else if (l2 == null) {
                return -1;
            }
            return l2.compareTo(l1);
        };
    }
}
