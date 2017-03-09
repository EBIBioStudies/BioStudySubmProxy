package uk.ac.ebi.biostudy.submission.rest.resources;

import org.json.JSONObject;

import java.util.function.Predicate;

/**
 * @author Olga Melnichuk
 */
enum SubmFilters implements SubmFilter {
    accNo {
        @Override
        public Predicate<JSONObject> create(String value) {
            return AccnoFilter.fromPattern(value);
        }
    },
    rTimeFrom {
        @Override
        public Predicate<JSONObject> create(String value) {
            return ReleaseDateFilter.from(Long.parseLong(value));
        }
    },
    rTimeTo {
        @Override
        public Predicate<JSONObject> create(String value) {
            return ReleaseDateFilter.to(Long.parseLong(value));
        }
    },
    keywords {
        @Override
        public Predicate<JSONObject> create(String value) {
            return TitleFilter.fromKeywords(value);
        }
    };
}
