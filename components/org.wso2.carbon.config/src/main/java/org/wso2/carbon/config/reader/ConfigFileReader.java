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
package org.wso2.carbon.config.reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigurationException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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
        String customConfig = System.getProperty("config");
        String customConfigContent;
        if (customConfig != null && (!customConfig.trim().isEmpty())) {
            try {
                File customDeploymentFile = new File(customConfig);
                if (customDeploymentFile.isFile()) {
                    log.info("Default deployment configuration updated with provided custom configuration file " +
                            customDeploymentFile.getName());
                    customConfigContent = getStringContentFromFile(customDeploymentFile);
                } else {
                    customConfigContent = customConfig;
                }
                List<String> configContentList = new ArrayList<>();
                File defaultConfigFile = new File(configurationFilePath.toString());
                String defaultConfigContent = getStringContentFromFile(defaultConfigFile);
                configContentList.add(defaultConfigContent);
                configContentList.add(customConfigContent);
                YmlMerger ymlMerger = new YmlMerger();
                ymlMerger.setVariablesToReplace(System.getenv());
               return ymlMerger.mergeToString(configContentList);
            } catch (IOException e) {
                String message = "Error occurred while overriding the default deployment configuration with provided" +
                        "custom configurations.";
                log.error(message);
                throw new ConfigurationException(message, e);
            }
        } else {
            try {
                byte[] contentBytes = Files.readAllBytes(configurationFilePath);
                return new String(contentBytes, StandardCharsets.UTF_8);
            } catch (IOException e) {
                String message = "Error while reading configuration file";
                log.error(message, e);
                throw new ConfigurationException(message, e);
            }
        }
    }

    private String getStringContentFromFile(File inputFile) throws IOException {
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        StringBuilder inputSB = new StringBuilder();
        try {
            inputStream = new FileInputStream(inputFile);
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line = bufferedReader.readLine();
            while (line != null) {
                inputSB.append(line);
                line = bufferedReader.readLine();
                if (line != null) {
                    // add new line character
                    inputSB.append("\n");
                }
            }
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return inputSB.toString();
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
