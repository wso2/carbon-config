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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.config.samples.configuration.demo;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;
import org.wso2.carbon.config.annotation.Ignore;

/**
 * Demo configuration class demonstrating the basic use of carbon configuration.
 *
 * @since 1.0.0
 */
@Configuration(namespace = "demo.configuration", description = "This is a demo configuration")
public class DemoConfiguration {

    @Element(description = "Property with element tag")
    private String propertyWithElement = "Property 1";

    @Ignore
    private String ignoreProperty = "Property 2";

    private String propertyWithoutElement = "Property 3";

    @Element(description = "Example required property", required = true)
    private String requiredProperty = "Property 4";

    public String getPropertyWithElement() {
        return propertyWithElement;
    }

    public String getIgnoreProperty() {
        return ignoreProperty;
    }

    public String getPropertyWithoutElement() {
        return propertyWithoutElement;
    }

    public String getRequiredProperty() {
        return requiredProperty;
    }
}
