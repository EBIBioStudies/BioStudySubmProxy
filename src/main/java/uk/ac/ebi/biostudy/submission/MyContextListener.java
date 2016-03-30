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
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.File;
import java.io.IOException;

@WebListener
public class MyContextListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(MyContextListener.class);

    @Override
    public void contextDestroyed(ServletContextEvent contextEvent) {
        MyContext.closeDb(contextEvent.getServletContext());
    }

    @Override
    public void contextInitialized(ServletContextEvent contextEvent) {
        ServletContext context = contextEvent.getServletContext();
        initConfig(context);
        initDb(context);
    }

    private void initDb(ServletContext context) {
        String classpath = getClass().getResource("/").getFile();
        File file = new File(new File(classpath) + "/submissiondb");

        logger.info("DB file storage: " + file.getAbsolutePath());
        MyContext.createDb(context, file);
        logger.info("DB initialized");
    }

    private void initConfig(ServletContext context) {
        try {
            MyContext.createConfig(context);
            logger.info("DB initialized");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}