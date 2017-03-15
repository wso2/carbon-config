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
import org.wso2.carbon.utils.Constants;

/**
 * This class tests the functionality of ConfigurationUtils class
 *
 * @since 1.0.0
 */
public class UtilsTest {

    @Test
    public void testSubstituteVarsSystemPropertyNotNull() {
        String carbonHome = System.getProperty(Constants.CARBON_HOME);
        boolean isCarbonHomeChanged = false;

        if (carbonHome == null) {
            carbonHome = "test-carbon-home";
            System.setProperty(Constants.CARBON_HOME, carbonHome);
            isCarbonHomeChanged = true;
        }

        Assert.assertEquals(ConfigurationUtils.substituteVariables("${carbon.home}"), carbonHome);

        if (isCarbonHomeChanged) {
            System.clearProperty(Constants.CARBON_HOME);
        }
    }

    @Test
    public void testValueSubstituteVariables() {
        String carbonHome = System.getProperty(Constants.CARBON_HOME);
        boolean isCarbonHomeChanged = false;

        if (carbonHome == null) {
            carbonHome = "test-carbon-home";
            System.setProperty(Constants.CARBON_HOME, carbonHome);
            isCarbonHomeChanged = true;
        }

        Assert.assertEquals(ConfigurationUtils.substituteVariables("ValueNotExist"), "ValueNotExist");
        if (isCarbonHomeChanged) {
            System.clearProperty(Constants.CARBON_HOME);
        }
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testSubstituteVarsSystemPropertyIsNull() {
        String carbonHome = System.getProperty(Constants.CARBON_HOME);
        boolean isCarbonHomeChanged = false;

        if (carbonHome != null) {
            System.clearProperty(Constants.CARBON_HOME);
            isCarbonHomeChanged = true;
        }

        try {
            ConfigurationUtils.substituteVariables("${carbon.home}");
        } finally {
            if (isCarbonHomeChanged) {
                System.setProperty(Constants.CARBON_HOME, carbonHome);
            }
        }
    }

    @Test
    public void testSetGetSystemVariableValue() {
        // Set system variables
        EnvironmentUtils.setEnv("testEnvironmentVariable", "EnvironmentVariable");
        EnvironmentUtils.setEnv("server.key", "carbon-kernel");
        // Get system variables
        Assert.assertEquals(ConfigurationUtils.getSystemVariableValue("testEnvironmentVariable",
                null), "EnvironmentVariable");
        Assert.assertEquals(ConfigurationUtils.getSystemVariableValue("${server.key.not.exist}",
                null, ConfigConstants.PlaceHolders.class),
                null);
        Assert.assertEquals(ConfigurationUtils.getSystemVariableValue("server.key",
                null, ConfigConstants.PlaceHolders.class),
                "carbon-kernel");
    }

    @DataProvider(name = "paths")
    public Object[][] createPaths() {
        return new Object[][]{{"/home/wso2/wso2carbon", "/"},
                {"C:\\Users\\WSO2\\Desktop\\CARBON~1\\WSO2CA~1.0-S", "\\"}};
    }

    @Test(dataProvider = "paths")
    public void testPathSubstitution(String carbonHome, String pathSeparator) {
        System.setProperty(Constants.CARBON_HOME, carbonHome);
        String config = "${" + Constants.CARBON_HOME + "}" + pathSeparator + "deployment" + pathSeparator;
        Assert.assertEquals(ConfigurationUtils.substituteVariables(config),
                carbonHome + pathSeparator + "deployment" + pathSeparator);
    }
}
