/*
 * Copyright (c) 2017 European Molecular Biology Laboratory
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

package uk.ac.ebi.biostudies.submissiontool.context;

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
import java.util.stream.Stream;

import static java.lang.Boolean.parseBoolean;

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

        void set(ConfigProperty cp, String value) {
            cp.set(this, value);
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

        AppConfigBuilder setServerUrl(URI url) {
            config.setServerUrl(url);
            return this;
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
        AppConfig build() {
            return config;
        }
    }

    enum ConfigProperty {
        SERVER_URL("BS_SERVER_URL") {
            @Override
            void set(AppConfigBuilder builder, String value) {
                builder.setServerUrl(value);
            }
        },
        OFFLINE_MODE("OFFLINE_MODE") {
            @Override
            void set(AppConfigBuilder builder, String value) {
                builder.setOfflineMode(value);
            }
        },
        OFFLINE_USER_DIR("OFFLINE_USER_DIR") {
            @Override
            void set(AppConfigBuilder builder, String value) {
                builder.setOfflineUserDir(value);
            }
        };

        private String name;

        ConfigProperty(String name) {
            this.name = name;
        }

        abstract void set(AppConfigBuilder builder, String value);
    }

    private static abstract class ConfigSource<T> {
        private final T source;

        ConfigSource(T source) {
            this.source = source;
        }

        String read(String name) {
            return read(name, source);
        }

        abstract String read(String name, T source);
    }

    private static class PropertiesConfigSource extends ConfigSource<Properties> {

        PropertiesConfigSource(Properties source) {
            super(source);
        }

        @Override
        public String read(String name, Properties source) {
            String value = (String) source.get(name);
            logger.debug("properties: " + name + "=" + value);
            return value;
        }
    }

    private static class ContextConfigSource extends ConfigSource<ServletContext> {

        ContextConfigSource(ServletContext source) {
            super(source);
        }

        @Override
        public String read(String name, ServletContext source) {
            String value = source.getInitParameter(name);
            logger.debug("context: " + name + "=" + value);
            return value;
        }
    }

    public static AppConfig loadConfig(ServletContext context) throws IOException {
        logger.info("Loading from context...");
        ContextConfigSource  configSource = new ContextConfigSource(context);
        return buildConfig(configSource);
    }

    public static AppConfig loadConfig(InputStream input) throws IOException {
        logger.info("Loading from input stream...");
        Properties props = new Properties();
        if (input == null) {
            throw new IOException("Config file not found");
        }
        props.load(input);
        PropertiesConfigSource  configSource = new PropertiesConfigSource(props);
        return buildConfig(configSource);
    }

    static <T extends ConfigSource> AppConfig buildConfig(T configSource) {
        AppConfigBuilder builder = new AppConfigBuilder();
        Stream.of(ConfigProperty.values())
                .forEach(p -> {
                    String value = configSource.read(p.name);
                    builder.set(p, value);
                });
        return builder.build();
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

    AppConfig overwrite(AppConfig config) {
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
