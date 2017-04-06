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
import org.wso2.carbon.config.samples.common.ParentConfiguration;
import org.wso2.carbon.secvault.SecureVaultConstants;
import org.wso2.carbon.secvault.SecureVaultUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Main class for Carbon Config non-OSGi class.
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
        deleteCopiedFiles(); // Delete copied files
    }

    /**
     * Delete created files.
     * This method will delete the previously created files
     */
    private static void deleteCopiedFiles() {
        try {
            Files.deleteIfExists(Paths.get(FILE_FOLDER, "conf", FILE_NAME));
            Files.deleteIfExists(Paths.get(FILE_FOLDER, "conf"));

            // Delete secure vault files
            Files.deleteIfExists(Paths.get(FILE_FOLDER, "resources", "security", "wso2carbon.jks"));
            Files.deleteIfExists(Paths.get(FILE_FOLDER, "resources", "security"));
            Files.deleteIfExists(Paths.get(FILE_FOLDER, "resources"));
            Files.deleteIfExists(Paths.get(FILE_FOLDER, "securevault", "conf", SecureVaultConstants
                    .MASTER_KEYS_FILE_NAME));
            Files.deleteIfExists(Paths.get(FILE_FOLDER, "securevault", "conf", SecureVaultConstants
                    .SECRETS_PROPERTIES_FILE_NAME));
            Files.deleteIfExists(Paths.get(FILE_FOLDER, "securevault", "conf"));
            Files.deleteIfExists(Paths.get(FILE_FOLDER, "securevault"));
            Files.deleteIfExists(Paths.get(FILE_FOLDER));
        } catch (IOException e) {
            logger.error("Error in deleting files", e);
        }
    }

    /**
     * Copy required files to the current working directory for demo purpose.
     * This method will only copy the files out of from resources for demonstration purpose.
     */
    private static void copyFilesToPWD() {
        String[] deploymentYamlPath = {"conf", FILE_NAME};
        String[] masterKeysPaths = {"securevault", "conf", SecureVaultConstants.MASTER_KEYS_FILE_NAME};
        String[] secretPropertiesPath = {"securevault", "conf", SecureVaultConstants.SECRETS_PROPERTIES_FILE_NAME};
        String[] jksResourcePath = {"resources", "security", "wso2carbon.jks"};

        // Copy config files
        try {
            Files.createDirectories(Paths.get(FILE_FOLDER, "conf"));
            Files.createDirectories(Paths.get(FILE_FOLDER, "securevault", "conf"));
            Files.createDirectories(Paths.get(FILE_FOLDER, "resources", "security"));
        } catch (IOException e) {
            logger.error("Error occurred in creating directories", e);
        }

        try (InputStream deploymentYamlInputStream = getResourceInputStream(deploymentYamlPath)
                .orElseThrow(() -> new IOException("Error in copying " + FILE_NAME));
             InputStream masterKeyInputStream = getResourceInputStream(masterKeysPaths)
                     .orElseThrow(() -> new IOException("Error in copying " +
                             SecureVaultConstants.MASTER_KEYS_FILE_NAME));
             InputStream secretPropertiesInputStream = getResourceInputStream(secretPropertiesPath)
                     .orElseThrow(() -> new IOException("Error in copying " +
                             SecureVaultConstants.SECRETS_PROPERTIES_FILE_NAME));
             InputStream jksInputStream = getResourceInputStream(jksResourcePath)
                     .orElseThrow(() -> new IOException("Error in copying file wso2carbon.jks"))) {
            Files.copy(deploymentYamlInputStream, Paths.get(FILE_FOLDER, deploymentYamlPath));
            // Copy secure vault files
            Files.copy(masterKeyInputStream, Paths.get(FILE_FOLDER, masterKeysPaths));
            Files.copy(secretPropertiesInputStream, Paths.get(FILE_FOLDER, secretPropertiesPath));
            Files.copy(jksInputStream, Paths.get(FILE_FOLDER, jksResourcePath));
        } catch (IOException e) {
            logger.error("Error occurred in copying files", e);
        }
    }

    /**
     * Get input stream from the given resource.
     *
     * @param resourcePaths resource paths
     * @return input stream of the resource
     */
    private static Optional<InputStream> getResourceInputStream(String... resourcePaths) {
        InputStream inputStream = SecureVaultUtils.class.getClassLoader()
                .getResourceAsStream(Paths.get("",
                        resourcePaths).toString());
        return Optional.ofNullable(inputStream);
    }
}
