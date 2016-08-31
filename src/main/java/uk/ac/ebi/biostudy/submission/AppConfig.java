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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static java.lang.Boolean.parseBoolean;
import static uk.ac.ebi.biostudy.submission.AppConfig.ConfigProperty.*;

/**
 * @author Olga Melnichuk
 */
public class AppConfig {

    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    private static class AppConfigBuilder {
        private final AppConfig config;

        AppConfigBuilder() {
            this.config = new AppConfig();
        }

        AppConfigBuilder(AppConfig config) {
            this.config = new AppConfig(config);
        }

        AppConfigBuilder setServerUrl(String value) {
            URI url = null;
            try {
                if (value != null) {
                    url = new URL(value).toURI();
                }
            } catch (URISyntaxException | MalformedURLException e) {
                logger.error("Malformed URL parameter in config", e);
            }
            return setServerUrl(url);
        }

        AppConfigBuilder setOfflineMode(String value) {
            config.setOfflineMode(parseBoolean(value));
            return this;
        }

        AppConfigBuilder setOfflineUserDir(String value) {
            File dir = null;
            try {
                if (value != null) {
                    dir = new File(value);
                    if (!dir.exists()) {
                        Files.createDirectory(dir.toPath());
                    }
                    if (!dir.isDirectory()) {
                        dir = dir.getParentFile();
                    }
                }
            } catch (IOException e) {
                logger.error("Can't set user dir setting", e);
                dir = null;
            }
            config.setOfflineUserDir(dir == null ? null : dir.toPath());
            return this;
        }

        AppConfigBuilder setServerUrl(URI url) {
            config.setServerUrl(url);
            return this;
        }

        AppConfig build() {
            return config;
        }
    }

    enum ConfigProperty {
        SERVER_URL("BS_SERVER_URL"),
        OFFLINE_MODE("OFFLINE_MODE"),
        OFFLINE_USER_DIR("OFFLINE_USER_DIR");

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

    static AppConfig loadConfig(ServletContext context) throws IOException {
        logger.info("Loading from context...");
        return new AppConfigBuilder()
                .setServerUrl(SERVER_URL.get(context))
                .setOfflineMode(OFFLINE_MODE.get(context))
                .setOfflineUserDir(OFFLINE_USER_DIR.get(context))
                .build();
    }

    static AppConfig loadConfig(InputStream input) throws IOException {
        logger.info("Loading from input stream...");
        Properties props = new Properties();
        if (input == null) {
            throw new IOException("Config file not found");
        }
        props.load(input);
        return new AppConfigBuilder()
                .setServerUrl(SERVER_URL.get(props))
                .setOfflineMode(OFFLINE_MODE.get(props))
                .setOfflineUserDir(OFFLINE_USER_DIR.get(props))
                .build();
    }

    static AppConfig defaultConfig() throws IOException {
        logger.info("Loading from classpath...");
        URL configURL = AppConfig.class.getClassLoader().getResource("/config.properties");
        logger.info("Config URL: " + configURL);
        return loadConfig(configURL == null ? null : configURL.openStream());
    }

    private AppConfig parent;

    private URI serverUrl;
    private Boolean offlineMode;
    private Path userDir;

    private AppConfig() {
    }

    private AppConfig(AppConfig parent) {
        this.parent = parent;
    }

    public Path getUserDir() {
        return userDir == null ? getUserDir(parent) : userDir;
    }

    public URI getServerUrl() {
        return serverUrl == null ? getServerUrl(parent) : serverUrl;
    }

    public boolean isOfflineModeOn() {
        return offlineMode == null ? isOfflineModeOn(parent) : offlineMode;
    }

    private URI getServerUrl(AppConfig parent) {
        return parent == null ? null : parent.getServerUrl();
    }

    private Path getUserDir(AppConfig parent) {
        return parent == null ? null : parent.getUserDir();
    }

    private boolean isOfflineModeOn(AppConfig parent) {
        return parent != null && parent.isOfflineModeOn();
    }

    private void setServerUrl(URI serverUrl) {
        this.serverUrl = serverUrl;
    }

    private void setOfflineMode(Boolean offlineMode) {
        this.offlineMode = offlineMode;
    }

    private void setOfflineUserDir(Path path) {
        this.userDir = path;
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
