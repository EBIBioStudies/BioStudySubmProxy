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

import org.json.JSONArray;

import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MyJSONArray {
    private final JSONArray arr;

    public MyJSONArray(JSONArray arr) {
        this.arr = arr;
    }

    public int length() {
        return arr.length();
    }

    public Stream<MyJSONObject> getMyJSONObjects() {
        return IntStream.range(0, arr.length()).mapToObj(i -> new MyJSONObject(arr.getJSONObject(i)));
    }
}
