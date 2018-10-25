/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.config.configprovider;

import org.wso2.carbon.config.annotation.Configuration;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Sample configuration class for testing purposes.
 *
 * @since 1.0.0
 */
@XmlRootElement(name = "testconfiguration")
@Configuration(namespace = "wso2.test.config", description = "Test Configurations Bean")
public class ComplexNameSpaceConfiguration {

    private String tenant = "default";
    private Transports transports = new Transports();

    @XmlElement
    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getTenant() {
        return tenant;
    }

    @XmlElement
    public void setTransports(Transports transports) {
        this.transports = transports;
    }

    public Transports getTransports() {
        return transports;
    }
}
