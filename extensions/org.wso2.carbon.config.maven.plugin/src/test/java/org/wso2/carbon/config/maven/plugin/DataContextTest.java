package org.wso2.carbon.config.maven.plugin;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;

public class DataContextTest {
    private DataContext dataContext = new DataContext();
    private DataModel dataModel = new DataModel();
    @BeforeMethod
    public void setUp() throws Exception {
    }

    @AfterMethod
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetDisplayName() throws Exception {
        assertEquals(dataContext.getDisplayName(),
                null, "Valid getDisplayName().");
        assertNotEquals(dataContext.getDisplayName(),
                "displayName", "Invalid getDisplayName().");
        assertFalse(Boolean.parseBoolean(dataContext.getDisplayName()),
                "Invalid getDisplayName().");
    }

    @Test
    public void testSetDisplayName() throws Exception {
        dataContext.setDisplayName("Carbon Configurations");
        assertEquals(dataContext.getDisplayName(),
                "Carbon Configurations", "Valid Display Name");
    }

    @Test
    public void testGetYamlString() throws Exception {
        assertEquals(dataContext.getYamlString(),
                null, "Valid getYamlString().");
        assertNotEquals(dataContext.getYamlString(),
                "yamlString", "Invalid getYamlString().");
        assertFalse(Boolean.parseBoolean(dataContext.getYamlString()),
                "Invalid getYamlString().");
    }

    @Test
    public void testSetYamlString() throws Exception {
        dataContext.setYamlString("wso2.configuration:\\n  name: WSO2\\n  value: 10\\n  childConfiguration:\\n    " +
                "isEnabled: false\\n    destination: destination-name\\n");
        assertEquals(dataContext.getYamlString(),
                "wso2.configuration:\\n  name: WSO2\\n  value: 10\\n  childConfiguration:\\n    " +
                        "isEnabled: false\\n    destination: destination-name\\n", "Valid Yaml String");
    }

    @Test
    public void testGetElements() throws Exception {
        List<DataModel> expectedelementsTrue = new ArrayList<>();
        dataModel.setElementName(null);
        dataModel.setDataType(null);
        dataModel.setDescription(null);
        dataModel.setDefaultValue(null);
        dataModel.setRequired(null);
        dataModel.setRequired(null);
        dataModel.setChildElements(expectedelementsTrue);
        expectedelementsTrue.add(dataModel);
        assertNotEquals(dataContext.getElements(),
                expectedelementsTrue.toArray(new DataModel[expectedelementsTrue.size()]),
                "Valid getElements().");
    }


    @Test
    public void testSetElements() throws Exception {
        List<DataModel> expectedelementsTrue = new ArrayList<>();
        dataModel.setElementName(null);
        dataModel.setDataType(null);
        dataModel.setDescription(null);
        dataModel.setDefaultValue(null);
        dataModel.setRequired(null);
        dataModel.setRequired(null);
        dataModel.setChildElements(expectedelementsTrue);
        expectedelementsTrue.add(dataModel);
        dataContext.setElements(expectedelementsTrue);
        assertEquals(dataContext.getElements(), expectedelementsTrue,
                "Valid Elements");
    }

}
