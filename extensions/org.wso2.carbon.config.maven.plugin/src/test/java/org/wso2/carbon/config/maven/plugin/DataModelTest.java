package org.wso2.carbon.config.maven.plugin;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;

public class DataModelTest {
    private DataModel dataModel = new DataModel();

    @BeforeMethod
    public void setUp() throws Exception {
    }

    @AfterMethod
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetElementName() throws Exception {
        assertEquals(dataModel.getElementName(),
                null, "Valid getElementName().");
        assertNotEquals(dataModel.getElementName(),
                "passwordkshfy", "Invalid getElementName().");
        assertFalse(Boolean.parseBoolean(dataModel.getElementName()),
                "Invalid getElementName().");

    }

    @Test
    public void testSetElementName() throws Exception {
        dataModel.setElementName("hostName");
        assertEquals(dataModel.getElementName(),
                "hostName", "Valid Element Name");
    }

    @Test
    public void testGetDataType() throws Exception {
        assertEquals(dataModel.getDataType(),
                null, "Valid getDataType().");
        assertNotEquals(dataModel.getDataType(),
                "dataType", "Invalid getDataType().");
        assertFalse(Boolean.parseBoolean(dataModel.getDataType()),
                "Invalid getDataType().");
    }

    @Test
    public void testSetDataType() throws Exception {
        dataModel.setDataType("String");
        assertEquals(dataModel.getDataType(),
                "String", "Valid Data Type");
    }

    @Test
    public void testGetDescription() throws Exception {
        assertEquals(dataModel.getDescription(),
                null, "Valid getDescription().");
        assertNotEquals(dataModel.getDescription(),
                "description", "Invalid getDescription().");
        assertFalse(Boolean.parseBoolean(dataModel.getDescription()),
                "Invalid getDescription().");
    }

    @Test
    public void testSetDescription() throws Exception {
        dataModel.setDescription("Name of the Host");
        assertEquals(dataModel.getDescription(),
                "Name of the Host", "Valid Description");
    }

    @Test
    public void testGetDefaultValue() throws Exception {
        assertEquals(dataModel.getDefaultValue(),
                null, "Valid getDefaultValue().");
        assertNotEquals(dataModel.getDefaultValue(),
                "defaultValue", "Invalid getDefaultValue().");
        assertFalse(Boolean.parseBoolean(dataModel.getDefaultValue()),
                "Invalid getDefaultValue().");
    }

    @Test
    public void testSetDefaultValue() throws Exception {
        dataModel.setDefaultValue("localhost");
        assertEquals(dataModel.getDefaultValue(),
                "localhost", "Valid Default Value");
    }

    @Test
    public void testGetRequired() throws Exception {
        assertEquals(dataModel.getRequired(),
                null, "Valid getRequired().");
        assertNotEquals(dataModel.getRequired(),
                "required", "Invalid getRequired().");
        assertFalse(Boolean.parseBoolean(dataModel.getRequired()),
                "Invalid getRequired().");
    }

    @Test
    public void testSetRequired() throws Exception {
        dataModel.setRequired("Required");
        assertEquals(dataModel.getRequired(),
                "Required", "Valid Required Field ");
    }

    @Test
    public void testGetPossibleValues() throws Exception {
        assertEquals(dataModel.getPossibleValues(),
                null, "Valid getPossibleValues().");
        assertNotEquals(dataModel.getPossibleValues(),
                "possibleValues", "Invalid getPossibleValues().");
        assertFalse(Boolean.parseBoolean(dataModel.getPossibleValues()),
                "Invalid getPossibleValues().");
    }

    @Test
    public void testSetPossibleValues() throws Exception {
        dataModel.setPossibleValues("localhost");
        assertEquals(dataModel.getPossibleValues(),
                "localhost", "Valid Possible Values");
    }

    @Test
    public void testGetChildElements() throws Exception {
        List<DataModel> expectedchildelementsTrue = new ArrayList<>();
        dataModel.setElementName(null);
        dataModel.setDataType(null);
        dataModel.setDescription(null);
        dataModel.setDefaultValue(null);
        dataModel.setRequired(null);
        dataModel.setPossibleValues(null);
        expectedchildelementsTrue.add(dataModel);
        assertNotEquals(dataModel.getChildElements(),
                expectedchildelementsTrue.toArray(new DataModel[expectedchildelementsTrue.size()]),
                "Valid getChildElements().");
    }

    @Test
    public void testSetChildElements() throws Exception {
        List<DataModel> expectedchildelementsTrue = new ArrayList<>();
        dataModel.setElementName(null);
        dataModel.setDataType(null);
        dataModel.setDescription(null);
        dataModel.setDefaultValue(null);
        dataModel.setRequired(null);
        dataModel.setPossibleValues(null);
        expectedchildelementsTrue.add(dataModel);
        dataModel.setChildElements(expectedchildelementsTrue);
        assertEquals(dataModel.getChildElements(), expectedchildelementsTrue,
                "Valid Child Elements");
    }

}
