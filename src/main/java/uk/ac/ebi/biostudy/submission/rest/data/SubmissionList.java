/*
 * Copyright (c) 2016 European Molecular Biology Laboratory
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

package uk.ac.ebi.biostudy.submission.rest.data;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.function.Function;

import static java.lang.Long.parseLong;
import static uk.ac.ebi.biostudy.submission.rest.data.SubmissionList.ListColumn.*;
import static uk.ac.ebi.biostudy.submission.rest.data.SubmissionList.SubmissionStatus.*;

/**
 * @author Olga Melnichuk
 */
public class SubmissionList {

    enum SubmissionStatus {
        NEW,
        SUBMITTED,
        MODIFIED
    }

    enum ListColumn {
        ACCNO("accno"),
        TITLE("title"),
        RELEASE_DATE("rtime"),
        MODIFICATION_DATE("mtime"),
        STATUS("status");

        private String name;

        ListColumn(String name) {
            this.name = name;
        }

        String get(JSONObject obj) {
            return obj.getString(name);
        }

        void set(JSONObject obj, String value) {
            obj.put(name, value);
        }
    }

    private static Comparator<JSONObject> byModificationDate = (o1, o2) -> {
        String m1 = MODIFICATION_DATE.get(o1);
        String m2 = MODIFICATION_DATE.get(o2);
        Long l1 = m1.isEmpty() ? null : parseLong(m1);
        Long l2 = m2.isEmpty() ? null : parseLong(m2);
        if (l1 == null && l2 == null) {
            return 0;
        } else if (l1 == null) {
            return 1;
        } else if (l2 == null) {
            return -1;
        }
        return l2.compareTo(l1);
    };

    public static List<JSONObject> transformSubmitted(JSONArray array) {
        return transform(array, obj -> {
            String accno = obj.getString("accno");
            String title = obj.getString("title");
            Long rtime = obj.getLong("rtime");
            return listItem(accno, title, rtime, null, SUBMITTED);
        });
    }

    public static List<JSONObject> transformTemporary(JSONArray array) {
        return transform(array, obj -> {
            String accno = obj.getString("accno");
            JSONObject data = obj.getJSONObject("data");
            JSONArray attrs = data.getJSONArray("attributes");
            String title = getAttributeValue(attrs, "title");
            Long rtime = readLong(getAttributeValue(attrs, "releaseDate"));
            Long mtime = readLong(obj.getString("changed"));
            return listItem(accno, title, rtime, mtime, NEW);
        });
    }

    private static final Long readLong(String value) {
        return value == null || value.isEmpty() ? null : parseLong(value);
    }

    private static String getAttributeValue(JSONArray attrs, String attrName) {
        for (int i = 0; i < attrs.length(); i++) {
            JSONObject attr = attrs.getJSONObject(i);
            if (attr.getString("name").equalsIgnoreCase(attrName)) {
                return attr.getString("value");
            }
        }
        return "";
    }

    public static JSONArray merge(List<JSONObject> temporary, List<JSONObject> submitted) {
        Map<String, JSONObject> copies = new HashMap<>();
        List<JSONObject> merged = new ArrayList<>();
        for (JSONObject item : temporary) {
            String accno = ACCNO.get(item);
            if (Submission.isGeneratedAccession(accno)) {
                merged.add(item);
            } else {
                copies.put(accno, item);
            }
        }

        for (JSONObject item : submitted) {
            String accno = ACCNO.get(item);
            if (copies.containsKey(accno)) {
                JSONObject copy = copies.get(accno);
                String title = TITLE.get(copy);
                String modifDate = MODIFICATION_DATE.get(copy);
                String releaseDate = RELEASE_DATE.get(item);
                merged.add(listItem(accno, title,
                        releaseDate.isEmpty() ? null : parseLong(releaseDate),
                        modifDate.isEmpty() ? null : parseLong(modifDate),
                        MODIFIED));
            } else {
                merged.add(item);
            }
        }
        merged.sort(byModificationDate);
        return toJSONArray(merged);
    }

    private static JSONArray toJSONArray(List<JSONObject> list) {
        JSONArray result = new JSONArray();
        for (JSONObject obj : list) {
            result.put(obj);
        }
        return result;
    }

    private static JSONObject listItem(String accno, String title, Long releaseDate, Long modificationDate, SubmissionStatus status) {
        JSONObject obj = new JSONObject();
        ACCNO.set(obj, accno);
        TITLE.set(obj, title == null ? "" : title);
        RELEASE_DATE.set(obj, releaseDate == null ? "" : "" + releaseDate);
        MODIFICATION_DATE.set(obj, modificationDate == null ? "" : "" + modificationDate);
        STATUS.set(obj, status.name());
        return obj;
    }

    private static List<JSONObject> transform(JSONArray array, Function<JSONObject, JSONObject> func) {
        List<JSONObject> transformed = new ArrayList<>();
        for (int j = 0; j < array.length(); j++) {
            JSONObject o = array.getJSONObject(j);
            transformed.add(func.apply(o));
        }
        return transformed;
    }
}
