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
package org.wso2.carbon.config.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigConstants;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.ConfigurationRuntimeException;
import org.wso2.carbon.config.ConfigurationUtils;
import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.reader.ConfigFileReader;
import org.wso2.carbon.secvault.SecureVault;
import org.wso2.carbon.secvault.exception.SecureVaultException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.lang.model.SourceVersion;

/**
 * This impl class provide the ability to override configurations in various components using a single file which has
 * the name {@link ConfigProviderImpl}.
 *
 * @since 1.0.0
 */
public class ConfigProviderImpl implements ConfigProvider {
    private static final Logger logger = LoggerFactory.getLogger(ConfigProviderImpl.class.getName());
    private static final String CONFIG_LEVEL_SEPARATOR = "_";
    private static final int CONFIG_MIN_ELEMENT_COUNT = 2;
    private static final String[] UNIQUE_ATTRIBUTE_NAMES = {"ID", "NAME"};
    private static final String UNIQUE_ATTRIBUTE_SPECIFIER = "UNIQUE";

    private Map<String, String> deploymentConfigs = null;
    //This regex is used to identify placeholders
    private static final String PLACEHOLDER_REGEX;
    //This is used to match placeholders
    private static final Pattern PLACEHOLDER_PATTERN;

    private ConfigFileReader configFileReader;

    private SecureVault secureVault;

    static {
        PLACEHOLDER_REGEX = "(.*?)(\\$\\{(" + getPlaceholderString() + "):([^,]+?)((,)(.+?))?\\})(.*?)";
        PLACEHOLDER_PATTERN = Pattern.compile(PLACEHOLDER_REGEX);
    }

    /**
     * Enum to hold the supported placeholder types.
     */
    private enum Placeholder {
        SYS("sys"), ENV("env"), SEC("sec");
        private String value;

        Placeholder(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public ConfigProviderImpl(ConfigFileReader configFileReader, SecureVault secureVault) {
        this.configFileReader = configFileReader;
        this.secureVault = secureVault;
    }

    @Override
    public <T> T getConfigurationObject(Class<T> configClass) throws ConfigurationException {
        //get configuration namespace from the class annotation
        String namespace = null;
        if (configClass.isAnnotationPresent(Configuration.class)) {
            Configuration configuration = configClass.getAnnotation(Configuration.class);
            if (!ConfigConstants.NULL.equals(configuration.namespace())) {
                namespace = configuration.namespace();
            }
        }
        // lazy loading deployment.yaml configuration.
        loadDeploymentConfiguration(configFileReader);

        //  if (namespace != null && deploymentConfigs.containsKey(namespace)) {
        String yamlConfigString = deploymentConfigs.get(namespace);
        if (logger.isDebugEnabled()) {
            logger.debug("class name: " + configClass.getSimpleName() + " | new configurations: \n" + yamlConfigString);
        }

        T configObject;
        if (yamlConfigString != null && !yamlConfigString.isEmpty()) {
            String yamlProcessedString = processPlaceholder(yamlConfigString);
            yamlProcessedString = ConfigurationUtils.substituteVariables(yamlProcessedString);
            configObject = getConfigurationObject(configClass, configClass.getClassLoader(), yamlProcessedString);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Deployment configuration mapping doesn't exist: " +
                             "creating configuration instance with default values");
            }
            try {
                configObject = configClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new ConfigurationException("Error while creating configuration instance: "
                                                 + configClass.getSimpleName(), e);
            }
        }
        return overrideConfigWithSystemVars(namespace, configObject);
    }

