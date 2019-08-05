/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.config.provider;

import org.wso2.carbon.config.ConfigurationException;

import java.util.ArrayList;

/**
 * ConfigProvider provides the configuration mapping of the class namespace.
 * This will update the configuration values with
 * following placeholders ${env:alias}, ${sys:alias} and ${sec:alias}
 *
 * @since 1.0.0
 */
public interface ConfigProvider {

    /**
     * Returns configuration object of the class.
     * if configuration doesn't exist in deployment.yaml, returns object with default values.
     *
     * @param configClass configuration bean class
     * @param <T>         object type
     * @return configuration bean object of given type
     * @throws ConfigurationException if there is a problem with config object instantiation.
     */
    <T extends Object> T getConfigurationObject(Class<T> configClass) throws ConfigurationException;

    /**
     * Returns configuration object of the namespace.
     * Configuration object can be either List or Map, it depends on configuration of the namespace.
     *
     * @param namespace config namespace
     * @return configuration object
     * @throws ConfigurationException if there is a problem while reading the configurations
     */
    Object getConfigurationObject(String namespace) throws ConfigurationException;

    /**
     * Returns configuration object of the class.
     * <p>
     * If configuration namespace is given, reads the configuration of the namespace and create instance of the the
     * class from that configurations.
     * <p>
     * If namespace is null or configuration doesn't exist for in configuration file, returns configurations based on
     * the bean class.
     *
     * @param namespace config namespace
     * @param configClass configuration bean class
     * @param <T>        object type
     * @return configuration object
     * @throws ConfigurationException if there is a problem while reading the configurations
     */
    <T> T getConfigurationObject(String namespace, Class<T> configClass) throws ConfigurationException;

    /**
     * Returns te configuration object list of the class under the namespace
     *
     * @param namespace config namespace
     * @param configClass configuration bean class
     * @param <T>        object type
     * @return list of configuration object
     * @throws ConfigurationException if there is a problem while reading the configurations
     */
    <T> ArrayList<T> getConfigurationObjectList(String namespace, Class<T> configClass) throws ConfigurationException;
}
