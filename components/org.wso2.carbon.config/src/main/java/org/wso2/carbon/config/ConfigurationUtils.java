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
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Configuration internal utils.
 *
 * @since 1.0.0
 */
public class ConfigurationUtils {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationUtils.class);
    private static final Pattern varPattern = Pattern.compile("\\$\\{([^}]*)}");
    private static final char[] specialCharArray = new char[]{'\\', '+', '-', '!', '(', ')', ':', '^', '[', ']',
            '\"', '{', '}', '~', '*', '?', '|', '&', ';', '/', '$', '%'};

    private ConfigurationUtils() {
    }

    /**
     * This method converts the yaml string to configuration map.
     * Map contains, key : yaml (root)key
     * values  : yaml string of the key
     *
     * @param yamlString yaml string
     * @return configuration map
     */
    public static Map<String, String> getDeploymentConfigMap(String yamlString) {
        Map<String, String> deploymentConfigs = new HashMap<>();
        Yaml yaml = new Yaml();
        Map<String, Object> map = (Map<String, Object>) yaml.loadAs(yamlString, Map.class);

        map.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .forEach((entry) -> {
                    String yamlValue;
                    if (entry.getValue() instanceof Map) {
                        yamlValue = yaml.dumpAsMap(entry.getValue());
                    } else {
                        yamlValue = yaml.dumpAs(entry.getValue(), Tag.SEQ, DumperOptions.FlowStyle.BLOCK);
                    }
                    deploymentConfigs.put(entry.getKey(), yamlValue);
                });
        return deploymentConfigs;
    }

    /**
     * This method reads project properties in resource file.
     *
     * @return project properties
     */
    public static Properties loadProjectProperties() {
        Properties properties = new Properties();
        try (InputStream in = ConfigurationUtils.class.getClassLoader().getResourceAsStream(ConfigConstants
                .PROJECT_DEFAULTS_PROPERTY_FILE)) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException e) {
            logger.error("Error while reading the project default properties, hence apply default values.", e);
        }
        return properties;
    }

    /**
     * Replace system property holders in the property values.
     * e.g. Replace ${carbon.home} with value of the carbon.home system property.
     *
     * @param value string value to substitute
     * @return String substituted string
     */
    public static String substituteVariables(String value) {
        // Fix the issue #3, This method should not execute in doc generation phase. Need not substitute vaule.
        // Check the system property to identify the call is coming in doc generation phase.
        if (Boolean.getBoolean(ConfigConstants.SYSTEM_PROPERTY_DOC_GENERATION)) {
            return value;
        }

        Matcher matcher = varPattern.matcher(value);
        boolean found = matcher.find();
        if (!found) {
            return value;
        }
        StringBuffer sb = new StringBuffer();
        do {
            String sysPropKey = matcher.group(1);
            String sysPropValue = getSystemVariableValue(sysPropKey, null);
            if (sysPropValue == null || sysPropValue.length() == 0) {
                String msg = "System property " + sysPropKey + " is not specified";
                logger.error(msg);
                throw new RuntimeException(msg);
            }
            // Due to reported bug under CARBON-14746
            sysPropValue = sysPropValue.replace("\\", "\\\\");
            matcher.appendReplacement(sb, sysPropValue);
        } while (matcher.find());
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * A utility which allows reading variables from the environment or System properties.
     * If the variable in available in the environment as well as a System property, the System property takes
     * precedence.
     *
     * @param variableName System/environment variable name
     * @param defaultValue default value to be returned if the specified system variable is not specified.
     * @return value of the system/environment variable
     */
    public static String getSystemVariableValue(String variableName, String defaultValue) {
        return getSystemVariableValue(variableName, defaultValue, ConfigConstants.PlaceHolders.class);
    }

    /**
     * A utility which allows reading variables from the environment or System properties.
     * If the variable in available in the environment as well as a System property, the System property takes
     * precedence.
     *
     * @param variableName  System/environment variable name
     * @param defaultValue  default value to be returned if the specified system variable is not specified.
     * @param constantClass Class from which the Predefined value should be retrieved if system variable and default
     *                      value is not specified.
     * @return value of the system/environment variable
     */
    public static String getSystemVariableValue(String variableName, String defaultValue, Class constantClass) {
        String value = null;
        if (System.getProperty(variableName) != null) {
            value = System.getProperty(variableName);
        } else if (System.getenv(variableName) != null) {
            value = System.getenv(variableName);
        } else {
            try {
                String constant = variableName.replaceAll("\\.", "_").toUpperCase(Locale.getDefault());
                Field field = constantClass.getField(constant);
                value = (String) field.get(constant);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                //Nothing to do
            }
            if (value == null) {
                value = defaultValue;
            }
        }
        return value;
    }

    /**
     * Returns replace value with escaped characters.
     *
     * @param value replaced values
     * @return value with escaped characters
     */
    public static String escapeSpecialCharacters(String value) {
        if (value == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            boolean charExists = false;
            for (char s : specialCharArray) {
                if (c == s) {
                    charExists = true;
                }
            }
            if (charExists || Character.isWhitespace(c)) {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
