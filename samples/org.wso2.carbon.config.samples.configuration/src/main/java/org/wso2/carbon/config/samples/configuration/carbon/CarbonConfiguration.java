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
package org.wso2.carbon.config.samples.configuration.carbon;

import org.wso2.carbon.config.ConfigurationUtils;
import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;
import org.wso2.carbon.config.annotation.Ignore;
import org.wso2.carbon.utils.Constants;

import java.util.Properties;

/**
 * CarbonConfiguration class holds static configuration parameters.
 *
 * @since 1.0.0
 */
// Namespace defines the configuration section
@Configuration(namespace = "wso2.carbon", description = "Carbon Configuration Parameters")
public class CarbonConfiguration {

    public CarbonConfiguration() {
        Properties properties = ConfigurationUtils.loadProjectProperties();
        version = properties.getProperty(Constants.MAVEN_PROJECT_VERSION);
    }

    // This property will be in configuration file with the description
    // Furthermore "# THIS IS A MANDATORY FIELD" comment will be added (because required = true)
    @Element(description = "value to uniquely identify a server", required = true)
    private String id = "carbon-kernel";

    @Element(description = "server name")
    private String name = "WSO2 Carbon Kernel";

    // This property will not be in the configuration file
    @Ignore
    private String version;

    // This property will be in the configuration file, but without a description
    private String tenant = Constants.DEFAULT_TENANT;

    @Element(description = "ports used by this server")
    private PortsConfig ports = new PortsConfig();

    @Element(description = "StartupOrderResolver related configurations")
    private StartupResolverConfig startupResolver = new StartupResolverConfig();

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getTenant() {
        return tenant;
    }

    public PortsConfig getPorts() {
        return ports;
    }

    public StartupResolverConfig getStartupResolver() {
        return startupResolver;
    }
}
