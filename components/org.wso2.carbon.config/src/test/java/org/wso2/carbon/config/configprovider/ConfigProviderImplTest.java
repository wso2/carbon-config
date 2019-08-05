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
import java.nio.file.Path;
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
    private static final String CONFIG_LEVEL_SEPARATOR = "_";
    private static final String NAMESPACE_LEVEL_SEPERATOR = "__";
    private static final String UNIQUE_ATTRIBUTE_SPECIFIER = "UNIQUE";
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
        Path resourcePath = TestUtils.getResourcePath("conf", "Example.yaml").get();
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
            ConfigFileReader fileReader = new YAMLBasedConfigFileReader(TestUtils.getResourcePath("conf", "Example" +
                    ".yaml").get());
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

    @Test(description = "This test will test functionality of creating configuration object with different namespace")
    public void configObjectWithDifferentNamespaceTestCase() throws ConfigurationException {
        ConfigFileReader fileReader = new YAMLBasedConfigFileReader(TestUtils.getResourcePath("conf", "Example" +
                ".yaml").get());
        ConfigProvider configProvider = new ConfigProviderImpl(fileReader, secureVault);
        TestConfiguration configurations = configProvider.getConfigurationObject("testconfiguration.v2",
                TestConfiguration.class);

        //Transport 1
        Assert.assertEquals(configurations.getTenant(), "tenant");
        Assert.assertEquals(configurations.getTransports().getTransport().get(0).getName(), "abc");
        Assert.assertEquals(configurations.getTransports().getTransport().get(0).getPort(), 9090);
        Assert.assertEquals(configurations.getTransports().getTransport().get(0).isSecure(), "true");
        Assert.assertEquals(configurations.getTransports().getTransport().get(0).getDesc(),
                "This transport will use 8000 as its port");
        Assert.assertEquals(configurations.getTransports().getTransport().get(0).getPassword(), PASSWORD);

        //Transport 2
        Assert.assertEquals(configurations.getTransports().getTransport().get(1).getName(), "pqrs");
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
    }

    @Test(description = "This test will test functionality of creating configuration object with invalid namespace")
    public void configObjectWithInvaildNamespaceTestCase() throws ConfigurationException {
        ConfigFileReader fileReader = new YAMLBasedConfigFileReader(TestUtils.getResourcePath("conf", "Example" +
                ".yaml").get());
        ConfigProvider configProvider = new ConfigProviderImpl(fileReader, secureVault);
        TestConfiguration configurations = configProvider.getConfigurationObject("testconfiguration.v3",
                TestConfiguration.class);

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
    }

    @Test(description = "This test will test functionality of creating configuration object with null namespace")
    public void configObjectWithNullNamespaceTestCase() throws ConfigurationException {
        ConfigFileReader fileReader = new YAMLBasedConfigFileReader(TestUtils.getResourcePath("conf", "Example" +
                ".yaml").get());
        ConfigProvider configProvider = new ConfigProviderImpl(fileReader, secureVault);
        TestConfiguration configurations = configProvider.getConfigurationObject(null,
                TestConfiguration.class);

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
    }

    @Test(description = "This test will test functionality of creating configuration object without namespace")
    public void configObjectWithoutNamespaceTestCase() throws ConfigurationException {

        ConfigFileReader fileReader = new YAMLBasedConfigFileReader(TestUtils.getResourcePath("conf", "Example" +
                ".yaml").get());
        ConfigProvider configProvider = new ConfigProviderImpl(fileReader, secureVault);
        BaseConfiguration configurations = configProvider.getConfigurationObject("nonamespace.configuration",
                BaseConfiguration.class);

        Assert.assertEquals(configurations.getName(), "transport");
        Assert.assertEquals(configurations.getTestBean().getId(), 30);

        configurations = configProvider.getConfigurationObject(null,
                BaseConfiguration.class);

        Assert.assertEquals(configurations.getName(), "test");
        Assert.assertEquals(configurations.getTestBean().getId(), 20);
    }

    @Test(description = "This test will test functionality when using yaml config file and configuration map")
    public void yamlFileConfigMapTestCase() throws IOException {
        Path resourcePath = TestUtils.getResourcePath("conf", "Example.yaml").get();
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
            ConfigFileReader fileReader = new YAMLBasedConfigFileReader(TestUtils.getResourcePath("conf", "Example" +
                    ".yaml").get());
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
        ConfigFileReader fileReader = new YAMLBasedConfigFileReader(TestUtils.getResourcePath("conf",
                "sampledatasource.yaml").get());
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
        ConfigFileReader fileReader = new YAMLBasedConfigFileReader(TestUtils.getResourcePath("conf",
                "Example1.yaml").get());
        ConfigProvider configProvider = new ConfigProviderImpl(fileReader, secureVault);
        TestConfiguration configurations = configProvider.getConfigurationObject(TestConfiguration.class);
        Assert.assertNull(configurations, "configurations object should be null");
    }

    @Test(description = "This test will test functionality when configurations are not found in yaml file and " +
            "configuration map")
    public void invalidYAMLConfigMapTestCase() throws ConfigurationException {
        ConfigFileReader fileReader = new YAMLBasedConfigFileReader(TestUtils.getResourcePath("conf",
                "invalidconfiguration.yaml").get());
        ConfigProvider configProvider = new ConfigProviderImpl(fileReader, secureVault);
        Map configurationMap = (Map) configProvider.getConfigurationObject("configurations");
        Assert.assertNull(configurationMap, "configurations map should be null, " +
                "since no configuration found in yaml");
    }

    @Test(description = "This test will test functionality when configurations are not found in yaml file and " +
            "configuration object")
    public void invalidYAMLConfigObjectTestCase() throws ConfigurationException {
        ConfigFileReader fileReader = new YAMLBasedConfigFileReader(TestUtils.getResourcePath("conf",
                "invalidconfiguration.yaml").get());
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
                new YAMLBasedConfigFileReader(TestUtils.getResourcePath("conf", "systemconfigwithoutdefaults.yaml")
                        .get());
        ConfigProvider configProvider = new ConfigProviderImpl(fileReader, secureVault);
        TestConfiguration configurations = configProvider.getConfigurationObject(TestConfiguration.class);
        Assert.assertNull(configurations, "configurations object should be null");
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Environment variable.*",
            description = "This test will test functionality when configurations are not found in yaml file and " +
                    "configuration object")
    public void yamlConfigWithoutEnvValueTestCase() throws ConfigurationException {
        ConfigFileReader fileReader =
                new YAMLBasedConfigFileReader(TestUtils.getResourcePath("conf", "envconfigwithoutdefaults.yaml")
                        .get());
        ConfigProvider configProvider = new ConfigProviderImpl(fileReader, secureVault);
        TestConfiguration configurations = configProvider.getConfigurationObject(TestConfiguration.class);
        Assert.assertNull(configurations, "configurations object should be null");
    }

    @Test(description = "Tests the functionality when deployment configuration is overridden with environment " +
                        "variables")
    public void yamlConfigOverrideWithEnvVariable() throws ConfigurationException {
        String newTenantName = "NewTenant";
        String envVariable = CONFIG_NAMESPACE.toUpperCase() + NAMESPACE_LEVEL_SEPERATOR + "TENANT";
        EnvironmentUtils.setEnvironmentVariables(envVariable, newTenantName);
        ConfigFileReader fileReader = new YAMLBasedConfigFileReader(TestUtils.getResourcePath("conf",
                "envconfigoverride.yaml").get());
        ConfigProvider configProvider = new ConfigProviderImpl(fileReader, secureVault);
        TestConfiguration configurations = configProvider.getConfigurationObject(TestConfiguration.class);
        TestConfiguration testConfig = configProvider.getConfigurationObject(CONFIG_NAMESPACE, TestConfiguration.class);
        Assert.assertEquals(configurations.getTenant(), newTenantName);
        Assert.assertEquals(testConfig.getTenant(), newTenantName);
        EnvironmentUtils.unsetEnvironmentVariables(envVariable);
    }

    @Test(description = "Tests the functionality when deployment configuration is overridden with system properties")
    public void yamlConfigOverrideWithSystemProperty() throws ConfigurationException {
        String newTenantName = "NewTenant";
        String systemProperty = CONFIG_NAMESPACE.toUpperCase() + NAMESPACE_LEVEL_SEPERATOR + "TENANT";
        System.setProperty(systemProperty, newTenantName);
        ConfigFileReader fileReader = new YAMLBasedConfigFileReader(TestUtils.getResourcePath("conf",
                "envconfigoverride.yaml").get());
        ConfigProvider configProvider = new ConfigProviderImpl(fileReader, secureVault);
        TestConfiguration configurations = configProvider.getConfigurationObject(TestConfiguration.class);
        Assert.assertEquals(configurations.getTenant(), newTenantName);
        System.clearProperty(systemProperty);
    }

    @Test(description = "Tests the priority of system and environment variables when config is provided via system " +
                        "variables")
    public void yamlConfigOverrideSystemVarPriorityTest() throws ConfigurationException {
        String systemPropertyTenantName = "SystemPropertyTenant";
        String environmentVariableTenantName = "EnvironmentVariableTenant";
        String systemVariable = CONFIG_NAMESPACE.toUpperCase() + NAMESPACE_LEVEL_SEPERATOR + "TENANT";

        EnvironmentUtils.setEnvironmentVariables(systemVariable, environmentVariableTenantName);
        System.setProperty(systemVariable, systemPropertyTenantName);

        ConfigFileReader fileReader = new YAMLBasedConfigFileReader(TestUtils.getResourcePath("conf",
                "envconfigoverride.yaml").get());
        ConfigProvider configProvider = new ConfigProviderImpl(fileReader, secureVault);
        TestConfiguration configurations = configProvider.getConfigurationObject(TestConfiguration.class);
        Assert.assertEquals(configurations.getTenant(), environmentVariableTenantName);

        EnvironmentUtils.unsetEnvironmentVariables(systemVariable);
        System.clearProperty(systemVariable);
    }

    @Test(description = "Tests the functionality when deployment configuration is overridden with the environment " +
                        "variables - complex values")
    public void yamlConfigOverrideWithEnvVariablesComplex() throws ConfigurationException {
        String envVariable1Name = "TestBeanName";
        String envVariable2Name = "ComplexBeanName";
        String envVariable3Name = "ComplexBeanTestBeanName";
        String envVariable1 = "BASICTESTCONFIGURATION" + NAMESPACE_LEVEL_SEPERATOR + "TESTBEAN_NAME";
        String envVariable2 = "BASICTESTCONFIGURATION" + NAMESPACE_LEVEL_SEPERATOR + "COMPLEXTESTBEAN_NAME";
        String envVariable3 = "BASICTESTCONFIGURATION" + NAMESPACE_LEVEL_SEPERATOR + "COMPLEXTESTBEAN_TESTBEAN_NAME";
        EnvironmentUtils.setEnvironmentVariables(envVariable1, envVariable1Name);
        EnvironmentUtils.setEnvironmentVariables(envVariable2, envVariable2Name);
        EnvironmentUtils.setEnvironmentVariables(envVariable3, envVariable3Name);
        ConfigFileReader fileReader = new YAMLBasedConfigFileReader(TestUtils.getResourcePath("conf",
                "envconfigoverridecomplex.yaml").get());
        ConfigProvider configProvider = new ConfigProviderImpl(fileReader, secureVault);
        BasicTestConfiguration configurations = configProvider.getConfigurationObject(BasicTestConfiguration.class);
        Assert.assertEquals(configurations.getTestBean().getName(), envVariable1Name);
        Assert.assertEquals(configurations.getComplexTestBean().getName(), envVariable2Name);
        Assert.assertEquals(configurations.getComplexTestBean().getTestBean().getName(), envVariable3Name);
        EnvironmentUtils.unsetEnvironmentVariables(envVariable1);
        EnvironmentUtils.unsetEnvironmentVariables(envVariable2);
        EnvironmentUtils.unsetEnvironmentVariables(envVariable3);
    }

    @Test(description = "Tests the functionality when deployment configuration is overridden with the environment " +
                        "variables - array type")
    public void yamlConfigOverrideWithEnvVariablesArray() throws ConfigurationException {
        // Transport 1
        String transport1Name = "abc";
        int transport1Port = 8005;
        String transport1Secure = "true";
        String transport1Desc = "Transport 1 description";
        String transport1Password = "password";

        String transport1NameEnv = CONFIG_NAMESPACE.toUpperCase() + NAMESPACE_LEVEL_SEPERATOR +
                "TRANSPORTS_TRANSPORT_0_NAME";
        String transport1PortEnv = CONFIG_NAMESPACE.toUpperCase() + NAMESPACE_LEVEL_SEPERATOR +
                "TRANSPORTS_TRANSPORT_0_PORT";
        String transport1SecureEnv = CONFIG_NAMESPACE.toUpperCase() + NAMESPACE_LEVEL_SEPERATOR +
                "TRANSPORTS_TRANSPORT_0_SECURE";
        String transport1DescEnv = CONFIG_NAMESPACE.toUpperCase() + NAMESPACE_LEVEL_SEPERATOR +
                "TRANSPORTS_TRANSPORT_0_DESC";
        String transport1PasswordEnv = CONFIG_NAMESPACE.toUpperCase() + NAMESPACE_LEVEL_SEPERATOR +
                "TRANSPORTS_TRANSPORT_0_PASSWORD";
        EnvironmentUtils.setEnvironmentVariables(transport1NameEnv, transport1Name);
        EnvironmentUtils.setEnvironmentVariables(transport1PortEnv, String.valueOf(transport1Port));
        EnvironmentUtils.setEnvironmentVariables(transport1SecureEnv, transport1Secure);
        EnvironmentUtils.setEnvironmentVariables(transport1DescEnv, transport1Desc);
        EnvironmentUtils.setEnvironmentVariables(transport1PasswordEnv, transport1Password);

        // Transport 2
        String transport2Name = "pqr";
        String transport2password = "transport2password";

        String transport2NameEnv = CONFIG_NAMESPACE.toUpperCase() + NAMESPACE_LEVEL_SEPERATOR +
                "TRANSPORTS_TRANSPORT_8_NAME";
        String transport2PasswordEnv = CONFIG_NAMESPACE.toUpperCase() + NAMESPACE_LEVEL_SEPERATOR +
                "TRANSPORTS_TRANSPORT_8_PASSWORD";
        EnvironmentUtils.setEnvironmentVariables(transport2NameEnv, transport2Name);
        EnvironmentUtils.setEnvironmentVariables(transport2PasswordEnv, transport2password);

        // Transport 3 values not changed
        String transport3password = "zzz";

        ConfigFileReader fileReader = new YAMLBasedConfigFileReader(TestUtils.getResourcePath("conf",
                "envconfigoverride.yaml").get());
        ConfigProvider configProvider = new ConfigProviderImpl(fileReader, secureVault);
        TestConfiguration configurations = configProvider.getConfigurationObject(TestConfiguration.class);

        // Transport 1 assertions
        Transport transport1 = configurations.getTransports().getTransport().stream()
                .filter(transport -> transport.getName().equals(transport1Name))
                .findFirst().get();
        Assert.assertEquals(transport1.getPort(), transport1Port);
        Assert.assertEquals(transport1.isSecure(), transport1Secure);
        Assert.assertEquals(transport1.getDesc(), transport1Desc);
        Assert.assertEquals(transport1.getPassword(), transport1Password);

        // Transport 2 assertions
        Transport transport2 = configurations.getTransports().getTransport().stream()
                .filter(transport -> transport.getName().equals(transport2Name))
                .findFirst().get();
        Assert.assertEquals(transport2.getPassword(), transport2password);

        // Transport 3 assertions
        Transport transport3 = configurations.getTransports().getTransport().stream()
                .filter(transport -> transport.getName().equals("xyz"))
                .findFirst().get();
        Assert.assertEquals(transport3.getPassword(), transport3password);

        // Unset environment variables
        EnvironmentUtils.unsetEnvironmentVariables(transport1NameEnv);
        EnvironmentUtils.unsetEnvironmentVariables(transport1PortEnv);
        EnvironmentUtils.unsetEnvironmentVariables(transport1SecureEnv);
        EnvironmentUtils.unsetEnvironmentVariables(transport1DescEnv);
        EnvironmentUtils.unsetEnvironmentVariables(transport1PasswordEnv);

        EnvironmentUtils.unsetEnvironmentVariables(transport2NameEnv);
        EnvironmentUtils.unsetEnvironmentVariables(transport2PasswordEnv);
    }

    @Test(description = "Tests invalid environment variables - config provider should ignore invalid environment " +
                        "variables")
    public void invalidEnvVariableFormatsTest() throws ConfigurationException {
        String invalidEnvVar1 = CONFIG_LEVEL_SEPARATOR;
        String invalidEnvVar2 = CONFIG_NAMESPACE;
        String invalidEnvVar3 = CONFIG_LEVEL_SEPARATOR + CONFIG_NAMESPACE;
        String invalidEnvVar4 = CONFIG_LEVEL_SEPARATOR + CONFIG_NAMESPACE.toUpperCase() + CONFIG_LEVEL_SEPARATOR;
        String invalidEnvVar5 = CONFIG_LEVEL_SEPARATOR + CONFIG_LEVEL_SEPARATOR + CONFIG_LEVEL_SEPARATOR;
        String invalidEnvVar6 = CONFIG_LEVEL_SEPARATOR + CONFIG_LEVEL_SEPARATOR + "config";

        EnvironmentUtils.setEnvironmentVariables(invalidEnvVar1, "");
        EnvironmentUtils.setEnvironmentVariables(invalidEnvVar2, "");
        EnvironmentUtils.setEnvironmentVariables(invalidEnvVar3, "");
        EnvironmentUtils.setEnvironmentVariables(invalidEnvVar4, "");
        EnvironmentUtils.setEnvironmentVariables(invalidEnvVar5, "");
        EnvironmentUtils.setEnvironmentVariables(invalidEnvVar6, "");

        ConfigFileReader fileReader = new YAMLBasedConfigFileReader(TestUtils.getResourcePath("conf",
                "envconfigoverride.yaml").get());
        ConfigProvider configProvider = new ConfigProviderImpl(fileReader, secureVault);
        configProvider.getConfigurationObject(TestConfiguration.class);

        EnvironmentUtils.unsetEnvironmentVariables(invalidEnvVar1);
        EnvironmentUtils.unsetEnvironmentVariables(invalidEnvVar2);
        EnvironmentUtils.unsetEnvironmentVariables(invalidEnvVar3);
        EnvironmentUtils.unsetEnvironmentVariables(invalidEnvVar4);
        EnvironmentUtils.unsetEnvironmentVariables(invalidEnvVar5);
        EnvironmentUtils.unsetEnvironmentVariables(invalidEnvVar6);
    }

    @Test(description = "Tests invalid fields in environment variable")
    public void envInvalidFieldTest() throws ConfigurationException {
        boolean isExceptionOccurred = false;
        String transportNameEnv = CONFIG_NAMESPACE.toUpperCase() + NAMESPACE_LEVEL_SEPERATOR +
                "TRANSPORTS_TRANSPORT_0_NAME";
        String transportInvalidAttributeEnv = CONFIG_NAMESPACE.toUpperCase() + NAMESPACE_LEVEL_SEPERATOR +
                "TRANSPORTS_TRANSPORT_0_INVALIDATTRIBUTE";

        EnvironmentUtils.setEnvironmentVariables(transportNameEnv, "abc");
        EnvironmentUtils.setEnvironmentVariables(transportInvalidAttributeEnv, "INVALID");

        ConfigFileReader fileReader = new YAMLBasedConfigFileReader(TestUtils.getResourcePath("conf",
                "envconfigoverride.yaml").get());
        ConfigProvider configProvider = new ConfigProviderImpl(fileReader, secureVault);

        try {
            configProvider.getConfigurationObject(TestConfiguration.class);
        } catch (ConfigurationException e) {
            isExceptionOccurred = true;
            Assert.assertEquals(e.getMessage(),
                    "Field INVALIDATTRIBUTE not found in class " + Transport.class.getName());
        } finally {
            EnvironmentUtils.unsetEnvironmentVariables(transportNameEnv);
            EnvironmentUtils.unsetEnvironmentVariables(transportInvalidAttributeEnv);
        }
        if (!isExceptionOccurred) {
            Assert.fail("Expected ConfigurationException exception not occurred.");
        }
    }

    @Test(description = "Tests expected exception when an invalid value is set to a field")
    public void envFieldValueCastExceptionTest() throws ConfigurationException {
        boolean isExceptionOccurred = false;
        String transportNameEnv = CONFIG_NAMESPACE.toUpperCase() + NAMESPACE_LEVEL_SEPERATOR +
                "TRANSPORTS_TRANSPORT_0_NAME";
        String transportInvalidPortEnv = CONFIG_NAMESPACE.toUpperCase() + NAMESPACE_LEVEL_SEPERATOR +
                "TRANSPORTS_TRANSPORT_0_PORT";

        EnvironmentUtils.setEnvironmentVariables(transportNameEnv, "abc");
        EnvironmentUtils.setEnvironmentVariables(transportInvalidPortEnv, "INVALID");

        ConfigFileReader fileReader = new YAMLBasedConfigFileReader(TestUtils.getResourcePath("conf",
                "envconfigoverride.yaml").get());
        ConfigProvider configProvider = new ConfigProviderImpl(fileReader, secureVault);

        try {
            configProvider.getConfigurationObject(TestConfiguration.class);
        } catch (NumberFormatException e) {
            isExceptionOccurred = true;
            Assert.assertEquals(e.getMessage(), "For input string: \"INVALID\"");
        } finally {
            EnvironmentUtils.unsetEnvironmentVariables(transportNameEnv);
            EnvironmentUtils.unsetEnvironmentVariables(transportInvalidPortEnv);
        }
        if (!isExceptionOccurred) {
            Assert.fail("Expected ConfigurationException exception not occurred.");
        }
    }

    @Test(description = "Tests setting array element value failure when the unique element is not located")
    public void envArrayNoUniqueElementTest() {
        boolean isExceptionOccurred = false;
        String transportPortEnv = CONFIG_NAMESPACE.toUpperCase() + NAMESPACE_LEVEL_SEPERATOR +
                "TRANSPORTS_TRANSPORT_0_PORT";

        EnvironmentUtils.setEnvironmentVariables(transportPortEnv, "INVALID");

        ConfigFileReader fileReader = new YAMLBasedConfigFileReader(TestUtils.getResourcePath("conf",
                "envconfigoverride.yaml").get());
        ConfigProvider configProvider = new ConfigProviderImpl(fileReader, secureVault);

        try {
            configProvider.getConfigurationObject(TestConfiguration.class);
        } catch (ConfigurationException e) {
            isExceptionOccurred = true;
            Assert.assertEquals(e.getMessage(), "Locating unique system variable key for system variable " +
                    transportPortEnv + " from default attributes [ID, NAME] failed. " + "Custom unique system " +
                    "variable key TESTCONFIGURATION__TRANSPORTS_" + "TRANSPORT_UNIQUE is not specified as well.");
        } finally {
            EnvironmentUtils.unsetEnvironmentVariables(transportPortEnv);
        }
        if (!isExceptionOccurred) {
            Assert.fail("Expected ConfigurationException exception not occurred.");
        }
    }

    @Test(description = "Tests setting array element value with unique element ID which is of highest priority")
    public void envArrayPriorityUniqueElementTest() throws ConfigurationException {
        // ID is the 1st priority. NAME is the 2nd priority
        String name = "abc";
        int id = 2;
        int port = 5005;
        String uniqueEnvName = "PRIORITYTESTCONFIGURATION__TESTBEANS_TESTBEANLIST_0_NAME";
        String uniqueEnvId = "PRIORITYTESTCONFIGURATION__TESTBEANS_TESTBEANLIST_0_ID";
        String portEnv = "PRIORITYTESTCONFIGURATION__TESTBEANS_TESTBEANLIST_0_PORT";

        EnvironmentUtils.setEnvironmentVariables(uniqueEnvName, name);
        EnvironmentUtils.setEnvironmentVariables(uniqueEnvId, String.valueOf(id));
        EnvironmentUtils.setEnvironmentVariables(portEnv, String.valueOf(port));

        ConfigFileReader fileReader = new YAMLBasedConfigFileReader(TestUtils.getResourcePath("conf",
                "envconfigoverridepriority.yaml").get());
        ConfigProvider configProvider = new ConfigProviderImpl(fileReader, secureVault);
        PriorityTestConfiguration configurations = configProvider
                .getConfigurationObject(PriorityTestConfiguration.class);

        PriorityTestBean priorityTestBean = configurations.getTestBeans().getTestBeanList().stream()
                .filter(t -> t.getId() == id)
                .findFirst().get();

        Assert.assertEquals(priorityTestBean.getName(), name); // Name will be overridden
        Assert.assertEquals(priorityTestBean.getPort(), port);

        EnvironmentUtils.unsetEnvironmentVariables(uniqueEnvName);
        EnvironmentUtils.unsetEnvironmentVariables(uniqueEnvId);
        EnvironmentUtils.unsetEnvironmentVariables(portEnv);
    }

    @Test(description = "Tests setting unique element manually")
    public void envSetCustomUniqueElementTest() throws ConfigurationException {
        String uniqueElement = "abc";
        int port = 5005;
        String uniqueEnvName =
                "UNIQUEELEMENTTESTCONFIGURATION__UNIQUEELEMENTTESTBEANS_UNIQUEELEMENTTESTBEANLIST_0_UNIQUEELEMENT";
        String portEnv =
                "UNIQUEELEMENTTESTCONFIGURATION__UNIQUEELEMENTTESTBEANS_UNIQUEELEMENTTESTBEANLIST_0_PORT";
        String customUniqueEnv = "UNIQUEELEMENTTESTCONFIGURATION__UNIQUEELEMENTTESTBEANS_UNIQUEELEMENTTESTBEANLIST_" +
                                 UNIQUE_ATTRIBUTE_SPECIFIER;

        EnvironmentUtils.setEnvironmentVariables(uniqueEnvName, uniqueElement);
        EnvironmentUtils.setEnvironmentVariables(portEnv, String.valueOf(port));
        EnvironmentUtils.setEnvironmentVariables(customUniqueEnv, "UNIQUEELEMENT");

        ConfigFileReader fileReader = new YAMLBasedConfigFileReader(TestUtils.getResourcePath("conf",
                "envconfigoverrideuniqueelement.yaml").get());
        ConfigProvider configProvider = new ConfigProviderImpl(fileReader, secureVault);
        UniqueElementTestConfiguration configurations =
                configProvider.getConfigurationObject(UniqueElementTestConfiguration.class);

        UniqueElementTestBean uniqueElementTestBean =
                configurations.getUniqueElementTestBeans().getUniqueElementTestBeanList().stream()
                        .filter(t -> t.getUniqueElement().equals(uniqueElement))
                        .findFirst().get();

        Assert.assertEquals(uniqueElementTestBean.getPort(), port);

        EnvironmentUtils.unsetEnvironmentVariables(uniqueEnvName);
        EnvironmentUtils.unsetEnvironmentVariables(portEnv);
        EnvironmentUtils.unsetEnvironmentVariables(customUniqueEnv);
    }


    @Test(description = "Sets the unique element via system property and environment variable and tests if the value " +
                        "specified in the environment variable is honored")
    public void envSetUniqueElementSystemVarPriorityTest() throws ConfigurationException {
        String uniqueElement = "abc";
        int port = 5005;
        String uniqueVarName =
                "UNIQUEELEMENTTESTCONFIGURATION__UNIQUEELEMENTTESTBEANS_UNIQUEELEMENTTESTBEANLIST_0_UNIQUEELEMENT";
        String portVar =
                "UNIQUEELEMENTTESTCONFIGURATION__UNIQUEELEMENTTESTBEANS_UNIQUEELEMENTTESTBEANLIST_0_PORT";
        String customUniqueVar = "UNIQUEELEMENTTESTCONFIGURATION__UNIQUEELEMENTTESTBEANS_UNIQUEELEMENTTESTBEANLIST_" +
                                 UNIQUE_ATTRIBUTE_SPECIFIER;

        EnvironmentUtils.setEnvironmentVariables(uniqueVarName, uniqueElement);
        EnvironmentUtils.setEnvironmentVariables(portVar, String.valueOf(port));

        // Environment variable tells uniqueElement is unique
        EnvironmentUtils.setEnvironmentVariables(customUniqueVar, "UNIQUEELEMENT");
        System.setProperty(customUniqueVar, "PORT"); // System property tells port is unique

        ConfigFileReader fileReader = new YAMLBasedConfigFileReader(TestUtils.getResourcePath("conf",
                "envconfigoverrideuniqueelement.yaml").get());
        ConfigProvider configProvider = new ConfigProviderImpl(fileReader, secureVault);
        UniqueElementTestConfiguration configurations =
                configProvider.getConfigurationObject(UniqueElementTestConfiguration.class);

        UniqueElementTestBean uniqueElementTestBean =
                configurations.getUniqueElementTestBeans().getUniqueElementTestBeanList().stream()
                        .filter(t -> t.getUniqueElement().equals(uniqueElement))
                        .findFirst().get();

        Assert.assertEquals(uniqueElementTestBean.getPort(), port);

        EnvironmentUtils.unsetEnvironmentVariables(uniqueVarName);
        EnvironmentUtils.unsetEnvironmentVariables(portVar);
        EnvironmentUtils.unsetEnvironmentVariables(customUniqueVar);
        System.clearProperty(customUniqueVar);
    }

    @Test(description = "Tests the functionality when deployment configuration is overridden with environment " +
            "variables with complex namespace")
    public void yamlConfigOverrideWithComplexNameSpaceEnvVariable() throws ConfigurationException {
        String newTenantName = "NewTenant";
        String envVariable = "WSO2_TEST_CONFIG" + NAMESPACE_LEVEL_SEPERATOR + "TENANT";
        String envVariable2 = "WSO2_TEST" + NAMESPACE_LEVEL_SEPERATOR + "TENANT";
        EnvironmentUtils.setEnvironmentVariables(envVariable, newTenantName);
        EnvironmentUtils.setEnvironmentVariables(envVariable2, "abc");
        ConfigFileReader fileReader = new YAMLBasedConfigFileReader(TestUtils.getResourcePath("conf",
                "complexnamespaceconfig.yaml").get());
        ConfigProvider configProvider = new ConfigProviderImpl(fileReader, secureVault);
        ComplexNameSpaceConfiguration configurations = configProvider.getConfigurationObject
                (ComplexNameSpaceConfiguration.class);
        Assert.assertEquals(configurations.getTenant(), newTenantName);
        EnvironmentUtils.unsetEnvironmentVariables(envVariable);
    }

    @Test(description = "This test will test functionality of namespace root level list objects")
    public void configObjectWithList() throws ConfigurationException {
        ConfigFileReader fileReader = new YAMLBasedConfigFileReader(TestUtils.getResourcePath("conf",
                "Example2.yaml").get());
        ConfigProvider configProvider = new ConfigProviderImpl(fileReader, secureVault);

        ArrayList<HashMap> testTransports =
                                        ((ArrayList<HashMap>) configProvider.getConfigurationObject("testTransports"));

        Assert.assertEquals(testTransports.size(), 3);

        HashMap<String, Object> testTransport1 = ((HashMap<String, Object>) testTransports.get(0).get("testTransport"));
        Assert.assertEquals(testTransport1.get("name"), "abc");
        Assert.assertEquals(testTransport1.get("port"), 9090);
        Assert.assertEquals(testTransport1.get("secure"), true);
        Assert.assertEquals(testTransport1.get("desc"), "This transport will use 8000 as its port");
        Assert.assertEquals(testTransport1.get("password"), PASSWORD);

        //Transport 2
        HashMap<String, Object> testTransport2 = ((HashMap<String, Object>) testTransports.get(1).get("testTransport"));
        Assert.assertEquals(testTransport2.get("name"), "pqrs");
        Assert.assertEquals(testTransport2.get("port"), 8501);
        Assert.assertEquals(testTransport2.get("secure"), true);
        Assert.assertEquals(testTransport2.get("desc"), "This transport will use 8501 as its port. Secure - true");

        //Transport 3
        HashMap<String, Object> testTransport3 = ((HashMap<String, Object>) testTransports.get(2).get("testTransport"));
        Assert.assertEquals(testTransport3.get("name"), "xyz");
        Assert.assertEquals(testTransport3.get("port"), 9000);
        Assert.assertEquals(testTransport3.get("secure"), true);
        Assert.assertEquals(testTransport3.get("desc"), "This transport will use 8888 as its port");
    }

    @Test(description = "This test will test functionality of getConfigurationObjectList()")
    public void configObjectList() throws ConfigurationException {
        ConfigFileReader fileReader = new YAMLBasedConfigFileReader(TestUtils.getResourcePath("conf",
                "Example2.yaml").get());
        ConfigProvider configProvider = new ConfigProviderImpl(fileReader, secureVault);

        List<TestTransportElement> testTransports =
                            configProvider.getConfigurationObjectList("testTransports", TestTransportElement.class);

        Assert.assertEquals(testTransports.size(), 3);

        Assert.assertEquals(testTransports.get(0).getTestTransport().getName(), "abc");
        Assert.assertEquals(testTransports.get(0).getTestTransport().getPort(), 9090);
        Assert.assertEquals(testTransports.get(0).getTestTransport().isSecure(), true);
        Assert.assertEquals(testTransports.get(0).getTestTransport().getDesc(),
                                                        "This transport will use 8000 as its port");
        Assert.assertEquals(testTransports.get(0).getTestTransport().getPassword(), PASSWORD);

        //Transport 2
        Assert.assertEquals(testTransports.get(1).getTestTransport().getName(), "pqrs");
        Assert.assertEquals(testTransports.get(1).getTestTransport().getPort(), 8501);
        Assert.assertEquals(testTransports.get(1).getTestTransport().isSecure(), true);
        Assert.assertEquals(testTransports.get(1).getTestTransport().getDesc(),
                                                        "This transport will use 8501 as its port. Secure - true");

        //Transport 3
        Assert.assertEquals(testTransports.get(2).getTestTransport().getName(), "xyz");
        Assert.assertEquals(testTransports.get(2).getTestTransport().getPort(), 9000);
        Assert.assertEquals(testTransports.get(2).getTestTransport().isSecure(), true);
        Assert.assertEquals(testTransports.get(2).getTestTransport().getDesc(),
                                                            "This transport will use 8888 as its port");
    }


    /**
     * Set environmental variables.
     */
    private void setUpEnvironment() {
        Map<String, String> envVarMap = new HashMap<>();
        envVarMap.put("pqr.http.port", "8501");
        envVarMap.put("sample.abc.port", "8081");
        EnvironmentUtils.setEnvironmentVariables(envVarMap);
        // This is how to set System properties
        System.setProperty("abc.http.port", "8001");
        System.setProperty("sample.xyz.port", "9091");
        System.setProperty("pqr.secure", "true");
    }
}
