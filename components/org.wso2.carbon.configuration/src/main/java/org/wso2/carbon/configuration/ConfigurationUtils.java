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
package org.wso2.carbon.configuration;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

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
 * @since 5.2.0
 */
public class ConfigurationUtils {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationUtils.class);
    private static final Pattern varPattern = Pattern.compile("\\$\\{([^}]*)}");

    private ConfigurationUtils() {
    }

    /**
     * This method converts a given XML String to YAML format.
     *
     * @param xmlString XML String that needs to be converted to YAML format
     * @return String in YAML format
     */
    public static String convertXMLToYAML(String xmlString) {
        String jsonString;
        try {
            JSONObject xmlJSONObj = XML.toJSONObject(xmlString);
            jsonString = xmlJSONObj.toString();
            Yaml yaml = new Yaml();
            Map map = yaml.loadAs(jsonString, Map.class);
            return yaml.dumpAsMap(map);
        } catch (JSONException e) {
            throw new RuntimeException("Exception occurred while converting XML to JSON: ", e);
        }
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
                .forEach(entry -> deploymentConfigs.put(entry.getKey(), yaml.dumpAsMap(entry.getValue())));
        return deploymentConfigs;
    }

    /**
     * This method reads project properties in resource file,
     *
     * @return project properties
     */
    public static Properties loadProjectProperties() {
        Properties properties = new Properties();
        try (InputStream in = ConfigurationUtils.class.getClassLoader().getResourceAsStream(Constants
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
        return getSystemVariableValue(variableName, defaultValue, Constants.PlaceHolders.class);
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
}
