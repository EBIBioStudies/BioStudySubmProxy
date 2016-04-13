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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.IOException;

import static uk.ac.ebi.biostudy.submission.AppConfig.defaultConfig;
import static uk.ac.ebi.biostudy.submission.AppConfig.loadConfig;

/**
 * @author Olga Melnichuk
 */
public class AppContext {

    private static final String CONFIG = "config";

    private static final Logger logger = LoggerFactory.getLogger(AppContextListener.class);

    public static AppConfig createConfig(ServletContext context) throws IOException {
        AppConfig config =
                defaultConfig()
                .overwrite(loadConfig(context));
        context.setAttribute(CONFIG, config);
        logger.info(config.toString());
        return config;
    }

    public static AppConfig getConfig(ServletContext context) {
        return (AppConfig) context.getAttribute(CONFIG);
    }
}
