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
package org.wso2.carbon.config.samples.common;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

import java.util.Locale;

/**
 * Child configuration of the {@link ParentConfiguration}.
 * <p>
 * since 1.0.0
 */
// In here do not specify the namespace since this configuration is a part of the
// ParentConfiguration. Specifying the namespace will break this configuration to a separate
// configuration under the section of the specified namespace
@Configuration(description = "Child configuration")
public class ChildConfiguration {

    @Element(description = "A boolean field")
    private boolean isEnabled = false;

    @Element(description = "A string field")
    private String destination = "destination-name";

    public boolean isEnabled() {
        return isEnabled;
    }

    public String getDestination() {
        return destination;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "destination : %s, isEnabled : %s",
                destination, isEnabled);
    }
}
