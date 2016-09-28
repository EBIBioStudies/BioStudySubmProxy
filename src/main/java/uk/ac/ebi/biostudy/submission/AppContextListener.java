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

import org.slf4j.bridge.SLF4JBridgeHandler;
import uk.ac.ebi.biostudy.submission.context.AppContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {

    private AppContext appContext;

    @Override
    public void contextInitialized(ServletContextEvent contextEvent) {
        initJulLogger();
        appContext = new AppContext(contextEvent.getServletContext());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        appContext.destroy();
    }

    private void initJulLogger() {
        // JUL -> SLF4j bridge init
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }
}
