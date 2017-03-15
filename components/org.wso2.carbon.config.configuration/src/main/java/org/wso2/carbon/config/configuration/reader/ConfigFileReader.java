/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.config.configuration.reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.configuration.ConfigurationException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Interface for reading a configuration file.
 *
 * @since 1.0.0
 */
public abstract class ConfigFileReader {

    private static final Logger log = LoggerFactory.getLogger(ConfigFileReader.class);
    private Path configurationFilePath;

    public ConfigFileReader(Path configurationFilePath) {
        this.configurationFilePath = configurationFilePath;
    }

    /**
     * Returns a populated Deployment Configuration Map which overrides default configuration.
     *
     * @return a instance of the Configuration Map, key: String, value: YAML string
     * @throws ConfigurationException if error occur while reading the configuration file.
     */
    public abstract Map<String, String> getDeploymentConfiguration()
            throws ConfigurationException;

    /**
     * Get contents of the file as a string.
     *
     * @return contents of the file as a string
     * @throws ConfigurationException if file name is null or on error on reading file
     */
    public final String getFileContent() throws ConfigurationException {
        if (configurationFilePath == null) {
            String message = "Error while reading the configuration file, file path is null";
            log.error(message);
            throw new ConfigurationException(message);
        }
        try {
            byte[] contentBytes = Files.readAllBytes(configurationFilePath);
            return new String(contentBytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            String message = "Error while reading configuration file";
            log.error(message, e);
            throw new ConfigurationException(message, e);
        }
    }

    /**
     * Get configuration file path.
     *
     * @return configuration file path
     */
    public Path getConfigurationFilePath() {
        return configurationFilePath;
    }
}
