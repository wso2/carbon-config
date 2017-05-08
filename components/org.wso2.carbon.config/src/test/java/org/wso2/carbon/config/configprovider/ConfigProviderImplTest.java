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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.config.provider.ConfigProviderImpl;
import org.wso2.carbon.config.reader.ConfigFileReader;
import org.wso2.carbon.config.reader.YAMLBasedConfigFileReader;
import org.wso2.carbon.config.utils.EnvironmentUtils;
import org.wso2.carbon.secvault.SecureVault;
import org.wso2.carbon.secvault.exception.SecureVaultException;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is to demonstrate the sample uses of the ConfigProvider.
 *
 * @since 1.0.0
 */
public class ConfigProviderImplTest {

    private static Logger logger = LoggerFactory.getLogger(ConfigProviderImplTest.class.getName());
    private static final String PASSWORD = "n3wP4s5w0r4";
    private static final String CONFIG_NAMESPACE = "testconfiguration";
    private static final String OS_NAME_KEY = "os.name";
    private static final String WINDOWS_PARAM = "indow";
    private SecureVault secureVault;

    @BeforeTest
    public void setup() throws ConfigurationException {
        setUpEnvironment();
        secureVault = EasyMock.mock(SecureVault.class);
        try {
            EasyMock.expect(secureVault.resolve(EasyMock.anyString())).andReturn(PASSWORD.toCharArray()).anyTimes();
        } catch (SecureVaultException e) {
            throw new ConfigurationException("Error resolving secure vault", e);
        }
        EasyMock.replay(secureVault);
    }

    @Test(description = "This test will test functionality when using yaml config file")
    public void yamlFileConfigObjectTestCase() throws IOException {
        Path resourcePath = getFilePath("conf", "Example.yaml");
        File file = resourcePath.toFile();
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            Yaml yaml = new Yaml();
            Map map = yaml.loadAs(fileInputStream, Map.class);
            Map configurationMap = (Map) map.get(CONFIG_NAMESPACE);
            Map transportsMap = (Map) configurationMap.get("transports");
            ArrayList transportList = (ArrayList) transportsMap.get("transport");
            LinkedHashMap transport1 = (LinkedHashMap) transportList.get(0);
            LinkedHashMap transport2 = (LinkedHashMap) transportList.get(1);
            LinkedHashMap transport3 = (LinkedHashMap) transportList.get(2);

            Assert.assertEquals(configurationMap.get("tenant"), "tenant");
            //Transport 1
            Assert.assertEquals(transport1.get("name"), "abc");
            Assert.assertEquals(transport1.get("port"), 8000);
            Assert.assertEquals(transport1.get("secure"), false);
            Assert.assertEquals(transport1.get("desc"), "This transport will use 8000 as its port");
            Assert.assertEquals(transport1.get("password"), "${sec:conn.auth.password}");
            //Transport 2
            Assert.assertEquals(transport2.get("name"), "pqr");
            Assert.assertEquals(transport2.get("port"), "${env:pqr.http.port}");
            Assert.assertEquals(transport2.get("secure"), "${sys:pqr.secure}");
            Assert.assertEquals(transport2.get("desc"),
                    "This transport will use ${env:pqr.http.port} as its port. Secure - ${sys:pqr.secure}");
            //Transport 3
            Assert.assertEquals(transport3.get("name"), "xyz");
            Assert.assertEquals(transport3.get("port"), "${env:xyz.http.port,9000}");
            Assert.assertEquals(transport3.get("secure"), "${sys:xyz.secure,true}");
            Assert.assertEquals(transport3.get("desc"),
                    "This transport will use ${env:xyz.http.port,8888} as its port");
        } catch (FileNotFoundException e) {
            logger.error(e.toString());
            Assert.fail();
        }

