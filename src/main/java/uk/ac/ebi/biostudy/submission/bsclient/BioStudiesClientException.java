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

package uk.ac.ebi.biostudy.submission.bsclient;

import org.json.JSONObject;

public class BioStudiesClientException extends Exception {

    private final int statusCode;

    private final String contentType;

    private final String result;

    public BioStudiesClientException(int statusCode, String result) {
        this.statusCode = statusCode;
        this.result = result;
    }

    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String getMessage() {
        JSONObject obj = new JSONObject();
        obj.append("statusCode", statusCode);
        obj.append("result", result);
        return obj.toString();
    }
}
