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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.schedulers.Schedulers;
import uk.ac.ebi.biostudy.submission.bsclient.BioStudiesClient;

import javax.servlet.ServletContext;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Olga Melnichuk
 */
public class AppContext {

    private static final Logger logger = LoggerFactory.getLogger(AppContext.class);

    private static String CONFIG = "config";
    private static String BS_CLIENT = "bs_client";

    private final Map<String, Factory<?>> factories = new LinkedHashMap<>();

    {
        factories.put(CONFIG, new AppConfigFactory());
        factories.put(BS_CLIENT, new BioStudiesClientFactory());
    }

    public AppContext(ServletContext context) {
        init(context);
    }

    private void init(ServletContext context) {
        logger.info("init(...)");
        factories.keySet().forEach(key -> {
            Factory<?> factory = factories.get(key);
            context.setAttribute(key, factory.create(context));
        });
    }

    public void destroy() {
        logger.info("destroy(...): gracefully shutdown context created services..");
        factories.keySet().forEach(key -> factories.get(key).destroy());

        logger.info("destroy(...): gracefully shutdown rxjava schedulers...");
        Schedulers.shutdown();
    }

    public static AppConfig getConfig(ServletContext context) {
        return (AppConfig) context.getAttribute(CONFIG);
    }

    public static BioStudiesClient getBioStudiesClient(ServletContext context) {
        return (BioStudiesClient) context.getAttribute(BS_CLIENT);
    }
}