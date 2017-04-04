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

package uk.ac.ebi.biostudy.submission.bsclient;

import jersey.repackaged.com.google.common.collect.Lists;
import org.json.JSONObject;

import java.util.Optional;

class MyJSONObject {
    private final JSONObject obj;

    public MyJSONObject(JSONObject obj) {
        this.obj = obj;
    }

    Optional<MyJSONArray> getJSONArray(final String key) {
        Optional<String> realKey = getRealKey(key);
        return realKey.map(s -> new MyJSONArray(obj.getJSONArray(s)));
    }

    public Optional<String> getString(String key) {
        Optional<String> realKey = getRealKey(key);
        return realKey.map(obj::getString);
    }

    private Optional<String> getRealKey(String key) {
        return Lists
                .newArrayList(obj.keys())
                .stream()
                .filter(k -> ((String) k).equalsIgnoreCase(key))
                .findFirst();
    }
}
