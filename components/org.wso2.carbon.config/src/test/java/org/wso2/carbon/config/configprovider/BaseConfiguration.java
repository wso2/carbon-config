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

/**
 * Sample configuration class for testing purposes.
 * This is to test generating configuration for beans without namespace.
 *
 * @since 2.1.3
 */
public class BaseConfiguration {
    private String name = "test";
    private BaseTestBean testBean = new BaseTestBean();

    public String getName() {
        return name;
    }

    public BaseTestBean getTestBean() {
        return testBean;
    }
}

/**
 * Sample configuration class for testing purposes.
 */
class BaseTestBean {
    private int id = 20;

    public int getId() {
        return id;
    }
}
