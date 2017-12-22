package org.wso2.carbon.config.maven.plugin;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;

public class MenuListTest {
    private MenuList menuList = new MenuList();
    @BeforeMethod
    public void setUp() throws Exception {
    }

    @AfterMethod
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetFilename() throws Exception {
        assertEquals(menuList.getFilename(),
                null, "Valid getFilename().");
        assertNotEquals(menuList.getFilename(),
                "fileName", "Invalid getFilename().");
        assertFalse(Boolean.parseBoolean(menuList.getFilename()),
                "Invalid getFilename().");
    }

    @Test
    public void testSetFilename() throws Exception {
        menuList.setFilename("wso2.configuration.json");
        assertEquals(menuList.getFilename(),
                "wso2.configuration.json", "Valid File Name");
    }

    @Test
    public void testGetDisplayName() throws Exception {
        assertEquals(menuList.getDisplayName(),
                null, "Valid getDisplayName().");
        assertNotEquals(menuList.getDisplayName(),
                "displayName", "Invalid getDisplayName().");
        assertFalse(Boolean.parseBoolean(menuList.getDisplayName()),
                "Invalid getDisplayName().");
    }

    @Test
    public void testSetDisplayName() throws Exception {
        menuList.setDisplayName("Carbon Configurations");
        assertEquals(menuList.getDisplayName(),
                "Carbon Configurations", "Valid Possible Values");
    }

}
