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

package uk.ac.ebi.biostudy.submission.context;

import javax.servlet.ServletContext;
import java.io.IOException;

import static uk.ac.ebi.biostudy.submission.context.AppConfig.defaultConfig;
import static uk.ac.ebi.biostudy.submission.context.AppConfig.loadConfig;

class AppConfigFactory implements Factory<AppConfig> {

    @Override
    public AppConfig create(ServletContext context) {
        try {
            return defaultConfig().overwrite(loadConfig(context));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void destroy() {
    }
}

