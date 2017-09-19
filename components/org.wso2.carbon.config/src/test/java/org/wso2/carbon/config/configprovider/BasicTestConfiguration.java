/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.config.configprovider;

import org.wso2.carbon.config.annotation.Configuration;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Sample configuration class for testing purposes.
 *
 * @since 2.0.6
 */
@XmlRootElement
@Configuration(description = "Test Bean")
class TestBean {

    private String name = "default";

    public String getName() {
        return name;
    }

    @XmlElement
    public void setName(String name) {
        this.name = name;
    }
}

/**
 * Sample configuration class for testing purposes.
 *
 * @since 2.0.6
 */
@XmlRootElement
@Configuration(description = "Complex Test Bean")
class ComplexTestBean {

    private String name = "default";
    private TestBean testBean = new TestBean();

    public String getName() {
        return name;
    }

    @XmlElement
    public void setName(String name) {
        this.name = name;
    }

    public TestBean getTestBean() {
        return testBean;
    }

    @XmlElement
    public void setTestBean(TestBean testBean) {
        this.testBean = testBean;
    }
}

/**
 * Sample configuration class for testing purposes.
 *
 * @since 2.0.6
 */
@XmlRootElement(name = "basictestconfiguration")
@Configuration(namespace = "basictestconfiguration", description = "Test Configurations Bean")
public class BasicTestConfiguration {

    private TestBean testBean = new TestBean();
    private ComplexTestBean complexTestBean = new ComplexTestBean();

    public TestBean getTestBean() {
        return testBean;
    }

    @XmlElement
    public void setTestBean(TestBean testBean) {
        this.testBean = testBean;
    }

    public ComplexTestBean getComplexTestBean() {
        return complexTestBean;
    }

    @XmlElement
    public void setComplexTestBean(ComplexTestBean complexTestBean) {
        this.complexTestBean = complexTestBean;
    }
}