    @Override
    public Object getConfigurationObject(String namespace) throws ConfigurationException {
        // lazy loading deployment.yaml configuration, if it is not exists
        loadDeploymentConfiguration(configFileReader);
        // check for json configuration from deployment configs of namespace.
        if (deploymentConfigs.containsKey(namespace)) {
            String configString = deploymentConfigs.get(namespace);
            String processedString = processPlaceholder(configString);
            processedString = ConfigurationUtils.substituteVariables(processedString);
            Yaml yaml = new Yaml();
            // Fix the issue #17. return object can be a List or Map
            return yaml.load(processedString);
        }
        logger.error("configuration doesn't exist for the namespace: {} in deployment yaml   . Hence " +
                "return null object", namespace);
        return null;
    }

    /**
     * Returns a map of variables which are prefixed with the given namespace. A hash map will always be returned
     * even if no variables are found which are prefixed with the given namespace.
     * <p>
     * A variable should at least consists of {@value CONFIG_MIN_ELEMENT_COUNT} elements, the configuration namespace
     * and the configuration element.
     *
     * @param namespace configuration namespace
     * @param variables Map of key and values
     * @return map of variable keys and values which are prefixed with the namespace
     */
    private Map<String, String> filterVariables(String namespace, Map<String, String> variables) {
        // Filter 1: Ignore case and filter by namespace
        // Filter 2: Check if the key format is correct
        // Filter 3: Check if the configuration is not empty
        // Filter 4: Ignore unique identification specifiers
        return variables.entrySet().stream()
                .filter(entry -> entry.getKey().toUpperCase()
                        .startsWith(namespace.toUpperCase() + CONFIG_LEVEL_SEPARATOR))
                .filter(entry -> (entry.getKey().split(CONFIG_LEVEL_SEPARATOR, CONFIG_MIN_ELEMENT_COUNT)
                                          .length == CONFIG_MIN_ELEMENT_COUNT))
                .filter(entry -> (!entry.getKey().split(CONFIG_LEVEL_SEPARATOR, CONFIG_MIN_ELEMENT_COUNT)[1]
                                          .trim().isEmpty()))
                .filter(entry -> (!entry.getKey().split(CONFIG_LEVEL_SEPARATOR, CONFIG_MIN_ELEMENT_COUNT)[1]
                        .toUpperCase().endsWith(UNIQUE_ATTRIBUTE_SPECIFIER.toUpperCase())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, HashMap::new));
    }

