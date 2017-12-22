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
package org.wso2.carbon.config.maven.plugin;

import com.google.gson.Gson;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigConstants;
import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;
import org.wso2.carbon.config.annotation.Ignore;
import org.wso2.carbon.config.maven.plugin.exceptions.ConfigurationMavenRuntimeException;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;


/**
 * This class will create configuration document from bean class annotated in the project.
 * Get all configuration bean classes in the project from the resource created from ConfigurationProcessor
 *
 * @since 1.0.0
 */
@Mojo(name = "create-doc")
public class ConfigDocumentMojo extends AbstractMojo {

    private static final Logger logger = LoggerFactory.getLogger(ConfigDocumentMojo.class.getName());
    private static final String YAML_FILE_EXTENTION = ".yaml";
    private static final String JSON_FILE_EXTENTION = ".json";
    private static final String NEW_LINE_REGEX_PATTERN = "\\r?\\n";
    private static final String COMMENT_KEY_PREFIX = "comment-";
    private static final String POSSIBLE_VALUE_PREFIX = "options-";
    private static final String OPTIONS_FIELD_COMMENT = "Possible Values - ";
    private static final String COMMENT_KEY_REGEX_PATTERN = COMMENT_KEY_PREFIX + ".*";
    private static final String POSSIBLE_VALUE_REGEX_PATTERN = POSSIBLE_VALUE_PREFIX + ".*";
    private static final String COMMENT_REGEX_PATTERN = "#" + ".*";
    private static final String EMPTY_LINE_REGEX_PATTERN = "(?m)^[ \t]*\r?\n";
    private static final String MANDATORY_FIELD_COMMENT = "THIS IS A MANDATORY FIELD";
    private static final String UTF_8_CHARSET = "UTF-8";
    private static final String PLUGIN_DESCRIPTOR_KEY = "pluginDescriptor";
    private static final String LICENSE_FILE = "LICENSE.txt";
    private static final String CSS_FILE = "configuration.css";
    private static final String JS_FILE = "handlebars-v4.0.11.js";
    private static final String MANDATORY_FIELD_ENTRY = "Required";

    @Parameter(defaultValue = "${project}", required = true)
    private MavenProject project;

    @Parameter(property = "configclasses")
    protected String[] configclasses;

    /**
     * Enum containing project artifact types.
     * This enum is used when adding project to the class path during plugin execution.
     * Only the specified types will be added to the class path. Other type will not be added
     */
    private enum ProjectArtifactTypes {
        JAR,
        BUNDLE
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // get the qualified names of all configuration bean in the project, if array is empty, return without further
        // processing and we not create any configuration document file.
        String[] configurationClasses = getConfigurationClasses();
        if (configurationClasses == null || configurationClasses.length == 0) {
            logger.info("Configuration classes doesn't exist in the component, hence configuration file not create");
            return;
        }

        PluginDescriptor descriptor = (PluginDescriptor) getPluginContext().get(PLUGIN_DESCRIPTOR_KEY);
        ClassRealm realm = descriptor.getClassRealm();
        addProjectToClasspath(realm);
        addDependenciesToClasspath(realm);

