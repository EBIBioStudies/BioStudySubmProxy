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

    public static final String TMP = "TMP_";

    public static JSONObject wrap(JSONObject sbm) {
        if (sbm == null) {
            return null;
        }

        String accno = accession(sbm.getString("accno"));

        JSONObject wrap = new JSONObject();
        wrap.put("accno", accno);
        wrap.put("data", sbm);
        return wrap;
    }

    public static boolean isGeneratedAccession(String accno) {
        return accno.startsWith(TMP);
    }

    private static String accession(String accno) {
        return accno.matches("[\\w\\-]+") ? "COPY_" + accno : generateAccession();
    }

    private static String generateAccession() {
        return TMP + System.currentTimeMillis();
    }

}
