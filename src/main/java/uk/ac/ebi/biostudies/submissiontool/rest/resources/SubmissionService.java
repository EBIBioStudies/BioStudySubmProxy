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
package uk.ac.ebi.biostudies.submissiontool.rest.resources;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

public class SubmissionService {

    private static final Logger logger = LoggerFactory.getLogger(SubmissionService.class);

    public Observable<String> helloRx(String name) {
        logger.debug("helloRx({})", name);
        ObjectNode resp = JsonNodeFactory.instance.objectNode();
        resp.put("message", "Hello " + name + "!");
        return Observable.from(new String[]{resp.toString()});
    }
}
