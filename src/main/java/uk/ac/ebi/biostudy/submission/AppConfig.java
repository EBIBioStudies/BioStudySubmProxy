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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import static uk.ac.ebi.biostudy.submission.AppConfig.ConfigProperty.SERVER_URL;

/**
 * @author Olga Melnichuk
 */
public class AppConfig {

    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    public static class AppConfigBuilder {
        private final AppConfig config;

        public AppConfigBuilder() {
            this.config = new AppConfig();
        }

        public AppConfigBuilder(AppConfig config) {
            this.config = new AppConfig(config);
        }

        public AppConfigBuilder setServerUrl(String value) {
            URI url = null;
            try {
                url = new URL(value).toURI();
            } catch (URISyntaxException | MalformedURLException e) {
                logger.error("Malformed URL parameter in config", e);
            }
            return setServerUrl(url);
        }

        public AppConfigBuilder setServerUrl(URI url) {
            config.setServerUrl(url);
            return this;
        }

        public AppConfig build() {
            return config;
        }
    }

    public static enum ConfigProperty {
        SERVER_URL("BS_SERVER_URL");

        private String name;

        ConfigProperty(String name) {
            this.name = name;
        }

        private String get(Properties properties) {
            String value = (String) properties.get(name);
            logger.debug("properties: " + name + "=" + value);
            return value;
        }

        private String get(ServletContext context) {
            String value = context.getInitParameter(name);
            logger.debug("context: " + name + "=" + value);
            return value;
        }
    }

    public static AppConfig loadConfig(ServletContext context) throws IOException {
        logger.info("Loading from context...");
        return new AppConfigBuilder()
                .setServerUrl(SERVER_URL.get(context))
                .build();
    }

    public static AppConfig loadConfig(InputStream input) throws IOException {
        logger.info("Loading from input stream...");
        Properties props = new Properties();
        if (input == null) {
            throw new IOException("Config file not found");
        }
        props.load(input);
        return new AppConfigBuilder()
                .setServerUrl(SERVER_URL.get(props))
                .build();
    }

    public static AppConfig defaultConfig() throws IOException {
        logger.info("Loading from classpath...");
        URL configURL = AppConfig.class.getClassLoader().getResource("/config.properties");
        logger.info("Config URL: " + configURL);
        return loadConfig(configURL == null ? null : configURL.openStream());
    }

    private AppConfig parent;

    private URI serverUrl;

    public AppConfig() {
    }

    public AppConfig(AppConfig parent) {
        this.parent = parent;
    }

    public URI getServerUrl() {
        return serverUrl == null ? getServerUrl(parent) : serverUrl;
    }

    private URI getServerUrl(AppConfig parent) {
        return parent == null ? null : parent.getServerUrl();
    }

    private void setServerUrl(URI serverUrl) {
        this.serverUrl = serverUrl;
    }

    public AppConfig overwrite(AppConfig config) {
        return new AppConfigBuilder(this)
                .setServerUrl(config.getServerUrl())
                .build();
    }

    @Override
    public String toString() {
        return "AppConfig{" +
                "serverUrl=" + getServerUrl() +
                '}';
    }
}
