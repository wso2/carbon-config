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
package org.wso2.carbon.config.samples.bundle;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;
import org.wso2.carbon.config.annotation.Ignore;

import java.util.Locale;

/**
 * Parent configuration class
 *
 * @since 1.0.0
 */
@Configuration(namespace = "wso2.configuration", description = "Parent configuration")
public class ParentConfiguration {

    @Element(description = "An example element for this configuration")
    private String name = "WSO2";

    @Element(description = "Another example element in the config", required = true)
    private int value = 10;

    // This value will not be visible in the configuration
    @Ignore
    private String ignored = "Ignored String";

    @Element(description = "Second level configuration")
    private ChildConfiguration childConfiguration = new ChildConfiguration();

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    public String getIgnored() {
        return ignored;
    }

    public ChildConfiguration getChildConfiguration() {
        return childConfiguration;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "name : %s, value : %s, childConfiguration - %s",
                name, value, childConfiguration);
    }
}
