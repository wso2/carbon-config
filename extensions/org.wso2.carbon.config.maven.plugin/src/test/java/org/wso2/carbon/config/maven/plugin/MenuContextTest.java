package org.wso2.carbon.config.maven.plugin;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;


public class MenuContextTest {
    private MenuContext menuContext = new MenuContext();
    private MenuList menuList = new MenuList();

    @BeforeMethod
    public void setUp() throws Exception {
    }

    @AfterMethod
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetMenuLists() throws Exception {
        List<MenuList> expectedmenulistsTrue = new ArrayList<>();
        menuList.setDisplayName(null);
        menuList.setFilename(null);
        expectedmenulistsTrue.add(menuList);
        assertNotEquals(menuContext.getMenuLists(),
                expectedmenulistsTrue.toArray(new MenuList[expectedmenulistsTrue.size()]),
                "Valid getMenuLists().");
    }

    @Test
    public void testSetMenuLists() throws Exception {
        List<MenuList> expectedmenulistsTrue = new ArrayList<>();
        menuList.setDisplayName(null);
        menuList.setFilename(null);
        expectedmenulistsTrue.add(menuList);
        menuContext.setMenuLists(expectedmenulistsTrue);
        assertEquals(menuContext.getMenuLists(), expectedmenulistsTrue,
                "Valid Menu Lists");
    }

}
