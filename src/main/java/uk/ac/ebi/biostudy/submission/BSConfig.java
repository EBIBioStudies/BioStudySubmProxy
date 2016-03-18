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
import java.net.URL;
import java.util.Properties;

/**
 * @author Olga Melnichuk
 */
public class BSConfig {

    private static final Logger logger = LoggerFactory.getLogger(ProxyServlet.class);

    public static BSConfig get(InputStream input) throws IOException {
        Properties props = new Properties();
        if (input != null) {
            props.load(input);
            URL serverUrl = new URL(props.getProperty("BS_SERVER_URL"));
            return new BSConfig(serverUrl);
        }
        throw new IOException("Property file was not found");
    }

    public static BSConfig get() throws IOException {
        logger.info("Loading config.properties from a classpath");
        return get(BSConfig.class.getClassLoader().getResourceAsStream("/config.properties"));
    }

    private final URL serverUrl;

    private BSConfig(URL serverUrl) {
        this.serverUrl = serverUrl;
    }

    public URL getServerUrl() {
        return serverUrl;
    }
}
