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
package org.wso2.carbon.config.maven.plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * This class will define the structure of the object model
 * which is passed as the context to the handlebars template
 * 'index.html' to generate the content displayed in the
 * generated output.
 *
 * @since 2.1.6
 */
public class DataContext {
    private String displayName = null;
    private String yamlString = null;
    private List<DataModel> elements = new ArrayList<>();

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getYamlString() {
        return yamlString;
    }

    public void setYamlString(String yamlString) {
        this.yamlString = yamlString;
    }

    public List<DataModel> getElements() {
        return elements;
    }

    public void setElements(List<DataModel> elements) {
        this.elements = elements;
    }
}