        try {
            ConfigFileReader fileReader = new YAMLBasedConfigFileReader(getFilePath("conf", "Example.yaml"));
            ConfigProvider configProvider = new ConfigProviderImpl(fileReader, secureVault);
            TestConfiguration configurations = configProvider.getConfigurationObject(TestConfiguration.class);

            //Transport 1
            Assert.assertEquals(configurations.getTenant(), "tenant");
            Assert.assertEquals(configurations.getTransports().getTransport().get(0).getName(), "abc");
            Assert.assertEquals(configurations.getTransports().getTransport().get(0).getPort(), 8000);
            Assert.assertEquals(configurations.getTransports().getTransport().get(0).isSecure(), "false");
            Assert.assertEquals(configurations.getTransports().getTransport().get(0).getDesc(),
                    "This transport will use 8000 as its port");
            Assert.assertEquals(configurations.getTransports().getTransport().get(0).getPassword(), PASSWORD);

            //Transport 2
            Assert.assertEquals(configurations.getTransports().getTransport().get(1).getName(), "pqr");
            Assert.assertEquals(configurations.getTransports().getTransport().get(1).getPort(), 8501);
            Assert.assertEquals(configurations.getTransports().getTransport().get(1).isSecure(), "true");
            Assert.assertEquals(configurations.getTransports().getTransport().get(1).getDesc(),
                    "This transport will use 8501 as its port. Secure - true");
            //Transport 3
            Assert.assertEquals(configurations.getTransports().getTransport().get(2).getName(), "xyz");
            Assert.assertEquals(configurations.getTransports().getTransport().get(2).getPort(), 9000);
            Assert.assertEquals(configurations.getTransports().getTransport().get(2).isSecure(), "true");
            Assert.assertEquals(configurations.getTransports().getTransport().get(2).getDesc(),
                    "This transport will use 8888 as its port");
        } catch (ConfigurationException e) {
            logger.error(e.toString());
            Assert.fail();
        }
    }

    @Test(description = "This test will test functionality when using yaml config file and configuration map")
    public void yamlFileConfigMapTestCase() throws IOException {
        Path resourcePath = getFilePath("conf", "Example.yaml");
        File file = resourcePath.toFile();
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            Yaml yaml = new Yaml();
            Map map = yaml.loadAs(fileInputStream, Map.class);
            Map configurationMap = (Map) map.get(CONFIG_NAMESPACE);
            Map transportsMap = (Map) configurationMap.get("transports");
            ArrayList transportList = (ArrayList) transportsMap.get("transport");
            LinkedHashMap transport1 = (LinkedHashMap) transportList.get(0);
            LinkedHashMap transport2 = (LinkedHashMap) transportList.get(1);
            LinkedHashMap transport3 = (LinkedHashMap) transportList.get(2);

            Assert.assertEquals(configurationMap.get("tenant"), "tenant");
            //Transport 1
            Assert.assertEquals(transport1.get("name"), "abc");
            Assert.assertEquals(transport1.get("port"), 8000);
            Assert.assertEquals(transport1.get("secure"), false);
            Assert.assertEquals(transport1.get("desc"), "This transport will use 8000 as its port");
            Assert.assertEquals(transport1.get("password"), "${sec:conn.auth.password}");
            //Transport 2
            Assert.assertEquals(transport2.get("name"), "pqr");
            Assert.assertEquals(transport2.get("port"), "${env:pqr.http.port}");
            Assert.assertEquals(transport2.get("secure"), "${sys:pqr.secure}");
            Assert.assertEquals(transport2.get("desc"),
                    "This transport will use ${env:pqr.http.port} as its port. Secure - ${sys:pqr.secure}");
            //Transport 3
            Assert.assertEquals(transport3.get("name"), "xyz");
            Assert.assertEquals(transport3.get("port"), "${env:xyz.http.port,9000}");
            Assert.assertEquals(transport3.get("secure"), "${sys:xyz.secure,true}");
            Assert.assertEquals(transport3.get("desc"),
                    "This transport will use ${env:xyz.http.port,8888} as its port");
        } catch (FileNotFoundException e) {
            logger.error(e.toString());
            Assert.fail();
        }

        try {
            ConfigFileReader fileReader = new YAMLBasedConfigFileReader(getFilePath("conf", "Example.yaml"));
            ConfigProvider configProvider = new ConfigProviderImpl(fileReader, secureVault);
            Map configurationMap = (Map) configProvider.getConfigurationObject(CONFIG_NAMESPACE);

            Map transportsMap = (Map) configurationMap.get("transports");
            List transportList = (List) transportsMap.get("transport");
            LinkedHashMap transport1 = (LinkedHashMap) transportList.get(0);
            LinkedHashMap transport2 = (LinkedHashMap) transportList.get(1);
            LinkedHashMap transport3 = (LinkedHashMap) transportList.get(2);

            Assert.assertEquals(configurationMap.get("tenant"), "tenant");
            //Transport 1
            Assert.assertEquals(transport1.get("name"), "abc");
            Assert.assertEquals(transport1.get("port"), 8000);
            Assert.assertEquals(transport1.get("secure"), false);
            Assert.assertEquals(transport1.get("desc"), "This transport will use 8000 as its port");
            Assert.assertEquals(transport1.get("password"), PASSWORD);
            //Transport 2
            Assert.assertEquals(transport2.get("name"), "pqr");
            Assert.assertEquals(transport2.get("port"), 8501);
            Assert.assertEquals(transport2.get("secure"), true);
            Assert.assertEquals(transport2.get("desc"),
                    "This transport will use 8501 as its port. Secure - true");
            //Transport 3
            Assert.assertEquals(transport3.get("name"), "xyz");
            Assert.assertEquals(transport3.get("port"), 9000);
            Assert.assertEquals(transport3.get("secure"), true);
            Assert.assertEquals(transport3.get("desc"),
                    "This transport will use 8888 as its port");
        } catch (ConfigurationException e) {
            logger.error(e.toString());
            Assert.fail();
        }
    }

    public void yamlConfigSequenceTestCase() throws ConfigurationException {
        ConfigFileReader fileReader = new YAMLBasedConfigFileReader(getFilePath("conf", "sampledatasource.yaml"));
        ConfigProvider configProvider = new ConfigProviderImpl(fileReader, secureVault);
        List<Map<String, String>> datasources = (List<Map<String, String>>) configProvider.getConfigurationObject
                ("wso2.datasources");
        Assert.assertNotNull(datasources, "datasource configuration should not be null");
        Assert.assertEquals(datasources.size(), 2);
        Assert.assertEquals(datasources.get(0).get("name"), "WSO2_CARBON_DB");
        Assert.assertEquals(datasources.get(1).get("name"), "WSO2_ANALYTICS_DB");
    }

    @Test(expectedExceptions = ConfigurationException.class, description = "This test will test functionality " +
            "when yaml config file not found")
    public void yamlFileNotFoundTestCase() throws ConfigurationException {
        ConfigFileReader fileReader = new YAMLBasedConfigFileReader(getFilePath("conf", "Example1.yaml"));
        ConfigProvider configProvider = new ConfigProviderImpl(fileReader, secureVault);
        TestConfiguration configurations = configProvider.getConfigurationObject(TestConfiguration.class);
        Assert.assertNull(configurations, "configurations object should be null");
    }

    @Test(description = "This test will test functionality when configurations are not found in yaml file and " +
            "configuration map")
    public void invalidYAMLConfigMapTestCase() throws ConfigurationException {
        ConfigFileReader fileReader = new YAMLBasedConfigFileReader(getFilePath("conf", "invalidconfiguration.yaml"));
        ConfigProvider configProvider = new ConfigProviderImpl(fileReader, secureVault);
        Map configurationMap = (Map) configProvider.getConfigurationObject("configurations");
        Assert.assertNull(configurationMap, "configurations map should be null, " +
                "since no configuration found in yaml");
    }

    @Test(description = "This test will test functionality when configurations are not found in yaml file and " +
            "configuration object")
    public void invalidYAMLConfigObjectTestCase() throws ConfigurationException {
        ConfigFileReader fileReader = new YAMLBasedConfigFileReader(getFilePath("conf", "invalidconfiguration.yaml"));
        ConfigProvider configProvider = new ConfigProviderImpl(fileReader, secureVault);
        TestConfiguration configurations = configProvider.getConfigurationObject(TestConfiguration.class);

        Assert.assertEquals(configurations.getTenant(), "default");
        Assert.assertEquals(configurations.getTransports().getTransport().get(0).getName(), "default transport");
        Assert.assertEquals(configurations.getTransports().getTransport().get(0).getPort(), 8000);
        Assert.assertEquals(configurations.getTransports().getTransport().get(0).isSecure(), "false");
        Assert.assertEquals(configurations.getTransports().getTransport().get(0).getDesc(),
                "Default Transport Configurations");
        Assert.assertEquals(configurations.getTransports().getTransport().get(0).getPassword(), "zzz");
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "System property.*",
            description = "This test will test functionality when configurations are not found in yaml file and " +
                    "configuration object")
    public void yamlConfigWithoutSystemValueTestCase() throws ConfigurationException {
        ConfigFileReader fileReader =
                new YAMLBasedConfigFileReader(getFilePath("conf", "systemconfigwithoutdefaults.yaml"));
        ConfigProvider configProvider = new ConfigProviderImpl(fileReader, secureVault);
        TestConfiguration configurations = configProvider.getConfigurationObject(TestConfiguration.class);
        Assert.assertNull(configurations, "configurations object should be null");
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Environment variable.*",
            description = "This test will test functionality when configurations are not found in yaml file and " +
                    "configuration object")
    public void yamlConfigWithoutEnvValueTestCase() throws ConfigurationException {
        ConfigFileReader fileReader =
                new YAMLBasedConfigFileReader(getFilePath("conf", "envconfigwithoutdefaults.yaml"));
        ConfigProvider configProvider = new ConfigProviderImpl(fileReader, secureVault);
        TestConfiguration configurations = configProvider.getConfigurationObject(TestConfiguration.class);
        Assert.assertNull(configurations, "configurations object should be null");
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

    /**
     * Set environmental variables.
     */
    private void setUpEnvironment() {
        Map<String, String> envVarMap = new HashMap<>();
        envVarMap.put("pqr.http.port", "8501");
        envVarMap.put("sample.abc.port", "8081");
        EnvironmentUtils.setEnv(envVarMap);
        // This is how to set System properties
        System.setProperty("abc.http.port", "8001");
        System.setProperty("sample.xyz.port", "9091");
        System.setProperty("pqr.secure", "true");
    }
}