    /**
     * Returns a map of system variables which is a combination of filtered system properties and filtered environment
     * variables. The duplicate entries are overridden by the environment variables.
     *
     * @param namespace configuration namespace
     * @return map of system variable keys and values which are prefixed with the namespace
     */
    private Map<String, String> getSystemVariables(String namespace) {
        if (namespace == null || namespace.trim().length() == 0) {
            return new HashMap<>();
        }

        // Collect filtered environment variables and system properties
        Map<String, String> envVariables = filterVariables(namespace, System.getenv());
        Map<String, String> systemProperties = filterVariables(namespace, System.getProperties().entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().toString(),
                        entry -> entry.getValue().toString())));

        Map<String, String> mergedMap = new HashMap<>();
        // Include system properties
        mergedMap.putAll(systemProperties);
        // Add environment variables or override system properties from environment variables
        mergedMap.putAll(envVariables);
        return mergedMap;
    }

    /**
     * Override the deployment.yaml configuration default values with configurations provided via system
     * variables.
     *
     * @param namespace   configuration namespace
     * @param configClass configuration bean class
     * @param <T>         object type
     * @return configuration bean object of given type
     */
    private <T> T overrideConfigWithSystemVars(String namespace, T configClass) throws ConfigurationException {
        Map<String, String> systemVariables = getSystemVariables(namespace);

        for (Map.Entry<String, String> entry : systemVariables.entrySet()) {
            String systemVarKey = entry.getKey();
            String configKey = systemVarKey.split(CONFIG_LEVEL_SEPARATOR, CONFIG_MIN_ELEMENT_COUNT)[1];
            String value = entry.getValue();

            List<String> configKeyElements = new ArrayList<>(Arrays.asList(configKey.split(CONFIG_LEVEL_SEPARATOR)));
            overrideConfigWithSystemVariable(configClass, null, configKeyElements, value, systemVarKey);
        }
        return configClass;
    }

    /**
     * Returns the overridden configuration element with the relevant system variable value.
     *
     * @param configClass       configuration element class
     * @param field             class field for which the value should be set
     * @param configKeyElements array of configuration elements to process
     * @param value             configuration value
     * @param systemVarKey      system variable key
     * @param <T>               type of configuration element class
     * @return overridden configuration element
     * @throws ConfigurationException when an error occurred in overriding the config value with the system variable
     */
    @SuppressWarnings("unchecked")
    private <T> Object overrideConfigWithSystemVariable(T configClass, Field field, List<String> configKeyElements,
                                                     String value, String systemVarKey) throws ConfigurationException {
        String configElement = configKeyElements.get(0);
        // Primitive values (ex: String : String)
        if (configKeyElements.size() == 1) {
            setFieldValue(configClass, configElement, value);
            configKeyElements.remove(configElement);
            return configClass;
        }
        // Array type
        if (isPositiveInteger(configElement)) {
            if (configClass instanceof Collection) {
                // Update or add config object to list.
                String configFieldName = configKeyElements.get(1); // Will not be NULL
                configKeyElements.remove(configElement);

                // Get unique element name and it's value
                ImmutablePair<String, String> uniqueVarEntry = getUniqueSystemVarEntry(systemVarKey);
                String uniqueVarKey = uniqueVarEntry.getFirst();
                String uniqueVarValue = uniqueVarEntry.getSecond();

                Optional configObjectOptional = ((Collection) configClass).stream()
                        .filter(element -> {
                            try {
                                Field uniqueField = getClassField(element, uniqueVarKey);
                                Object castedUniqueEnvValue = castToWrapperType(uniqueField, uniqueVarValue);
                                return getFieldValue(element, uniqueField).equals(castedUniqueEnvValue);
                            } catch (ConfigurationException e) {
                                return false;
                            }
                        })
                        .findFirst();
                if (configObjectOptional.isPresent()) {
                    Object configObject = configObjectOptional.get();
                    Field configField = getClassField(configObject, configFieldName);
                    ((Collection) configClass).remove(configObject); // Remove all ready existing object from list
                    ((Collection) configClass)
                            .add(overrideConfigWithSystemVariable(configObject, configField, configKeyElements, value,
                                    systemVarKey));
                } else {
                    Class<?> parameterizeType = getCollectionType(field);
                    Object parameterizeTypeObj = createInstanceFromClass(parameterizeType);
                    Field configField = getClassField(parameterizeTypeObj, configFieldName);
                    ((Collection) configClass)
                            .add(overrideConfigWithSystemVariable(parameterizeTypeObj, configField, configKeyElements,
                                    value, systemVarKey));
                }
                return configClass;
            }
            throw new ConfigurationException(String
                    .format(Locale.ENGLISH, "Cannot determine the array type of the system variable %s, " +
                                            "element %s", systemVarKey, configElement));
        }

        // Complex value (Ex: <Bean Class> : <Attribute> : <Value>)
        Field configField = getClassField(configClass, configElement);
        Object configElementObject = getFieldValue(configClass, configElement);

        configKeyElements.remove(configElement);
        setFieldValue(configClass, configElement,
                overrideConfigWithSystemVariable(configElementObject, configField, configKeyElements, value,
                        systemVarKey));
        return configClass;
    }

    /**
     * Returns an instance from the given class type.
     *
     * @param type class to create an instance from
     * @return instance from the given class type
     * @throws ConfigurationException thrown when the default constructor is not found or when the default
     *                                constructor is not accessible or when an error occurs during instantiating the
     *                                object from class
     */
    private Object createInstanceFromClass(Class<?> type) throws ConfigurationException {
        Constructor<?>[] constructors = type.getDeclaredConstructors();

        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterCount() == 0) {
                constructor.setAccessible(true);
                try {
                    return constructor.newInstance();
                } catch (InstantiationException e) {
                    throw new ConfigurationException(String.format(Locale.ENGLISH,
                            "Error occurred in instantiating an instance from %s", type.getName()));
                } catch (IllegalAccessException e) {
                    throw new ConfigurationException(String.format(Locale.ENGLISH,
                            "Cannot access default constructor in %s", type.getName()));
                } catch (InvocationTargetException e) {
                    throw new ConfigurationException(String.format(Locale.ENGLISH,
                            "Error occurred when invoking default constructor in %s", type.getName()));
                }
            }
        }
        throw new ConfigurationException(String.format(Locale.ENGLISH,
                "Default constructor not found in %s", type.getName()));
    }

    /**
     * Returns the key and the value relevant for the unique key for a given system variable.
     * <p>
     * Ex: considering the below system variables,
     * <ul>
     * <li> WSO2.DATASOURCES_0_NAME = "WSO2_CARBON_DB"</li>
     * <li> WSO2.DATASOURCES_0_DESCRIPTION = "The datasource used for registry and user manager"</li>
     * </ul>
     * If the unique key is NAME and system variable is WSO2.DATASOURCES_0_DESCRIPTION,
     * the returned value would be, {@code new ImmutablePair("WSO2.DATASOURCES_0_NAME, "WSO2_CARBON_DB")}
     * <p>
     * The unique key may either be retrieved from "ID" (1st priority)
     * <p>
     * Ex:
     * <ul>
     * <li>WSO2.DATASOURCES_0_ID = "WSO2_CARBON_DB"</li>
     * </ul>
     * or from "NAME" (2nd priority)
     * <ul>
     * <li>WSO2.DATASOURCES_0_NAME = "WSO2_CARBON_DB"</li>
     * </ul>
     * <p>
     * by default. If "ID" or "NAME" is not present, then the method will
     * look for whether a custom unique key is specified with the suffix {@value UNIQUE_ATTRIBUTE_SPECIFIER}
     * <p>
     * Ex:
     * <ul>
     * <li>WSO2.DATASOURCES_UNIQUE = "PRIMARYKEY"
     * WSO2.DATASOURCES_0_PRIMARYKEY = "WSO2_CARBON_DB"</li>
     * </ul>
     *
     * @param systemVarKey system variable key
     * @return the key and the value relevant for the unique key for a given system variable
     * @throws ConfigurationException when any of the unique identifiers for the given system variable cannot be
     *                                located
     */
    private ImmutablePair<String, String> getUniqueSystemVarEntry(String systemVarKey)
            throws ConfigurationException {
        // Get unique value from "ID" or "NAME"
        for (String uniqueKey : UNIQUE_ATTRIBUTE_NAMES) {
            String uniqueVarKey = systemVarKey.substring(0, systemVarKey.lastIndexOf(CONFIG_LEVEL_SEPARATOR)) +
                                  CONFIG_LEVEL_SEPARATOR + uniqueKey;
            String uniqueVarValue = getSystemVariableValue(uniqueVarKey);
            if (uniqueVarValue != null) {
                return ImmutablePair.of(uniqueKey, uniqueVarValue);
            }
        }

        // Check if the unique system variable is specified since unique value from "ID" and "NAME" failed.
        String uniqueVarKeySegment = systemVarKey.substring(0, systemVarKey.lastIndexOf(CONFIG_LEVEL_SEPARATOR));
        String arrayGroupString = uniqueVarKeySegment.substring(systemVarKey.lastIndexOf(CONFIG_LEVEL_SEPARATOR) - 1);
        uniqueVarKeySegment = uniqueVarKeySegment.substring(0, uniqueVarKeySegment.lastIndexOf(CONFIG_LEVEL_SEPARATOR));

        // Identify the unique system variable attribute
        String uniqueVarKeySpecifier = uniqueVarKeySegment + CONFIG_LEVEL_SEPARATOR + UNIQUE_ATTRIBUTE_SPECIFIER;
        String uniqueVarKeySpecifierValue = getSystemVariableValue(uniqueVarKeySpecifier);
        if (uniqueVarKeySpecifierValue == null) {
            throw new ConfigurationException(String
                    .format(Locale.ENGLISH, "Locating unique system variable key for system variable %s from " +
                                            "default attributes %s failed. Custom unique system variable key %s " +
                                            "is not specified as well.", systemVarKey,
                            Arrays.toString(UNIQUE_ATTRIBUTE_NAMES), uniqueVarKeySpecifier));
        }

        // Get the value for unique system variable
        String uniqueVarKey = uniqueVarKeySegment + CONFIG_LEVEL_SEPARATOR + arrayGroupString + CONFIG_LEVEL_SEPARATOR +
                              uniqueVarKeySpecifierValue;
        String uniqueEnvValue = getSystemVariableValue(uniqueVarKey);
        if (uniqueEnvValue == null) {
            throw new ConfigurationException(String
                    .format(Locale.ENGLISH, "Value for unique system variable %s is null when overriding " +
                                            "system variable %s", uniqueVarKey, systemVarKey));
        }

        return ImmutablePair.of(uniqueVarKeySpecifierValue, uniqueEnvValue);
    }

    /**
     * Returns the system property value or environment variable value (highest priority for environment variable)
     * for the given key.
     *
     * @param systemVariableKey key of the system property or environment variable
     * @return system property value or environment variable value for the given key
     * @throws ConfigurationException when the system property or the environment variable cannot be found for the
     *                                given key
     */
    private String getSystemVariableValue(String systemVariableKey) throws ConfigurationException {
        if (System.getenv(systemVariableKey) != null) {
            return System.getenv(systemVariableKey);
        }
        return System.getProperty(systemVariableKey);
    }

    /**
     * Check if the given string is a positive integer.
     *
     * @param configKeyElement String to check whether a positive integer
     * @return {@code true} if the string is a positive integer, {@code false} otherwise
     */
    private boolean isPositiveInteger(String configKeyElement) {
        try {
            return Integer.parseInt(configKeyElement) >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Returns the collection parametrised type.
     *
     * @param field collection field
     * @return collection parametrised type.
     */
    private Class<?> getCollectionType(Field field) {
        ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
        return (Class<?>) parameterizedType.getActualTypeArguments()[0];
    }

    /**
     * Returns the configuration object for the given YAML string.
     *
     * @param configClass returning configuration object type
     * @param classLoader class loader of the configuration object
     * @param yamlString  YAML string
     * @param <T>         type of the returning configuration object
     * @return configuration object for the given YAML string
     */
    private <T> T getConfigurationObject(Class<T> configClass, ClassLoader classLoader, String yamlString) {
        Yaml yaml = new Yaml(new CustomClassLoaderConstructor(configClass, classLoader));
        yaml.setBeanAccess(BeanAccess.FIELD);
        return yaml.loadAs(yamlString, configClass);
    }

    /**
     * Returns the field with the given field name of the given class.
     *
     * @param classObject instance of the class to obtain the given field
     * @param fieldName   name of the field to be returned
     * @param <T>         type of the instance of the class to obtain the given field
     * @return the field with the given field name of the given class
     * @throws ConfigurationException thrown when the field is not found in the class
     */
    private <T> Field getClassField(T classObject, String fieldName) throws ConfigurationException {
        if (!SourceVersion.isName(fieldName)) {
            throw new ConfigurationException(String.format(Locale.ENGLISH,
                    "Field name %s is not valid", fieldName));
        }
        String lowerCaseConfigKey = fieldName.toLowerCase(Locale.ENGLISH);
        Optional<Field> field = Arrays.stream(classObject.getClass().getDeclaredFields())
                .filter(fieldEntry -> fieldEntry.getName().toLowerCase(Locale.ENGLISH).equals(lowerCaseConfigKey))
                .findFirst();
        if (!field.isPresent()) {
            throw new ConfigurationException(String.format(Locale.ENGLISH,
                    "Field %s not found in %s", fieldName, classObject.getClass()));
        }
        field.get().setAccessible(true);
        return field.get();
    }

    /**
     * Sets a given value to a given field in a given class.
     *
     * @param classObject Object in which the value should be set to the field
     * @param configKey   Field in which the given value should be set to
     * @param value       value to be set to the given field in the given class
     * @param <T>         Type of class in which the value should be set to the field
     * @throws ConfigurationException thrown when the field to set the value is not present in the class
     */
    private <T> void setFieldValue(T classObject, String configKey, Object value) throws ConfigurationException {
        Field field = getClassField(classObject, configKey);

        if (field.getType().isPrimitive()) {
            value = castToWrapperType(field, value.toString());
        }

        try {
            field.set(classObject, value);
        } catch (IllegalAccessException e) {
            throw new ConfigurationException(String.format(Locale.ENGLISH,
                    "Error in overriding deployment config value with system config key %s value", configKey));
        }
    }

    /**
     * Returns the value of the given field of the given object.
     *
     * @param classObject Object to retrieve field value from
     * @param field       he field to get the value from
     * @param <T>         type of the object to retrieve field value from
     * @return value of the given field of the given object
     * @throws ConfigurationException when error occurred in obtaining the field value
     */
    private <T> Object getFieldValue(T classObject, Field field) throws ConfigurationException {
        try {
            return field.get(classObject);
        } catch (IllegalAccessException e) {
            throw new ConfigurationException(String.format(Locale.ENGLISH,
                    "Error in obtaining value for field %s in %s", field.getName(), classObject.getClass()));
        }
    }

    /**
     * Returns the value of the given field of the given object.
     *
     * @param classObject Object to retrieve field value from
     * @param fieldName   Name of the field to get the value from
     * @param <T>         type of the object to retrieve field value from
     * @return value of the given field of the given object
     * @throws ConfigurationException when error occurred in obtaining the field value
     */
    private <T> Object getFieldValue(T classObject, String fieldName) throws ConfigurationException {
        Field field = getClassField(classObject, fieldName);
        return getFieldValue(classObject, field);
    }

    /**
     * Cast a class field value to it's primitive type.
     *
     * @param field class field
     * @param value value to be casted to the primitive type
     * @return object casted to its primitive type
     */
    private Object castToWrapperType(Field field, String value) {
        Class<?> fieldType = field.getType();
        if (fieldType.isAssignableFrom(short.class)) {
            return Short.parseShort(value);
        } else if (fieldType.isAssignableFrom(int.class)) {
            return Integer.parseInt(value);
        } else if (fieldType.isAssignableFrom(long.class)) {
            return Long.parseLong(value);
        } else if (fieldType.isAssignableFrom(float.class)) {
            return Float.parseFloat(value);
        } else if (fieldType.isAssignableFrom(double.class)) {
            return Double.parseDouble(value);
        } else if (fieldType.isAssignableFrom(boolean.class)) {
            return Boolean.parseBoolean(value);
        } else if (fieldType.isAssignableFrom(char.class)) {
            return value.charAt(0);
        } else if (fieldType.isAssignableFrom(byte.class)) {
            return Byte.parseByte(value);
        } else {
            return value;
        }
    }

    /**
     * This method loads deployment configs in deployment.yaml.
     * loads only if deployment configuration not exists
     */
    private void loadDeploymentConfiguration(ConfigFileReader configFileReader) throws ConfigurationException {
        if (deploymentConfigs == null) {
            deploymentConfigs = configFileReader.getDeploymentConfiguration();
        }
    }

    /**
     * This method will concatenate and return the placeholder types.. Placeholder types will be separated
     * by | token. This method will be used to create the {@link ConfigProviderImpl#PLACEHOLDER_REGEX}.
     *
     * @return String that contains placeholder types which are separated by | token.
     */
    private static String getPlaceholderString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Placeholder placeholder : Placeholder.values()) {
            stringBuilder.append(placeholder.getValue()).append("|");
        }
        String value = stringBuilder.substring(0, stringBuilder.length() - 1);
        logger.debug("PlaceHolders String: {}", value);
        return value;
    }

    /**
     * This method returns the new value after processing the placeholders. This method can process multiple
     * placeholders within the same String as well.
     *
     * @param inputString Placeholder that needs to be replaced
     * @return New getContent which corresponds to inputString
     */
    private String processPlaceholder(String inputString) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(inputString);
        //Match all placeholders in the inputString
        while (matcher.find()) {
            //Group 3 corresponds to the key in the inputString
            String key = matcher.group(3);
            //Group 4 corresponds to the value of the inputString
            String value = matcher.group(4);
            //Group 7 corresponds to the default value in the inputString. If default value is not available, this
            // will be null
            String defaultValue = matcher.group(7);
            switch (key) {
                case "env":
                    inputString = processValue(System::getenv, value, inputString, defaultValue, Placeholder.ENV);
                    break;
                case "sys":
                    inputString = processValue(System::getProperty, value, inputString, defaultValue, Placeholder.SYS);
                    break;
                case "sec":
                    try {
                        SecureVault secureVault = getSecureVault().orElseThrow(() ->
                                new ConfigurationRuntimeException("Secure Vault service is not available"));
                        String newValue = new String(secureVault.resolve(value));
                        inputString = inputString.replaceFirst(PLACEHOLDER_REGEX, "$1" + newValue + "$8");
                    } catch (SecureVaultException e) {
                        throw new ConfigurationRuntimeException("Unable to resolve the given alias", e);
                    }
                    break;
                default:
                    String msg = String.format("Unsupported placeholder: %s", key);
                    logger.error(msg);
                    throw new ConfigurationRuntimeException(msg);
            }
        }
        return inputString;
    }

    /**
     * This method process a given placeholder string and returns the string with replaced new value.
     *
     * @param func         Function to apply.
     * @param key          Environment Variable/System Property key.
     * @param inputString  String which needs to process.
     * @param defaultValue Default value of the placeholder. If default value is not available, this is null.
     * @param type         Type of the placeholder (env/sys/sec) This is used to print the error message.
     * @return String which has the new value instead of the placeholder.
     */
    private static String processValue(Function<String, String> func, String key, String inputString, String
            defaultValue, Placeholder type) {
        String newValue = func.apply(key);
        //If the new value is not null, replace the placeholder with the new value and return the string.
        if (newValue != null) {
            return inputString.replaceFirst(PLACEHOLDER_REGEX, "$1" + newValue + "$8");
        }
        //If the new value is empty and the default value is not empty, replace the placeholder with the default
        // value and return the string
        if (defaultValue != null) {
            return inputString.replaceFirst(PLACEHOLDER_REGEX, "$1" + defaultValue + "$8");
        }
        //Otherwise print an error message and throw na exception
        String msg;
        if (Placeholder.ENV.getValue().equals(type.getValue())) {
            msg = String.format("Environment variable %s not found. Placeholder: %s", key,
                    inputString);
        } else if (Placeholder.SYS.getValue().equals(type.getValue())) {
            msg = String.format("System property %s not found. Placeholder: %s", key,
                    inputString);
        } else {
            msg = String.format("Unsupported placeholder type: %s", type.getValue());
        }
        logger.error(msg);
        throw new ConfigurationRuntimeException(msg);
    }

    private Optional<SecureVault> getSecureVault() {
        return Optional.ofNullable(secureVault);
    }
}
