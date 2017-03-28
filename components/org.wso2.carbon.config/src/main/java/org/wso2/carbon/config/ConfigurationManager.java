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

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.config.provider.ConfigProviderImpl;
import org.wso2.carbon.config.reader.ConfigFileReader;
import org.wso2.carbon.config.reader.XMLBasedConfigFileReader;
import org.wso2.carbon.config.reader.YAMLBasedConfigFileReader;
import org.wso2.carbon.secvault.securevault.SecureVault;
import org.wso2.carbon.secvault.securevault.SecureVaultInitializer;
import org.wso2.carbon.secvault.securevault.exception.SecureVaultException;

import java.nio.file.Path;
import java.util.Optional;


/**
 * This class contains the functionality in managing configuration.
 */
public class ConfigurationManager {

    private static final String YAML_EXTENSION = "yaml";
    private static final String XML_EXTENSION = "xml";
    private static Logger logger = LoggerFactory.getLogger(ConfigurationManager.class);
    private static ConfigurationManager instance = new ConfigurationManager();
    private ConfigProvider configProvider = null;
    private SecureVault secureVault = null;

    /**
     * Private constructor for ConfigurationManager class. This is a singleton class, thus the constructor is private.
     */
    private ConfigurationManager() {
    }

    /**
     * Returns the singleton instance of ConfigurationManager.
     *
     * @return ConfigurationManager
     */
    public static ConfigurationManager getInstance() {
        return instance;
    }

    /**
     * Initializes and returns configuration provider service from the configuration file provided.
     * If service already exists, return initialized service object without reinitializing the service.
     *
     * @param filePath configuration absolute filepath(e.g: {carbon-home}/conf/deployment.yaml})
     * @return configProvider service object.
     * @throws ConfigurationException if an error occurred while initializing the config provider.
     */
    public ConfigProvider iniConfigProvider(Path filePath) throws ConfigurationException {
        loadSecureVaultService();
        return initConfigProvider(filePath, secureVault);
    }

    /**
     * Initializes and returns configuration provider service from the configuration file provided.
     * If service exists and force initialize is not enabled, return initialized service object without reinitializing
     * the object.
     *
     * @param filePath configuration absolute filepath(e.g: {carbon-home}/conf/deployment.yaml})
     * @param forceInitEnabled flag to enable force reinitialize configuration provider.
     * @return configProvider service object.
     * @throws ConfigurationException if an error occurred while initializing the config provider.
     */
    public ConfigProvider iniConfigProvider(Path filePath, boolean forceInitEnabled) throws ConfigurationException {
        loadSecureVaultService();
        return initConfigProvider(filePath, secureVault, forceInitEnabled);
    }

    /**
     * Initializes and returns configuration provider service with the provided configuration file.
     * If service exists, return initialized service object without reinitializing the object.
     *
     * @param filePath configuration absolute filepath(e.g: {carbon-home}/conf/deployment.yaml})
     * @param secureVault {@code SecureVault>}
     * @return configProvider service object
     * @throws ConfigurationException if an error occurred while initializing the config provider.
     */
    public ConfigProvider initConfigProvider(Path filePath, SecureVault secureVault) throws ConfigurationException {
        return initConfigProvider(filePath, secureVault, Boolean.FALSE);
    }


    /**
     * Initializes and returns configuration provider service with the provided configuration file.
     * If filepath is not specified, returns null.
     * If service exists and force initialize is not enabled, return initialized service object without reinitializing
     * the object.
     *
     * @param filePath configuration absolute filepath(e.g: {carbon-home}/conf/deployment.yaml})
     * @param secureVault {@code SecureVault>}
     * @param forceInitEnabled flag to enable force reinitialize configuration provider.
     * @return configProvider service object
     * @throws ConfigurationException if an error occurred while initializing the config provider.
     */
    public ConfigProvider initConfigProvider(Path filePath, SecureVault secureVault, boolean forceInitEnabled) throws
            ConfigurationException {
        // check whether configuration provider is already initialized.
        // if force flag is disabled and provider already exists, returns the same object.
        if (!forceInitEnabled && this.configProvider != null) {
            logger.info("Configuration provider is already initialized. Returning the same provider without " +
                    "reinitializing.");
            return configProvider;
        }

        //check whether configuration filepath is null. proceed if not null.
        if (filePath == null) {
            throw new ConfigurationException("No configuration filepath is provided. configuration provider will " +
                    "not be initialized!");
        }
        //check whether securevault is null. proceed if not null.
        if (secureVault == null) {
            throw new ConfigurationException("No securevault service found. configuration provider will not be " +
                    "initialized!");
        }
        this.secureVault = secureVault;

        // initialize config provider service from the configuration file provided.
        String fileExtension = FilenameUtils.getExtension(filePath.toString());
        ConfigFileReader configFileReader;
        if (YAML_EXTENSION.equalsIgnoreCase(fileExtension)) {
            configFileReader = new YAMLBasedConfigFileReader(filePath);
        } else if (XML_EXTENSION.equalsIgnoreCase(fileExtension)) {
            configFileReader = new XMLBasedConfigFileReader(filePath);
        } else {
            throw new ConfigurationException("Error while initializing configuration provider, file extension:" +
                    fileExtension + " is not supported");
        }

        return configProvider = new ConfigProviderImpl(configFileReader, secureVault);
    }

    /**
     * Load {@code SecureVault} implementation in non-osgi mode.
     *
     * @throws ConfigurationException if an error occurred while loading securevault service.
     */
    private void loadSecureVaultService() throws ConfigurationException {
        if (secureVault == null) {
            try {
                this.secureVault = SecureVaultInitializer.getInstance().initializeSecureVault();
            } catch (SecureVaultException e) {
                throw new ConfigurationException("Error while loading securevault service", e);
            }
        }
    }

    /**
     * Returns configuration provider service object.
     *
     * @return configProvider service object
     */
    public Optional<ConfigProvider> getConfigProvider() {
        return Optional.ofNullable(configProvider);
    }
}
