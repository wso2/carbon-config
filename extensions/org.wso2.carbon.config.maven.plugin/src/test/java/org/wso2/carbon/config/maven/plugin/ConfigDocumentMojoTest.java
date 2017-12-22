package org.wso2.carbon.config.maven.plugin;

import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockObjectFactory;
import org.testng.IObjectFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.config.ConfigConstants;

import java.io.File;
import java.io.FileNotFoundException;
/*import java.util.Arrays;*/
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import static org.powermock.api.mockito.PowerMockito.when;
import static org.wso2.carbon.config.ConfigConstants.TEMP_CONFIG_FILE_NAME;


@PrepareForTest({MavenProject.class, Build.class, ConfigConstants.class})
public class ConfigDocumentMojoTest {

    ConfigDocumentMojo configDocumentMojo = new ConfigDocumentMojo();

    private static final String TEMP_CONFIG_FILE_NAME = "temp_config_classnames.txt";
    private static final String PLUGIN_DESCRIPTOR_KEY = "pluginDescriptor";

    @Mock
    private PluginDescriptor mockedPluginDescriptor;

    @Mock
    private AbstractMojo mockedAbstractMojo;

    @Mock
    private ClassRealm mockedClassRealm;

   /* @Mock
    private Class mockedClass;*/


    @BeforeMethod
    public void setUp() throws Exception {
    }


    @AfterMethod
    public void tearDown() throws Exception {
    }

    @Test
    public void testExecute() throws Exception {

        String[] classList = null;

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(TEMP_CONFIG_FILE_NAME).getFile());

        try (Scanner scanner = new Scanner(file)) {
            String content = scanner.useDelimiter("\\Z").next();
            classList = content.split(",");
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException("Error while reading the configuration classes file", e);
        }

        Map<String, Object> finalMap = new LinkedHashMap<>();
        finalMap.put(PLUGIN_DESCRIPTOR_KEY, PLUGIN_DESCRIPTOR_KEY);

        when(mockedAbstractMojo.getPluginContext().get(PLUGIN_DESCRIPTOR_KEY)).thenReturn(finalMap);
        when(mockedPluginDescriptor.getClassRealm()).thenReturn(mockedClassRealm);


    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new PowerMockObjectFactory();
    }


}
