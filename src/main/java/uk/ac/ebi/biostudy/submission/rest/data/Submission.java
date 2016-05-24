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

import org.json.JSONObject;

/**
 * @author Olga Melnichuk
 */
public class Submission {

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

    public static JSONObject deleted(JSONObject obj) {
        obj = modified(obj);
        obj.put("deleted", "true");
        return obj;
    }

    public static boolean isGeneratedAccession(String accno) {
        return accno.startsWith(TMP);
    }

    private static String accession(String accno) {
        return accno.matches("[\\w\\-]+") ? accno :  generateAccession();
    }

    private static String generateAccession() {
        return TMP + System.currentTimeMillis();
    }

}
