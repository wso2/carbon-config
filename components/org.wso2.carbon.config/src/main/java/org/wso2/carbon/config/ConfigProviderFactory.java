/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.config.provider.ConfigProviderImpl;
import org.wso2.carbon.config.reader.ConfigFileReader;
import org.wso2.carbon.config.reader.XMLBasedConfigFileReader;
import org.wso2.carbon.config.reader.YAMLBasedConfigFileReader;
import org.wso2.carbon.secvault.SecureVault;
import org.wso2.carbon.secvault.SecureVaultFactory;
import org.wso2.carbon.secvault.exception.SecureVaultException;

import java.nio.file.Path;


/**
 * This factory class will initialize and return configProvider instance.
 *
 * @since 1.0.0
 */
public class ConfigProviderFactory {

    private static final String YAML_EXTENSION = ".yaml";
    private static final String XML_EXTENSION = ".xml";
    private static Logger logger = LoggerFactory.getLogger(ConfigProviderFactory.class);

    /**
     * Initializes and returns configuration provider service from the configuration file provided.
     *
     * @param filePath configuration absolute filepath(e.g: {carbon-home}/conf/deployment.yaml})
     * @return configProvider service object.
     * @throws ConfigurationException if an error occurred while initializing the config provider.
     */
    public static ConfigProvider getConfigProvider(Path filePath) throws ConfigurationException {
        return getConfigProvider(filePath, getSecureVaultService(filePath));
    }

    /**
     * Initializes and returns configuration provider service with the provided configuration file.
     *
     * @param filePath configuration absolute filepath(e.g: {carbon-home}/conf/deployment.yaml})
     * @param secureVault {@code SecureVault>}
     * @return configProvider service object
     * @throws ConfigurationException if filepath == null or securevault == null or configuration file extension is
     * not equal to xml or yaml.
     */
    public static ConfigProvider getConfigProvider(Path filePath, SecureVault secureVault) throws
            ConfigurationException {
        //check whether configuration filepath is null. proceed if not null.
        if (filePath == null || !filePath.toFile().exists()) {
            throw new ConfigurationException("No configuration filepath is provided. configuration provider will " +
                    "not be initialized!");
        }
        //check whether securevault is null. proceed if not null.
        if (secureVault == null) {
            throw new ConfigurationException("No securevault service found. configuration provider will not be " +
                    "initialized!");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("initialize config provider instance from configuration file: " + filePath.toString());
        }
        // initialize config provider service from the configuration file provided.
        ConfigFileReader configFileReader;
        if (filePath.toString().endsWith(YAML_EXTENSION)) {
            configFileReader = new YAMLBasedConfigFileReader(filePath);
        } else if (filePath.toString().endsWith(XML_EXTENSION)) {
            configFileReader = new XMLBasedConfigFileReader(filePath);
        } else {
            throw new ConfigurationException("Error while initializing configuration provider, file extension is not " +
                    "supported");
        }
        return new ConfigProviderImpl(configFileReader, secureVault);
    }

    /**
     * Load {@code SecureVault} implementation in non-osgi mode.
     *
     * @param filePath configuration absolute filepath(e.g: {carbon-home}/conf/deployment.yaml})
     * @throws ConfigurationException if an error occurred while loading securevault service.
     */
    private static SecureVault getSecureVaultService(Path filePath) throws ConfigurationException {
        //check whether configuration filepath is null. proceed if not null.
        if (filePath == null || !filePath.toFile().exists()) {
            throw new ConfigurationException("Configuration filepath is not provided. configuration provider will " +
                    "not be initialized!");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("initialize securevault instance from configuration file: " + filePath.toString());
        }
        try {
            return SecureVaultFactory.getSecureVault(filePath).orElseThrow(() -> new ConfigurationException("Error " +
                    "while loading securevault service"));
        } catch (SecureVaultException e) {
            throw new ConfigurationException("Error while loading securevault service", e);
        }
    }
}
