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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.config.ConfigProviderFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.secvault.SecureVault;
import org.wso2.carbon.secvault.exception.SecureVaultException;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class is to test uses of the ConfigProviderFactory.
 *
 * @since 1.0.0
 */
public class ConfigProviderFactoryTest {
    private static final String SYS_KEY_MASTER_KEY_FILE = "master.key.file";
    private static final String SYS_KEY_SEC_PROP_FILE = "sec.prop.file";
    private static final String SYS_KEY_KEYSTORE_FILE = "keystore.file";
    private static final String PASSWORD = "n3wP4s5w0r4";
    private static final String OS_NAME_KEY = "os.name";
    private static final String WINDOWS_PARAM = "indow";
    private SecureVault secureVault;
    private Path configPath;

    @BeforeTest
    public void setup() throws ConfigurationException, URISyntaxException {
        System.setProperty(SYS_KEY_MASTER_KEY_FILE, getFilePath("conf", "master-keys.yaml").toString());
        System.setProperty(SYS_KEY_SEC_PROP_FILE, getFilePath("conf", "secrets.properties").toString());
        System.setProperty(SYS_KEY_KEYSTORE_FILE, getFilePath("conf", "wso2carbon.jks").toString());

        secureVault = EasyMock.mock(SecureVault.class);
        try {
            EasyMock.expect(secureVault.resolve(EasyMock.anyString())).andReturn(PASSWORD.toCharArray()).anyTimes();
        } catch (SecureVaultException e) {
            throw new ConfigurationException("Error resolving secure vault", e);
        }
        EasyMock.replay(secureVault);
    }

    @AfterClass
    public void clean() {
        System.clearProperty(SYS_KEY_MASTER_KEY_FILE);
        System.clearProperty(SYS_KEY_SEC_PROP_FILE);
        System.clearProperty(SYS_KEY_KEYSTORE_FILE);
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
        ConfigProviderFactory.getConfigProvider(getFilePath("conf", "incorrectfilepath.yaml"), secureVault);
    }

    @Test(description = "test case when securevault is not provided, when getting config provider", expectedExceptions
            = ConfigurationException.class, expectedExceptionsMessageRegExp = "No securevault service found. " +
            "configuration provider will not be initialized!")
    public void secureVaultNotProvidedTestCase() throws ConfigurationException {
        ConfigProviderFactory.getConfigProvider(getFilePath("conf", "Example.yaml"), null);
    }

    @Test(description = "test case for xml configuration file")
    public void xmlConfigFileTestCase() throws ConfigurationException {
        ConfigProvider configProvider = ConfigProviderFactory.getConfigProvider(getFilePath("conf", "Example.xml"),
                secureVault);
        Assert.assertNotNull(configProvider, "Configuration provider cannot be null");
    }

    @Test(description = "test case for yaml configuration file")
    public void yamlConfigFileTestCase() throws ConfigurationException {
        ConfigProvider configProvider = ConfigProviderFactory.getConfigProvider(getFilePath("conf", "Example.yaml"),
                secureVault);
        Assert.assertNotNull(configProvider, "Configuration provider cannot be null");
    }

    @Test(description = "test case for .txt configuration file. ", expectedExceptions = ConfigurationException.class,
            expectedExceptionsMessageRegExp = "Error while initializing configuration provider, file extension is not" +
                    " supported")
    public void invalidConfigFileTestCase() throws ConfigurationException {
        ConfigProviderFactory.getConfigProvider(getFilePath("conf", "Example.txt"),
                secureVault);
    }

    @Test(description = "test case for config provider when securevault is not predefined. yaml configuration file " +
            "contains securevault configuration")
    public void secureVaultNotPredefinedTestCase() throws ConfigurationException {

        ConfigProvider configProvider = ConfigProviderFactory.getConfigProvider(getFilePath("conf", "deployment.yaml"));
        Assert.assertNotNull(configProvider, "Configuration provider cannot be null");
        TestConfiguration testConfiguration = configProvider.getConfigurationObject(TestConfiguration.class);
        Assert.assertEquals(testConfiguration.getTenant(), "tenant");
    }

    /**
     * Get file from resources.
     *
     * @param fileName name of the file
     * @return file path
     */
    private Path getFilePath(String... fileName) {
        URL resourceURL = this.getClass().getClassLoader().getResource("");
        if (resourceURL != null) {
            String resourcePath = resourceURL.getPath();
            if (resourcePath != null) {
                resourcePath = System.getProperty(OS_NAME_KEY).contains(WINDOWS_PARAM) ?
                        resourcePath.substring(1) : resourcePath;
                return Paths.get(resourcePath, fileName);
            }
        }
        return null;
    }
}