        // process configuration bean to create configuration document
        for (String configClassName : configurationClasses) {
            // configuration map used to generate the YAML file
            Map<String, Object> finalMap = new LinkedHashMap<>();
            // configuration map used to create the object model
            Map<String, Object> contentMap = new LinkedHashMap<>();
            try {
                Class configClass = realm.loadClass(configClassName);
                if (configClass != null && configClass.isAnnotationPresent(Configuration.class)) {
                    // read configuration annotation
                    // Fix the issue #3, set the system.property before creating the object, to notify it is called
                    // by maven plugin.
                    System.setProperty(ConfigConstants.SYSTEM_PROPERTY_DOC_GENERATION, Boolean.toString(true));
                    Configuration configuration = (Configuration) configClass.getAnnotation(Configuration.class);
                    Object configObject = configClass.newInstance();
                    System.clearProperty(ConfigConstants.SYSTEM_PROPERTY_DOC_GENERATION);
                    Object configElements = readConfigurationElements(configObject, Boolean.TRUE);
                    // add description comment to the root node.
                    finalMap.put(COMMENT_KEY_PREFIX + configuration.namespace(), createDescriptionComment(configuration
                            .description()));
                    // add root node to the config Map
                    finalMap.put(configuration.namespace(), configElements);
                    contentMap.put(configuration.namespace(), configElements);
                    // write configuration map as a yaml file
                    writeConfigurationFile(finalMap, configuration.namespace());
                    copyCSS(readCSS());
                    copyJS(readJS());
                    List<DataModel> configItems = new ArrayList<>();
                    List<DataModel> configItemsList = generateContent(contentMap, configItems);
                    generateJSONFiles(getYamlString(contentMap), configuration.displayName(), configItemsList,
                            configuration.namespace());

                } else {
                    logger.error("Error while loading the configuration class : " + configClassName);
                }
            } catch (ClassNotFoundException e) {
                logger.error("Error while creating new instance of the class : " + configClassName, e);
            } catch (InstantiationException | IllegalAccessException e) {
                logger.error("Error while initializing the configuration class : " + configClassName, e);
            }
        }
    }

