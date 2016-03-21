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

package uk.ac.ebi.biostudy.submission;

import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author Olga Melnichuk
 */
public class Transformers {

    private Transformers() {
    }

    public static RequestTransform transformActivationReq() {
        return req -> {
            JSONObject obj = toJson(readRequestBody(req));
            if (obj.has("key")) {
                String key = obj.getString("key");
                String urlBackend = "/auth/activate/" + key;
                return new GetMethod(urlBackend);
            }
            throw new BadRequestException("Bad activation request");
        };
    }


    private static JSONObject toJson(String str) {
        return new JSONObject(str);
    }

    private static String readRequestBody(HttpServletRequest request) throws IOException {
        return request.getReader().lines()
                .reduce("", (accumulator, actual) -> accumulator + actual);
    }
}
