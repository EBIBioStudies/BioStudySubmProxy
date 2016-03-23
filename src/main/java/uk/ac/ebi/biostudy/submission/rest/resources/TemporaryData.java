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

package uk.ac.ebi.biostudy.submission.rest.resources;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mapdb.DB;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.concurrent.ConcurrentNavigableMap;

/**
 * @author Olga Melnichuk
 */
public class TemporaryData {

    private final DB db;

    public TemporaryData(DB db) {
        this.db = db;
    }

    public void saveSubmission(String username, JSONObject obj) {
        ConcurrentNavigableMap<String, String> map = this.db.treeMap("submissions" + username);

        String acc = "";
        if (obj.has("accno")) {
            acc = obj.getString("accno");
        }
        if (acc.equals("!{S-STA}")) {
            acc = "TEMP-" + map.size() + 1;
            obj.put("accno", acc);
        }
        if (acc != null && !acc.equals("")) {
            map.remove(acc);
            map.put(acc, obj.toString());
        }
        db.commit();

    }

    public void deleteSubmission(final String acc, final String username) {
        ConcurrentNavigableMap<String, String> map = this.db.treeMap("submissions" + username);
        Iterator<String> it = map.keySet().iterator();
        while (it.hasNext()) {
            System.out.println("key " + it.next());

        }
        if (map.remove(acc) == null) {
            // nothing to delete
            System.out.println("Nothing to delete for " + acc);

        }

        db.commit();
        db.compact();

    }

    public JSONArray listSubmissions(final String username) {
        JSONArray array = new JSONArray();

        ConcurrentNavigableMap<String, String> map = this.db.treeMap("submissions" + username);
        Iterator<String> it = map.keySet().iterator();
        DateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

        while (it.hasNext()) {
            String key = it.next();
            if (map.containsKey(key) && key != null && !key.equals("")) {
                JSONObject o = new JSONObject(map.get(key));
                JSONArray attrs = o.getJSONArray("attributes");
                for (int i = 0; i < attrs.length(); i++) {
                    JSONObject attr = attrs.getJSONObject(i);
                    if (attr.getString("name").equals("Title")) {
                        o.put("title", attr.getString("value"));
                    }
                    if (attr.getString("name").equals("ReleaseDate")) {
                        String sdate = attr.getString("value");

                        // formatDate.parse(sdate).getTime();
                        // Date drdate = new Date(new Long(sdate));

                        // o.put("rtime", attr.getString("value"));
                    }
                }
                System.out.println("Get" + key + "key" + map.get(key));
                array.put(o);
            }
        }
        return array;
    }


}