    /**
     * read license header from the resource file(LICENSE.txt) and add copyright year.
     *
     * @return license header
     */
    private String getLicenseHeader() {
        InputStream inputStream = ConfigDocumentMojo.class.getClassLoader().getResourceAsStream(LICENSE_FILE);
        StringBuilder sb = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, UTF_8_CHARSET))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            logger.error("Error while reading the license header file.", e);
        }
        Calendar now = Calendar.getInstance();   // Gets the current date and time
        int year = now.get(Calendar.YEAR);      // The current year as an int
        return String.format(sb.toString(), year);
    }

    /**
     * write configuration map to configuration file.
     *
     * @param finalMap configuration map
     * @param filename filename with out extension.
     * @throws MojoExecutionException
     */
    private void writeConfigurationFile(Map<String, Object> finalMap, String filename) throws MojoExecutionException {
        // create the yaml string from the map
        Yaml yaml = new Yaml();
        String content = yaml.dumpAsMap(finalMap);
        // remove all comments key lines from the content. this was added as key of each field description in the map.
        content = content.replaceAll(COMMENT_KEY_REGEX_PATTERN, "");
        content = content.replaceAll(POSSIBLE_VALUE_REGEX_PATTERN, "");
        content = content.replaceAll(EMPTY_LINE_REGEX_PATTERN, "");
        File configDir = new File(project.getBuild().getOutputDirectory(), ConfigConstants.CONFIG_DIR);

        // create config directory inside project output directory to save config files
        if (!configDir.exists() && !configDir.mkdirs()) {
            throw new MojoExecutionException("Error while creating config directory in classpath");
        }
        // write the yaml string to the configuration file in config directory
        try (PrintWriter out = new PrintWriter(new File(configDir.getPath(), filename
                + YAML_FILE_EXTENTION), UTF_8_CHARSET)) {
            out.println(getLicenseHeader());
            out.println(content);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            throw new MojoExecutionException("Error while creating new resource file from the classpath", e);
        }
        // add configuration document to the project resources under config-docs/ directory.
        Resource resource = new Resource();
        resource.setDirectory(configDir.getAbsolutePath());
        resource.setTargetPath(ConfigConstants.CONFIG_DIR);
        project.addResource(resource);
    }

    /**
     * read css file in the resource folder.
     *
     * @throws MojoExecutionException
     */
    private String readCSS() throws MojoExecutionException {
        InputStream inputStream = ConfigDocumentMojo.class.getClassLoader().getResourceAsStream(CSS_FILE);
        StringBuilder sb = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, UTF_8_CHARSET))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            logger.error("Error while reading the css file.", e);
        }

        return sb.toString();

    }

    /**
     * copy css file in the resource folder.
     *
     * @throws MojoExecutionException
     */
    private void copyCSS(String css) throws MojoExecutionException {

        File configDir = new File(project.getBuild().getOutputDirectory(), ConfigConstants.CONFIG_DIR);

        if (!configDir.exists() && !configDir.mkdirs()) {
            throw new MojoExecutionException("Error while creating config directory in classpath");
        }

        try (PrintWriter out = new PrintWriter(new File(configDir.getPath(), CSS_FILE), UTF_8_CHARSET)) {
            out.println(css);
        } catch (FileNotFoundException |
                UnsupportedEncodingException e) {
            throw new MojoExecutionException("Error while creating new resource file from the classpath", e);
        }

        Resource resource = new Resource();
        resource.setDirectory(configDir.getAbsolutePath());
        resource.setTargetPath(ConfigConstants.CONFIG_DIR);
        project.addResource(resource);
    }

    /**
     * read handlebars.js file in the resource folder.
     *
     * @throws MojoExecutionException
     */
    private String readJS() throws MojoExecutionException {
        InputStream inputStream = ConfigDocumentMojo.class.getClassLoader().getResourceAsStream(JS_FILE);
        StringBuilder sb = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, UTF_8_CHARSET))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            logger.error("Error while reading the css file.", e);
        }

        return sb.toString();

    }

    /**
     * copy handlebars.js file in the resource folder.
     *
     * @throws MojoExecutionException
     */
    private void copyJS(String js) throws MojoExecutionException {

        File configDir = new File(project.getBuild().getOutputDirectory(), ConfigConstants.CONFIG_DIR);

        if (!configDir.exists() && !configDir.mkdirs()) {
            throw new MojoExecutionException("Error while creating config directory in classpath");
        }

        try (PrintWriter out = new PrintWriter(new File(configDir.getPath(), JS_FILE), UTF_8_CHARSET)) {
            out.println(js);
        } catch (FileNotFoundException |
                UnsupportedEncodingException e) {
            throw new MojoExecutionException("Error while creating new resource file from the classpath", e);
        }

        Resource resource = new Resource();
        resource.setDirectory(configDir.getAbsolutePath());
        resource.setTargetPath(ConfigConstants.CONFIG_DIR);
        project.addResource(resource);
    }

    /**
     * create the YAML String from the contentMap
     *
     * @param contentMap configuration map
     * @throws MojoExecutionException
     */
    private String getYamlString(Map<String, Object> contentMap) throws MojoExecutionException {
        // create the yaml string from the map
        Yaml yaml = new Yaml();
        String content = yaml.dumpAsMap(contentMap);
        // remove all comments key lines from the content.
        content = content.replaceAll(COMMENT_KEY_REGEX_PATTERN, "");
        content = content.replaceAll(POSSIBLE_VALUE_REGEX_PATTERN, "");
        content = content.replaceAll(COMMENT_REGEX_PATTERN, "");
        content = content.replaceAll(EMPTY_LINE_REGEX_PATTERN, "");

        return content;
    }

    /**
     * creating an object model to pass to the handlebars template
     * using configuration map
     *
     * @param contentMap      configuration map
     * @param configItemsList array list object
     * @throws MojoExecutionException
     */
    private List<DataModel> generateContent(Map<String, Object> contentMap,
                                            List<DataModel> configItemsList) throws MojoExecutionException {
        String elementName = null;
        String description = null;
        String dataType = null;
        String defaultValue = null;
        String required = null;
        String possibleValues = null;

        for (Map.Entry<String, Object> entry : contentMap.entrySet()) {

            if (entry.getKey().contains(COMMENT_KEY_PREFIX)) {

                elementName = entry.getKey().replaceAll(COMMENT_KEY_PREFIX, "");
                description = entry.getValue().toString().replaceAll(NEW_LINE_REGEX_PATTERN, "")
                        .replaceAll("#", "")
                        .replaceAll(MANDATORY_FIELD_COMMENT, "");

                if (entry.getValue().toString().endsWith(MANDATORY_FIELD_COMMENT)) {
                    required = MANDATORY_FIELD_ENTRY;
                } else {
                    required = "";
                }

            } else if (entry.getKey().contains(POSSIBLE_VALUE_PREFIX)) {
                possibleValues = entry.getValue().toString().replaceAll(OPTIONS_FIELD_COMMENT, "")
                        .replaceAll("#", "");
            } else if (!(entry.getValue() instanceof LinkedHashMap)) {

                if (entry.getValue() instanceof List) {
                    dataType = entry.getValue().getClass().getSimpleName();
                    String valueList = entry.getValue().toString().replaceAll("\\[", "").replaceAll("\\]", "");
                    String[] values = valueList.split(",");
                    StringBuilder sb = new StringBuilder();
                    for (String value : values) {
                        if (sb.length() == 0) {
                            sb.append("<br />").append(sb).append(value);
                        } else {
                            sb.append("<br />").append(value);
                        }
                    }
                    defaultValue = sb.toString();
                } else if (entry.getValue() instanceof Map) {
                    dataType = entry.getValue().getClass().getSimpleName();
                    String valueList = entry.getValue().toString().replaceAll("\\{", "").replaceAll("\\}", "");
                    String[] values = valueList.split(",");
                    StringBuilder builder = new StringBuilder();
                    for (String items : values) {
                        if (builder.length() == 0) {
                            builder.append("<br />").append(builder).append(items);
                        } else {
                            builder.append("<br />").append(items);
                        }
                    }
                    defaultValue = builder.toString();

                } else {
                    dataType = entry.getValue().getClass().getSimpleName();
                    defaultValue = entry.getValue().toString();
                }

                DataModel dataModel = new DataModel();
                if (elementName != null) {
                    dataModel.setElementName(elementName);
                    dataModel.setDataType(dataType);
                    dataModel.setDescription(description);
                    dataModel.setDefaultValue(defaultValue);
                    dataModel.setRequired(required);
                    dataModel.setPossibleValues(possibleValues);

                    dataType = "";
                    defaultValue = "";
                    possibleValues = "";

                    configItemsList.add(dataModel);
                }

            } else if (entry.getValue() instanceof LinkedHashMap) {

                DataModel dataModel = new DataModel();
                if (elementName == null) {
                    generateContent((Map<String, Object>) entry.getValue(), configItemsList);
                } else {
                    dataModel.setElementName(elementName);
                    dataModel.setDataType(dataType);
                    dataModel.setDescription(description);
                    dataModel.setDefaultValue(defaultValue);
                    dataModel.setRequired(required);
                    dataModel.setPossibleValues(possibleValues);

                    dataType = "";
                    defaultValue = "";
                    possibleValues = "";
                    configItemsList.add(dataModel);

                    generateContent((Map<String, Object>) entry.getValue(), dataModel.childElements);
                }
            }
        }
        return configItemsList;
    }

    /**
     * generating the JSON files and
     * write them to the class output directory
     *
     * @param configItems configuration list
     * @throws MojoExecutionException, IOException
     */
    private void generateJSONFiles(String yamlString, String displayName,
                                   List<DataModel> configItems, String filename) throws MojoExecutionException {
        Gson gson = new Gson();
        DataContext dataContext = new DataContext();
        dataContext.setElements(configItems);
        dataContext.setDisplayName(displayName);
        dataContext.setYamlString(yamlString);
        Object json = gson.toJson(dataContext);

        File configDir = new File(project.getBuild().getOutputDirectory(), ConfigConstants.CONFIG_DIR);

        // create config directory inside project output directory to save config files
        if (!configDir.exists() && !configDir.mkdirs()) {
            throw new MojoExecutionException("Error while creating config directory in classpath");
        }

        try (PrintWriter out = new PrintWriter(new File(configDir.getPath(), filename
                + JSON_FILE_EXTENTION), UTF_8_CHARSET)) {
            out.println(json);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            throw new MojoExecutionException("Error while creating new resource file from the classpath", e);
        }

        // add configguration document to the project resources under config-docs/ directory.
        Resource resource = new Resource();
        resource.setDirectory(configDir.getAbsolutePath());
        resource.setTargetPath(ConfigConstants.CONFIG_DIR);
        project.addResource(resource);

    }

    /**
     * Read the resource file created by ConfigurationProcessor and create array of qualified names of bean classes.
     *
     * @return Array of qualified Name of configuration beans
     * @throws MojoExecutionException
     */
    private String[] getConfigurationClasses() throws MojoExecutionException {
        String[] classList = null;
        if (configclasses != null && configclasses.length != 0) {
            classList = configclasses;
        } else {
            File configFile = new File(project.getBuild().getOutputDirectory(), ConfigConstants.TEMP_CONFIG_FILE_NAME);
            if (configFile.exists()) {
                try (Scanner scanner = new Scanner(configFile, UTF_8_CHARSET)) {
                    String content = scanner.useDelimiter("\\Z").next();
                    classList = content.split(",");
                } catch (FileNotFoundException e) {
                    throw new MojoExecutionException("Error while reading the configuration classes file", e);
                }
            }
        }
        return classList;
    }

    /**
     * This method will recursively run through the configuration bean class and create a map with,
     * key : field name
     * value : field value
     * description : field descriptions added in Element annotation. Omitting the description of composite type of an
     * array and argument type of a collection.
     *
     * @param configObject      configuration bean object.
     * @param enableDescription flag to enable description of the field. if true, it reads the annotated description
     *                          and add description before each field. omit the description otherwise.
     *                          This is added to omit description of composite type of an array and argument type of
     *                          a collection
     * @return Map of field name and values
     * @throws MojoExecutionException
     */
    private Map<String, Object> readConfigurationElements(Object configObject, boolean enableDescription) throws
            MojoExecutionException {
        if (configObject == null) {
            throw new MojoExecutionException("Error while reading the configuration elements, config object is null");
        }

        Map<String, Object> elementMap = new LinkedHashMap<>();
        Field[] fields = configObject.getClass().getDeclaredFields();

        for (Field field : fields) {
            // if @Ignore, it omits field from the configuration.
            if (field.getAnnotation(Ignore.class) != null) {
                continue;
            }
            // if the field is not accessible, make it accessible to read the value of the field.
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            // read the field type to check whether it is a composite type
            Class fieldTypeClass = field.getType();

            // read the field value from the bean object. IllegalAccessException will not occur, since it made
            // accessible.
            Object fieldValue = null;
            try {
                fieldValue = field.get(configObject);
            } catch (IllegalAccessException e) {
                logger.error("Error while accessing the value of the field: " + field.getName(), e);
            }

            // read the description of the field. if the required flag is set, it appends additional mandatory field
            // comment to the description.
            String fieldDescription = null;
            if (enableDescription && field.isAnnotationPresent(Element.class)) {
                Element element = field.getAnnotation(Element.class);
                fieldDescription = createDescriptionComment(element.description());
                if (element.required()) {
                    fieldDescription = fieldDescription + "# " + MANDATORY_FIELD_COMMENT;
                }
            }

            // read the possible values of the field. And create the possible value description
            String possibleValueDescription = null;
            if (enableDescription && field.isAnnotationPresent(Element.class)) {
                Element element = field.getAnnotation(Element.class);
                if (!(Arrays.asList(element.possibleValues()).contains(ConfigConstants.NOT_APPLICABLE))) {
                    possibleValueDescription = createOptionsComment(element.possibleValues());
                }
            }

            // check whether the field value is null, to avoid further processing, which is not required.
            if (fieldValue == null) {
                elementMap.put(field.getName(), null);
                continue;
            }

            // check whether the field type is another configuration bean
            if (fieldTypeClass != null && fieldTypeClass.isAnnotationPresent(Configuration.class)) {
                Configuration configuration = (Configuration) fieldTypeClass.getAnnotation(Configuration.class);
                fieldDescription = createDescriptionComment(configuration.description());
                fieldValue = readConfigurationElements(fieldValue, Boolean.TRUE);
                // check whether the field type is an enum
            } else if (fieldTypeClass != null && fieldTypeClass.isEnum()) {
                fieldValue = fieldValue.toString();
                // check whether the field type is an array
            } else if (fieldTypeClass != null && fieldTypeClass.isArray()) {
                Class compositeType = fieldTypeClass.getComponentType();
                // check whether the composite type is another configuration bean
                if (compositeType != null && compositeType.isAnnotationPresent(Configuration.class)) {
                    int length = Array.getLength(fieldValue);
                    Object[] elementArray = new Object[length];
                    for (int i = 0; i < length; i++) {
                        Object arrayElement = Array.get(fieldValue, i);
                        elementArray[i] = readConfigurationElements(arrayElement, Boolean.FALSE);
                    }
                    fieldValue = elementArray;
                }
                // check whether the field type is an collection
            } else if (fieldTypeClass != null && Collection.class.isAssignableFrom(fieldTypeClass)) {
                ParameterizedType paramType = (ParameterizedType) field.getGenericType();
                Class<?> argumentType = (Class<?>) paramType.getActualTypeArguments()[0];
                // check whether the argument type is another configuration bean
                if (argumentType != null && argumentType.isAnnotationPresent(Configuration.class)) {
                    final Collection<?> c = (Collection<?>) fieldValue;
                    Object[] elementArray = new Object[c.size()];
                    int i = 0;
                    for (final Object obj : c) {
                        elementArray[i] = readConfigurationElements(obj, Boolean.FALSE);
                        i++;
                    }
                    fieldValue = elementArray;
                }
            } else if (fieldValue instanceof Optional) {
                if (((Optional) fieldValue).isPresent()) {
                    fieldValue = ((Optional) fieldValue).get();
                } else {
                    fieldValue = null;
                }
            }

            // add description of each field, if description not null
            if (fieldDescription != null) {
                elementMap.put(COMMENT_KEY_PREFIX + field.getName(), fieldDescription);
            }

            // add possible values description of each field if possible values are present
            if (possibleValueDescription != null) {
                elementMap.put(POSSIBLE_VALUE_PREFIX + field.getName(), possibleValueDescription);
            }
            // add field to the element map
            elementMap.put(field.getName(), fieldValue);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("class name: " + configObject.getClass().getSimpleName() + " | default configurations :: "
                    + elementMap.toString());
        }
        return elementMap;
    }

    /**
     * convert the annotated field description to comment.
     *
     * @param description field description
     * @return comment string
     */
    private String createDescriptionComment(String description) {
        StringBuilder builder = new StringBuilder();
        String lines[] = description.split(NEW_LINE_REGEX_PATTERN);
        for (String line : lines) {
            builder.append("# ").append(line).append("\n");
        }
        return builder.toString();
    }

    /**
     * convert the annotated possible values to comment.
     *
     * @param options possible values
     * @return comment string
     */
    private String createOptionsComment(String[] options) {
        String delimiter = ",";
        StringBuilder sb = new StringBuilder();
        StringBuilder sb1 = new StringBuilder();

        for (String element : options) {
            if (sb.length() > 0) {
                sb.append(delimiter);
            }
            sb.append(element);
        }

        String items = sb.toString();
        String itemsComment = sb1.append("# ").append(OPTIONS_FIELD_COMMENT).append(items).toString();
        return itemsComment;
    }

    /**
     * Add project to the class realm.
     *
     * @param realm class realm
     */
    private void addProjectToClasspath(ClassRealm realm) {
        if (Arrays.stream(ProjectArtifactTypes.values()).anyMatch(type -> type.name().equalsIgnoreCase(project
                .getArtifact().getType()))) {
            try {
                final URL url = project.getArtifact().getFile().toURI().toURL();
                realm.addURL(url);
            } catch (MalformedURLException e) {
                throw new ConfigurationMavenRuntimeException("Error when adding project to the class realm", e);
            }
        }
    }

    /**
     * Add dependencies to the class path.
     *
     * @param realm Class Realm
     */
    private void addDependenciesToClasspath(ClassRealm realm) {
        for (Object artifact : project.getDependencyArtifacts()) {
            try {
                final URL url = ((Artifact) artifact).getFile().toURI().toURL();
                realm.addURL(url);
            } catch (MalformedURLException e) {
                throw new ConfigurationMavenRuntimeException("Error when adding dependencies to the class realm", e);
            }
        }
    }

}

