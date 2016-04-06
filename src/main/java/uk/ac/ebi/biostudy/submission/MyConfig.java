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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import java.io.File;

/**
 * @author Olga Melnichuk
 */
public class MyConfig {

    private static final Logger logger = LoggerFactory.getLogger(MyConfig.class);

    public static MyConfig get(InputStream input) throws IOException {
        Properties props = new Properties();
        if (input == null) {
            throw new IOException("Config file not found");
        }
        props.load(input);
        try {
            URI serverUrl = new URL(props.getProperty("BS_SERVER_URL")).toURI();
            logger.info("serverUrl: " + serverUrl);

            File dbPath = new File(props.getProperty("BS_TMP_DB_PATH"));
            logger.info("dbPath: " + dbPath);

            return new MyConfig(serverUrl, dbPath);
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    public static MyConfig get() throws IOException {
        URL configURL = MyConfig.class.getClassLoader().getResource("/config.properties");
        logger.info("Config URL: " + configURL);
        return get(configURL == null ? null : configURL.openStream());
    }

    private final URI serverUrl;
    private final File dbPath;


    private MyConfig(URI serverUrl, File dbPath) {
        this.serverUrl = serverUrl;
        this.dbPath = dbPath;
    }

    public URI getServerUrl() {
        return serverUrl;
    }

    public File getDbPath() {
        return dbPath;
    }
}
