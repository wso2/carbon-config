/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.config.configprovider;

import org.wso2.carbon.config.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Sample configuration class for testing purposes.
 *
 * @since 1.0.0
 */
@XmlRootElement
@Configuration(namespace = "testTransports", description = "Test Transports Bean")
class TestTransportRoot {

    public List<TestTransportElement> testTransports;

    TestTransportRoot() {
        testTransports = new ArrayList<>();
        testTransports.add(new TestTransportElement());
    }

    @XmlElement
    public void setTestTransports(List<TestTransportElement> transportsTest) {
        this.testTransports = transportsTest;
    }

    public List<TestTransportElement> getTestTransports() {
        return testTransports;
    }
}

@XmlRootElement
@Configuration(description = "Test Transport Bean")
class TestTransportElement {

    public TestTransport testTransport = new TestTransport();

    public TestTransport getTestTransport() {
        return testTransport;
    }

    public void setTestTransport(TestTransport testTransport) {
        this.testTransport = testTransport;
    }
}

@XmlRootElement
@Configuration(description = "Test Transport Bean")
class TestTransport {

    public String name = "default transport";
    public int port = 8000;
    public String secure = "false";
    public String desc = "Default Transport Configurations";
    public String password = "zzz";

    @XmlAttribute
    public void setSecure(String secure) {
        this.secure = secure;
    }

    public String isSecure() {
        return secure;
    }

    @XmlElement
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @XmlElement
    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    @XmlElement
    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    @XmlElement
    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }
}

