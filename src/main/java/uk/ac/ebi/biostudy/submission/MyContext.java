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

import org.mapdb.DB;
import org.mapdb.DBMaker;
import uk.ac.ebi.biostudy.submission.proxy.Proxy;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;

/**
 * @author Olga Melnichuk
 */
public class MyContext {

    private static final String DB = "db";
    private static final String CONFIG = "config";


    public static void createDb(ServletContext context, File file) {
        DB db = DBMaker.fileDB(file).closeOnJvmShutdown().cacheSize(128).transactionDisable().make();
        context.setAttribute(DB, db);
    }

    public static DB getDb(ServletContext context) {
        return (DB) context.getAttribute(DB);
    }

    public static void closeDb(ServletContext context) {
        DB db = getDb(context);
        if (db != null) {
            db.close();
        }
    }

    public static void createConfig(ServletContext context) throws IOException {
        context.setAttribute(CONFIG, MyConfig.get());
    }

    public static MyConfig getConfig(ServletContext context) {
        return (MyConfig) context.getAttribute(CONFIG);
    }

    public static Proxy getProxy(ServletContext context) {
        return new Proxy(getConfig(context).getServerUrl());

    }

}
