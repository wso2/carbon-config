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
@Configuration(description = "Unique Element Test Bean")
class UniqueElementTestBean {

    private String uniqueElement = "default";
    private int port = 8000;

    public String getUniqueElement() {
        return uniqueElement;
    }

    @XmlElement
    public void setUniqueElement(String uniqueElement) {
        this.uniqueElement = uniqueElement;
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
@Configuration(description = "Unique Element Test Beans")
class UniqueElementTestBeans {

    private List<UniqueElementTestBean> uniqueElementTestBeanList;

    UniqueElementTestBeans() {
        uniqueElementTestBeanList = new ArrayList<>();
        uniqueElementTestBeanList.add(new UniqueElementTestBean());
    }

    public List<UniqueElementTestBean> getUniqueElementTestBeanList() {
        return uniqueElementTestBeanList;
    }

    @XmlElement
    public void setUniqueElementTestBeanList(List<UniqueElementTestBean> uniqueElementTestBeanList) {
        this.uniqueElementTestBeanList = uniqueElementTestBeanList;
    }

}

/**
 * Sample configuration class for testing purposes.
 *
 * @since 2.0.6
 */
@XmlRootElement(name = "uniqueelementtestconfiguration")
@Configuration(namespace = "uniqueelementtestconfiguration", description = "Unique Element Test Configurations Bean")
public class UniqueElementTestConfiguration {

    private UniqueElementTestBeans uniqueElementTestBeans = new UniqueElementTestBeans();

    public UniqueElementTestBeans getUniqueElementTestBeans() {
        return uniqueElementTestBeans;
    }

    @XmlElement
    public void setUniqueElementTestBeans(UniqueElementTestBeans uniqueElementTestBeans) {
        this.uniqueElementTestBeans = uniqueElementTestBeans;
    }
}
