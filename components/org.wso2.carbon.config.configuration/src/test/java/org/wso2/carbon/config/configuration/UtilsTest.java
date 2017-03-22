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
package org.wso2.carbon.config.configuration;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.config.configuration.utils.EnvironmentUtils;

/**
 * This class tests the functionality of ConfigurationUtils class
 *
 * @since 1.0.0
 */
public class UtilsTest {

    private static final String UTIL_ENV_TEST_VARIABLE = "util.test";

    @Test
    public void testSubstituteVarsSystemPropertyNotNull() {
        String testVariable = System.getProperty(UTIL_ENV_TEST_VARIABLE);
        boolean isTestVariableChanged = false;

        if (testVariable == null) {
            testVariable = "utils-test-variable";
            System.setProperty(UTIL_ENV_TEST_VARIABLE, testVariable);
            isTestVariableChanged = true;
        }

        Assert.assertEquals(ConfigurationUtils.substituteVariables("${util.test}"), testVariable);

        if (isTestVariableChanged) {
            System.clearProperty(UTIL_ENV_TEST_VARIABLE);
        }
    }

    @Test
    public void testValueSubstituteVariables() {
        String testVariable = System.getProperty(UTIL_ENV_TEST_VARIABLE);
        boolean isTestVariableChanged = false;

        if (testVariable == null) {
            testVariable = "utils-test-variable";
            System.setProperty(UTIL_ENV_TEST_VARIABLE, testVariable);
            isTestVariableChanged = true;
        }

        Assert.assertEquals(ConfigurationUtils.substituteVariables("ValueNotExist"), "ValueNotExist");
        if (isTestVariableChanged) {
            System.clearProperty(UTIL_ENV_TEST_VARIABLE);
        }
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testSubstituteVarsSystemPropertyIsNull() {
        String testVariable = System.getProperty(UTIL_ENV_TEST_VARIABLE);
        boolean isTestVariableChanged = false;

        if (testVariable != null) {
            System.clearProperty(UTIL_ENV_TEST_VARIABLE);
            isTestVariableChanged = true;
        }

        try {
            ConfigurationUtils.substituteVariables("${util.test}");
        } finally {
            if (isTestVariableChanged) {
                System.setProperty(UTIL_ENV_TEST_VARIABLE, testVariable);
            }
        }
    }

    @Test
    public void testSetGetSystemVariableValue() {
        // Set system variables
        EnvironmentUtils.setEnv("testEnvironmentVariable", "EnvironmentVariable");
        EnvironmentUtils.setEnv("server.key", "test-server");
        // Get system variables
        Assert.assertEquals(ConfigurationUtils.getSystemVariableValue("testEnvironmentVariable",
                null), "EnvironmentVariable");
        Assert.assertEquals(ConfigurationUtils.getSystemVariableValue("${server.key.not.exist}",
                null, ConfigConstants.PlaceHolders.class),
                null);
        Assert.assertEquals(ConfigurationUtils.getSystemVariableValue("server.key",
                null, ConfigConstants.PlaceHolders.class),
                "test-server");
    }

    @DataProvider(name = "paths")
    public Object[][] createPaths() {
        return new Object[][]{{"/home/wso2/wso2carbon", "/"},
                {"C:\\Users\\WSO2\\Desktop\\CARBON~1\\WSO2CA~1.0-S", "\\"}};
    }

    @Test(dataProvider = "paths")
    public void testPathSubstitution(String path, String pathSeparator) {
        System.setProperty(UTIL_ENV_TEST_VARIABLE, path);
        String config = "${" + UTIL_ENV_TEST_VARIABLE + "}" + pathSeparator + "deployment" + pathSeparator;
        Assert.assertEquals(ConfigurationUtils.substituteVariables(config),
                path + pathSeparator + "deployment" + pathSeparator);
    }
}
