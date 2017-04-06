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
package org.wso2.carbon.config.configprovider;

import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.config.ConfigProviderFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.secvault.SecureVault;
import org.wso2.carbon.secvault.exception.SecureVaultException;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class is to test uses of the ConfigProviderFactory.
 *
 * @since 1.0.0
 */
public class ConfigProviderFactoryTest {
    private SecureVault secureVault;
    private static final String PASSWORD = "n3wP4s5w0r4";
    private Path configPath;

    @BeforeTest
    public void setup() throws ConfigurationException {
        if (configPath == null) {
            URL resourceUrl = this.getClass().getClassLoader().getResource("conf");
            if (resourceUrl == null) {
                throw new ConfigurationException("Config path in resources not found");
            }
            configPath = Paths.get(resourceUrl.getPath());
        }

        secureVault = EasyMock.mock(SecureVault.class);
        try {
            EasyMock.expect(secureVault.resolve(EasyMock.anyString())).andReturn(PASSWORD.toCharArray()).anyTimes();
        } catch (SecureVaultException e) {
            throw new ConfigurationException("Error resolving secure vault", e);
        }
        EasyMock.replay(secureVault);
    }

    @Test(description = "test case when file path is not provided, when getting config provider", expectedExceptions
            = ConfigurationException.class, expectedExceptionsMessageRegExp = "No configuration filepath is provided." +
            " configuration provider will not be initialized!")
    public void filePathNotProvidedTestCase() throws ConfigurationException {
        ConfigProviderFactory.getConfigProvider(null, secureVault);
    }

    @Test(description = "test case when file path is incorrect, when getting config provider", expectedExceptions
            = ConfigurationException.class, expectedExceptionsMessageRegExp = "No configuration filepath is provided." +
            " configuration provider will not be initialized!")
    public void incorrectFilePathTestCase() throws ConfigurationException {
        ConfigProviderFactory.getConfigProvider(getFilePath("incorrectfilepath.yaml"), secureVault);
    }

    @Test(description = "test case when securevault is not provided, when getting config provider", expectedExceptions
            = ConfigurationException.class, expectedExceptionsMessageRegExp = "No securevault service found. " +
            "configuration provider will not be initialized!")
    public void secureVaultNotProvidedTestCase() throws ConfigurationException {
        ConfigProviderFactory.getConfigProvider(getFilePath("Example.yaml"), null);
    }

    @Test(description = "test case for xml configuration file")
    public void xmlConfigFileTestCase() throws ConfigurationException {
        ConfigProvider configProvider = ConfigProviderFactory.getConfigProvider(getFilePath("Example.xml"),
                secureVault);
        Assert.assertNotNull(configProvider, "Configuration provider cannot be null");
    }

    @Test(description = "test case for yaml configuration file")
    public void yamlConfigFileTestCase() throws ConfigurationException {
        ConfigProvider configProvider = ConfigProviderFactory.getConfigProvider(getFilePath("Example.yaml"),
                secureVault);
        Assert.assertNotNull(configProvider, "Configuration provider cannot be null");
    }

    @Test(description = "test case for .txt configuration file. ", expectedExceptions = ConfigurationException.class,
            expectedExceptionsMessageRegExp = "Error while initializing configuration provider, file extension is not" +
                    " supported")
    public void invalidConfigFileTestCase() throws ConfigurationException {
        ConfigProviderFactory.getConfigProvider(getFilePath("Example.txt"),
                secureVault);
    }

    /**
     * Get file from resources.
     *
     * @param fileName name of the file
     * @return file path
     */
    private Path getFilePath(String fileName) {
        URL resourceURL = this.getClass().getClassLoader().getResource("conf");
        if (resourceURL == null) {
            throw new RuntimeException("Resource path not found");
        }
        return Paths.get(resourceURL.getPath(), fileName);
    }
}
