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

import uk.ac.ebi.biostudy.submission.context.AppConfig;

import java.io.IOException;
import java.net.URI;

import static uk.ac.ebi.biostudy.submission.context.AppConfig.loadConfig;

/**
 * @author Olga Melnichuk
 */
public class TestEnvironment {

    public static boolean hasValidServerUrl() {
        try {
            getServerUrl();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static AppConfig getConfig() throws IOException {
        return loadConfig(TestEnvironment.class.getResourceAsStream("/config.properties"));
    }

    public static URI getServerUrl() throws IOException {
        return getConfig().getServerUrl();
    }
}
