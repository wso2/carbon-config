/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.config.reader;

import com.github.mustachejava.DefaultMustacheFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Merge multiple YML files into a single file, and substitute for any environment variables found.
 */
public class YmlMerger {

    private static final Logger LOG = LoggerFactory.getLogger(YmlMerger.class);
    private static final DefaultMustacheFactory DEFAULT_MUSTACHE_FACTORY = new DefaultMustacheFactory();

    private final Yaml snakeYaml;
    private Map<String, Object> variablesToReplace = new HashMap<String, Object>();

    public YmlMerger() {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumperOptions.setPrettyFlow(true);

        dumperOptions.setTimeZone(TimeZone.getTimeZone("UTC"));
        this.snakeYaml = new Yaml(dumperOptions);
    }

    public YmlMerger setVariablesToReplace(Map<String, String> vars) {
        this.variablesToReplace.clear();
        this.variablesToReplace.putAll(vars);
        return this;
    }

    /**
     * Merges the files at given paths to a map representing the resulting YAML structure.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> mergeYamlContents(List<String> contents) {
        Map<String, Object> mergedResult = new LinkedHashMap<String, Object>();
        for (String yamlContent : contents) {
            // Substitute variables.
            int bufferSize = yamlContent.length() + 100;
            final StringWriter writer = new StringWriter(bufferSize);
            DEFAULT_MUSTACHE_FACTORY.compile(new StringReader(yamlContent), "yaml-mergeYamlFiles-" +
                    System.currentTimeMillis()).execute(writer, variablesToReplace);

            // Parse the YAML.
            String yamlString = writer.toString();
            final Map<String, Object> yamlToMerge = (Map<String, Object>) this.snakeYaml.load(yamlString);

            // Merge into results map.
            mergeStructures(mergedResult, yamlToMerge);
        }
        return mergedResult;
    }

    @SuppressWarnings("unchecked")
    private void mergeStructures(Map<String, Object> targetTree, Map<String, Object> sourceTree) {
        if (sourceTree == null) {
            return;
        }

        for (Map.Entry entry : sourceTree.entrySet()) {
            Object yamlValue = entry.getValue();
            String key = entry.getKey().toString();
            if (yamlValue == null) {
                addToMergedResult(targetTree, key, null);
                continue;
            }

            Object existingValue = targetTree.get(key);
            if (existingValue != null) {
                if (yamlValue instanceof Map) {
                    if (existingValue instanceof Map) {
                        mergeStructures((Map<String, Object>) existingValue, (Map<String, Object>) yamlValue);
                    } else if (existingValue instanceof String) {
                        throw new IllegalArgumentException("Cannot merge Yaml files; complex element into a " +
                                "simple element: " + key);
                    } else {
                        throw unknownValueType(key, yamlValue);
                    }
                } else if (yamlValue instanceof List) {
                    mergeLists(targetTree, key, yamlValue);
                } else if (yamlValue instanceof String
                        || yamlValue instanceof Boolean
                        || yamlValue instanceof Double
                        || yamlValue instanceof Integer) {
                    LOG.debug("Overriding value of " + key + " with value " + yamlValue);
                    addToMergedResult(targetTree, key, yamlValue);

                } else {
                    throw unknownValueType(key, yamlValue);
                }

            } else {
                if (yamlValue instanceof Map
                        || yamlValue instanceof List
                        || yamlValue instanceof String
                        || yamlValue instanceof Boolean
                        || yamlValue instanceof Integer
                        || yamlValue instanceof Double) {
                    LOG.debug("Adding new key->value: " + key + " -> " + yamlValue);
                    addToMergedResult(targetTree, key, yamlValue);
                } else {
                    throw unknownValueType(key, yamlValue);
                }
            }
        }
    }

    private static IllegalArgumentException unknownValueType(String key, Object yamlValue) {
        final String msg = "Cannot merge Yaml files; element of unknown type: " + key + ": " +
                yamlValue.getClass().getName();
        LOG.error(msg);
        return new IllegalArgumentException(msg);
    }

    private static void addToMergedResult(Map<String, Object> mergedResult, String key, Object yamlValue) {
        mergedResult.put(key, yamlValue);
    }

    @SuppressWarnings("unchecked")
    private static void mergeLists(Map<String, Object> mergedResult, String key, Object yamlValue) {
        if (!(yamlValue instanceof List && mergedResult.get(key) instanceof List)) {
            throw new IllegalArgumentException("Cannot merge Yaml files; list with a non-list: " + key);
        }

        List<Object> originalList = (List<Object>) mergedResult.get(key);
        originalList.clear();
        originalList.addAll((List<Object>) yamlValue);
    }


    String mergeToString(List<String> contentToMerge) {
        Map<String, Object> merged = mergeYamlContents(contentToMerge);
        return exportToString(merged);
    }

    private String exportToString(Map<String, Object> merged) {
        return snakeYaml.dump(merged);
    }
}
