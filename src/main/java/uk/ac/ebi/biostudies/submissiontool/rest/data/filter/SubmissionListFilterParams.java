/*
 * Copyright (c) 2018 European Molecular Biology Laboratory
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

import javax.ws.rs.QueryParam;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SubmissionListFilterParams {

    private final Integer offset;
    private final Integer limit;
    private final String accNo;
    private final Long rTimeFrom;
    private final Long rTimeTo;
    private final String keywords;

    public SubmissionListFilterParams(@QueryParam("offset") Integer offset,
                                      @QueryParam("limit") Integer limit,
                                      @QueryParam("accNo") String accNo,
                                      @QueryParam("rTimeFrom") Long rTimeFrom,
                                      @QueryParam("rTimeTo") Long rTimeTo,
                                      @QueryParam("keywords") String keywords) {
        this.offset = offset;
        this.limit = limit;
        this.accNo = accNo;
        this.rTimeFrom = rTimeFrom;
        this.rTimeTo = rTimeTo;
        this.keywords = keywords;
    }

    public int getOffset() {
        return Optional.of(offset).isPresent() ? this.offset : 0;
    }

    public int getLimit() {
        return Optional.of(limit).isPresent() ? this.limit : 50;
    }

    public Predicate<? super SubmissionListItem> asPredicate() {
        List<Predicate<SubmissionListItem>> predicates = asMap().entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .filter(entry -> SubmissionListFilters.contains(entry.getKey()))
                .map(entry -> SubmissionListFilters.valueOf(entry.getKey()).create(entry.getValue()))
                .collect(Collectors.toList());
        return (Predicate<SubmissionListItem>) item -> predicates.stream().allMatch(p -> p.test(item));
    }

    public Map<String, String> asMap() {
        Map<String, String> map = new HashMap<>();
        Optional.of(offset).ifPresent(v -> map.put("offset", offset.toString()));
        Optional.of(limit).ifPresent(v -> map.put("limit", limit.toString()));
        Optional.of(accNo).ifPresent(v -> map.put("accNo", accNo));
        Optional.of(rTimeFrom).ifPresent(v -> map.put("rTimeFrom", rTimeFrom.toString()));
        Optional.of(rTimeTo).ifPresent(v -> map.put("rTimeTo", rTimeTo.toString()));
        Optional.of(keywords).ifPresent(v -> map.put("keywords", keywords));
        return map;
    }
}
