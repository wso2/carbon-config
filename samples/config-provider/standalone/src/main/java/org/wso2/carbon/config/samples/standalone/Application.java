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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.config.samples.standalone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigProviderFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.config.samples.ParentConfiguration;
import org.wso2.carbon.secvault.SecureVaultConstants;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Main class for Carbon Config non-OSGi sample.
 */
public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    private static final String FILE_FOLDER = "resources";
    private static final String FILE_NAME = "deployment.yaml";

    public static void main(String[] args) {
        copyFilesToPWD(); // Copies required files to the current working directory
        // Create new configuration provider
        Path deploymentConfigPath = Paths.get(FILE_FOLDER, "conf", FILE_NAME);

        // Get configuration
        try {
            ConfigProvider configProvider = ConfigProviderFactory.getConfigProvider(deploymentConfigPath);
            ParentConfiguration parentConfiguration = configProvider
                    .getConfigurationObject(ParentConfiguration.class);
            logger.info("Parent configuration - {}", parentConfiguration);
        } catch (ConfigurationException e) {
            logger.error("Error in getting configuration", e);
        }
    }


    /**
     * Copy required files to the current working directory for demo purpose.
     * This method will only copy the files out of from security for demonstration purpose.
     */
    private static void copyFilesToPWD() {

        Path deploymentYamlPath = Paths.get("conf", FILE_NAME);
        Path masterKeysPaths = Paths.get("conf", SecureVaultConstants.MASTER_KEYS_FILE_NAME);
        Path secretPropertiesPath = Paths.get("conf", SecureVaultConstants.SECRETS_PROPERTIES_FILE_NAME);
        Path jksResourcePath = Paths.get("security", "wso2carbon.jks");

        // Copy config files
        try {
            Files.createDirectories(Paths.get(FILE_FOLDER, "conf"));
            Files.createDirectories(Paths.get(FILE_FOLDER, "security"));
        } catch (IOException e) {
            logger.error("Error occurred in creating directories", e);
        }

        // copy deployment.yaml file to resources/conf directory
        if (Files.notExists(Paths.get(FILE_FOLDER, deploymentYamlPath.toString()))) {
            try (InputStream deploymentYamlInputStream = getResourceInputStream(deploymentYamlPath)
                    .orElseThrow(() -> new IOException("Error in copying " + FILE_NAME))) {
                Files.copy(deploymentYamlInputStream, Paths.get(FILE_FOLDER, deploymentYamlPath.toString()));
            } catch (IOException e) {
                logger.error("Error occurred in copying files", e);
            }
        }

        // copy master-keys.yaml file to resources/conf directory
        if (Files.notExists(Paths.get(FILE_FOLDER, masterKeysPaths.toString()))) {
            try (InputStream masterKeyInputStream = getResourceInputStream(masterKeysPaths)
                    .orElseThrow(() -> new IOException("Error in copying " +
                            SecureVaultConstants.MASTER_KEYS_FILE_NAME))) {
                Files.copy(masterKeyInputStream, Paths.get(FILE_FOLDER, masterKeysPaths.toString()));
            } catch (IOException e) {
                logger.error("Error occurred in copying files", e);
            }
        }

        // copy secrete.properties file to resources/conf directory
        if (Files.notExists(Paths.get(FILE_FOLDER, secretPropertiesPath.toString()))) {
            try (InputStream secretPropertiesInputStream = getResourceInputStream(secretPropertiesPath)
                    .orElseThrow(() -> new IOException("Error in copying " +
                            SecureVaultConstants.SECRETS_PROPERTIES_FILE_NAME))) {
                Files.copy(secretPropertiesInputStream, Paths.get(FILE_FOLDER, secretPropertiesPath.toString()));
            } catch (IOException e) {
                logger.error("Error occurred in copying files", e);
            }
        }

        // copy wso2carbon.jks file to resources/security directory
        if (Files.notExists(Paths.get(FILE_FOLDER, jksResourcePath.toString()))) {
            try (InputStream jksInputStream = getResourceInputStream(jksResourcePath)
                    .orElseThrow(() -> new IOException("Error in copying file wso2carbon.jks"))) {
                Files.copy(jksInputStream, Paths.get(FILE_FOLDER, jksResourcePath.toString()));
            } catch (IOException e) {
                logger.error("Error occurred in copying files", e);
            }
        }
    }

    /**
     * Get input stream from the given resource.
     *
     * @param resourcePaths resource paths
     * @return input stream of the resource
     */
    private static Optional<InputStream> getResourceInputStream(Path resourcePaths) {
        InputStream inputStream = Application.class.getClassLoader()
                .getResourceAsStream(resourcePaths.toString());
        return Optional.ofNullable(inputStream);
    }
}
