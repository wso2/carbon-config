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

/**
 * Configuration Constants.
 *
 * @since 1.0.0
 */
public final class ConfigConstants {

    public static final String NULL = "NULL";
    public static final String TEMP_CONFIG_FILE_NAME = "temp_config_classnames.txt";
    public static final String CONFIG_DIR = "config-docs";
    public static final String DEPLOYMENT_CONFIG_YAML = "deployment.yaml";

    /**
     * Maven project properties.
     */
    public static final String PROJECT_DEFAULTS_PROPERTY_FILE = "project.defaults.properties";

    /**
     * Default value if it is not set in sys prop/env.
     */
    public static class PlaceHolders {
        public static final String SERVER_KEY = "carbon-kernel";
        public static final String SERVER_NAME = "WSO2 Carbon Kernel";
        public static final String SERVER_VERSION = "5";

        private PlaceHolders() {
        }
    }

    private ConfigConstants() {
    }
}
