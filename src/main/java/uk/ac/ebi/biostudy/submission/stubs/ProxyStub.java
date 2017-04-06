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

package uk.ac.ebi.biostudy.submission.stubs;


import uk.ac.ebi.biostudy.submission.proxy.Proxy;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ProxyStub /*implements Proxy*/ {

  /*  private static Map<String, JSONObject> paths = new HashMap<String, JSONObject>() {
        {
            put("/auth/signin", new JSONObject()
                    .put("status", "OK")
                    .put("sessid", Session.ID)
                    .put("username", "Dev")
                    .put("email", "dev@nomail.com"));
        }
    };

    @Override
    public void proxyGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }

    @Override
    public void proxyPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Optional<String> path = paths.keySet().stream().filter(key ->
                req.getRequestURI().matches(".*" + key + ".*")
        ).findFirst();

        if (path.isPresent()) {
            responseWith(paths.get(path.get()), resp);
        } else {
            responseWith404(resp);
        }
    }

    private void responseWith(JSONObject json, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        out.print(json);
        out.flush();
    }

    private void responseWith404(HttpServletResponse resp) throws IOException {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }*/


}
