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

package uk.ac.ebi.biostudies.submissiontool.rest.resources.params;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import java.util.Collections;
import java.util.List;

/**
 * @author olkin
 */
public class SignUpParams {

    @JsonProperty()
    private String username;

    @JsonProperty()
    private String password;

    @JsonProperty()
    private String email;

    @JsonProperty()
    private List<String> aux;

    @JsonProperty("recaptcha2-response")
    private String captcha;

    @JsonProperty("path")
    @JsonView(SignUpParams.HiddenView.class)
    private String path;

    @JsonProperty("activationURL")
    @JsonView(SignUpParams.class)
    public String getPath() {
        return path;
    }

    public SignUpParams withPath(String newPath) {
        SignUpParams p = new SignUpParams();
        p.username = username;
        p.password = password;
        p.email = email;
        p.captcha = captcha;
        p.path = newPath;
        p.aux = Collections.unmodifiableList(aux);
        return p;
    }

    @Override
    public String toString() {
        return "SignUpParams{" +
                "username='" + username + '\'' +
                ", password='---'\'" +
                ", email='" + email + '\'' +
                ", aux='" + aux + '\'' +
                ", captcha='" + captcha + '\'' +
                ", path='" + path + '\'' +
                '}';
    }

    public static class HiddenView{}
}
