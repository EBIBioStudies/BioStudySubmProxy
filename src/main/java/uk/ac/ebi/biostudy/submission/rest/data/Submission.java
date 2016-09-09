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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * @author Olga Melnichuk
 */
public class Submission {
    private static final Logger logger = LoggerFactory.getLogger(Submission.class);

    private static final DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    private static final String TMP = "TMP_";

    public static JSONObject wrap(JSONObject sbm) {
        if (sbm == null) {
            return null;
        }

        String accno = accession(sbm.getString("accno"));

        JSONObject wrap = new JSONObject();
        wrap.put("accno", accno);
        wrap.put("data", sbm);
        return modified(wrap);
    }

    public static String titleAttribute(JSONObject obj) {
        return attributeValue(attributes(obj), "title");
    }

    public static String releaseDateAttribute(JSONObject obj) {
        return attributeValue(attributes(obj), "releaseDate");
    }

    public static Long releaseDateAttributeInSeconds(JSONObject obj) {
        return seconds(releaseDateAttribute(obj));
    }

    public static String accno(JSONObject obj) {
        return obj.getString("accno");
    }

    public static JSONObject data(JSONObject obj) {
        return obj.getJSONObject("data");
    }

    public static JSONObject modified(JSONObject obj) {
        obj.put("changed", System.currentTimeMillis());
        return obj;
    }

    private static JSONArray attributes(JSONObject obj) {
        return obj.getJSONArray("attributes");
    }

    private static String attributeValue(JSONArray attrs, String attrName) {
        for (int i = 0; i < attrs.length(); i++) {
            JSONObject attr = attrs.getJSONObject(i);
            if (attr.getString("name").equalsIgnoreCase(attrName)) {
                if (!attr.has("value")) {
                    return "";
                }
                return attr.getString("value");
            }
        }
        return "";
    }

    private static Long seconds(String value) {
        try {
            if (value != null && !value.isEmpty()) {
                return format.parse(value).getTime() / 1000;
            }
        } catch (ParseException e) {
            logger.error("Data format error: {}", value);
        }
        return null;
    }

    public static boolean isGeneratedAccession(String accno) {
        return accno.startsWith(TMP);
    }

    private static String accession(String accno) {
        return accno.matches("[\\w\\-]+") ? accno : generateAccession();
    }

    private static String generateAccession() {
        return TMP + System.currentTimeMillis();
    }

}
