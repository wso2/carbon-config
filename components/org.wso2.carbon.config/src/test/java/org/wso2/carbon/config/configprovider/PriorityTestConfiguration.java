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
package org.wso2.carbon.config.configprovider;

import org.wso2.carbon.config.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Sample configuration class for testing purposes.
 *
 * @since 2.0.6
 */
@XmlRootElement
@Configuration(description = "Priority Test Bean")
class PriorityTestBean {

    private String name = "default";
    private int id = 0;
    private int port = 8000;

    public int getId() {
        return id;
    }

    @XmlElement
    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    @XmlElement
    public void setName(String name) {
        this.name = name;
    }

    public int getPort() {
        return port;
    }

    @XmlElement
    public void setPort(int port) {
        this.port = port;
    }
}

/**
 * Sample configuration class for testing purposes.
 *
 * @since 2.0.6
 */
@XmlRootElement
@Configuration(description = "Test Beans")
class TestBeans {

    private List<PriorityTestBean> testBeanList;

    TestBeans() {
        testBeanList = new ArrayList<>();
        testBeanList.add(new PriorityTestBean());
    }

    public List<PriorityTestBean> getTestBeanList() {
        return testBeanList;
    }

    @XmlElement
    public void setTestBeanList(List<PriorityTestBean> testBeans) {
        this.testBeanList = testBeans;
    }

}

/**
 * Sample configuration class for testing purposes.
 *
 * @since 2.0.6
 */
@XmlRootElement(name = "prioritytestconfiguration")
@Configuration(namespace = "prioritytestconfiguration", description = "Priority Test Configurations Bean")
public class PriorityTestConfiguration {

    private TestBeans testBeans = new TestBeans();

    public TestBeans getTestBeans() {
        return testBeans;
    }

    @XmlElement
    public void setTestBeans(TestBeans priorityTestBeans) {
        this.testBeans = priorityTestBeans;
    }
}
