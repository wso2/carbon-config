package org.wso2.carbon.config.maven.plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * This class will define the structure of the object model
 * which is passed as the context to the handlebars template
 * 'main.hbs' to generate the content displayed in the
 * HTML files.
 *
 * @since 1.0.0
 */
public class DataModel {
    private String elementName = null;
    private String dataType = null;
    private String description = null;
    private String defaultValue = null;
    private String required = null;
    private String possibleValues = null;
    public List<DataModel> childElements = new ArrayList<>();

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getRequired() {
        return required;
    }

    public void setRequired(String required) {
        this.required = required;
    }

    public String getPossibleValues() {
        return possibleValues;
    }

    public void setPossibleValues(String possibleValues) {
        this.possibleValues = possibleValues;
    }

    public List<DataModel> getChildElements() {
        return childElements;
    }

    public void setChildElements(List<DataModel> childElements) {
        this.childElements = childElements;
    }
}
