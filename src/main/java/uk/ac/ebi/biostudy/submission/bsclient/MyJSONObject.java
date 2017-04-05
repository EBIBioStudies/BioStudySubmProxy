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

import com.fasterxml.jackson.databind.JsonNode;
import jersey.repackaged.com.google.common.collect.Lists;

import java.util.Optional;

class MyJSONObject {
    private final JsonNode obj;

    public MyJSONObject(JsonNode obj) {
        this.obj = obj;
    }

    Optional<MyJSONArray> getJSONArray(final String key) {
        Optional<String> propName = getRealKey(key);
        return propName.map(s -> new MyJSONArray(obj.get(s)));
    }

    public Optional<String> getString(String key) {
        Optional<String> realKey = getRealKey(key);
        return realKey.map(s -> obj.get(s).asText());
    }

    private Optional<String> getRealKey(String key) {
        return Lists
                .newArrayList(obj.fieldNames())
                .stream()
                .filter(k -> (k).equalsIgnoreCase(key))
                .findFirst();
    }
}
