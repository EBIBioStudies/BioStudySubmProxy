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

import java.util.function.Function;

/**
 * @author Olga Melnichuk
 */
public class SubmissionList {

    public static JSONArray transformSubmitted(JSONArray array) {
        return transform(array, obj -> {
            String accno = obj.getString("accno");
            String title = obj.getString("title");
            Long rtime = obj.getLong("rtime");
            return listItem(accno, title, rtime, true);
        });
    }

    public static JSONArray transformTemporary(JSONArray array) {
        return transform(array, obj -> {
            String accno = obj.getString("accno");
            JSONObject data = obj.getJSONObject("data");
            JSONArray attrs = data.getJSONArray("attributes");
            String title = getAttributeValue(attrs, "title");
            return listItem(accno, title, null, false);
        });
    }

    private static String getAttributeValue(JSONArray attrs, String attrName) {
        for (int i = 0; i < attrs.length(); i++) {
            JSONObject attr = attrs.getJSONObject(i);
            if (attr.getString("name").equals(attrName)) {
                return attr.getString("value");
            }
        }
        return null;
    }

    private static JSONObject listItem(String accno, String title, Long releaseDate, boolean submitted) {
        JSONObject obj = new JSONObject();
        obj.put("accno", accno);
        obj.put("title", title == null ? "" : title);
        obj.put("rtime", releaseDate == null ? "" : "" + releaseDate);
        obj.put("submitted", submitted);
        return obj;
    }

    private static JSONArray transform(JSONArray array, Function<JSONObject, JSONObject> func) {
        JSONArray transformed = new JSONArray();
        for (int j = 0; j < array.length(); j++) {
            JSONObject o = array.getJSONObject(j);
            transformed.put(func.apply(o));
        }
        return transformed;
    }
}
