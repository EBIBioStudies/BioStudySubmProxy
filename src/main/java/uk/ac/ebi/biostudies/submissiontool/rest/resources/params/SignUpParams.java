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

/**
 * @author olkin
 */
public class SignUpParams extends WithPath<SignUpParams> {
    private String username;
    private String password;
    private String email;
    private String orcid;
    private String captcha;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getOrcid() {
        return orcid;
    }

    public String getCaptcha() {
        return captcha;
    }

    @Override
    public SignUpParams copyAll() {
        SignUpParams p = new SignUpParams();
        p.username = username;
        p.password = password;
        p.email = email;
        p.orcid = orcid;
        p.captcha = captcha;
        return p;
    }

    @Override
    public String toString() {
        return "SignUpParams{" +
                "username='" + username + '\'' +
                ", password='---'\'" +
                ", email='" + email + '\'' +
                ", orcid='" + orcid + '\'' +
                ", captcha='" + captcha + '\'' +
                ", path='" + getPath() + '\'' +
                '}';
    }

    public boolean hasOrcid() {
        return orcid != null && !orcid.isEmpty();
    }
}
