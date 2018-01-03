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

import com.google.gson.Gson;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**
 * This class will create zip archive combining all configuration documents coming with features.
 * This is applied when creating product distribution. plugin will collect config-docs from the features in p2-repo
 * and generate zip archive.
 *
 * @since 2.0.6
 */
@Mojo(name = "collect-docs")
public class ProductConfigurationMojo extends AbstractMojo {
    private static final Logger logger = LoggerFactory.getLogger(ProductConfigurationMojo.class.getName());
    private static final String P2_REPO_DIR = "p2-repo";
    private static final String FEATURES_DIR = "features";
    private static final String PLUGINS_DIR = "plugins";
    private static final String CONFIG_DOCS_DIR = "config-docs/";
    private static final String JSON_FILE_EXTENSION = ".json";
    private static final String INDEX_FILE = "index.html";
    private static final String UTF_8_CHARSET = "UTF-8";
    private File outputDirectory;

    @Parameter(defaultValue = "${project}", required = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        outputDirectory = new File(project.getBuild().getOutputDirectory());
        Path featurePath = Paths.get(outputDirectory.getParent(), P2_REPO_DIR, FEATURES_DIR);
        Path pluginPath = Paths.get(outputDirectory.getParent(), P2_REPO_DIR, PLUGINS_DIR);
        File[] featuresList = featurePath.toFile().listFiles();
        File[] pluginsList = pluginPath.toFile().listFiles();
        if (featuresList == null) {
            return;
        }

        if (pluginsList == null) {
            return;
        }

        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        for (File file : featuresList) {
            try (JarFile jar = new JarFile(file)) {
                JarEntry configDocs = jar.getJarEntry(CONFIG_DOCS_DIR);
                if (configDocs != null) {
                    Enumeration enumEntries = jar.entries();
                    while (enumEntries.hasMoreElements()) {
                        JarEntry jarEntry = (JarEntry) enumEntries.nextElement();
                        if (!jarEntry.getName().startsWith("config-docs/")) {
                            continue;
                        }
                        File destFile = new File(outputDirectory.getParent(), jarEntry.getName());
                        if (jarEntry.isDirectory()) {
                            if (!destFile.exists() && !destFile.mkdirs()) {
                                throw new MojoExecutionException("Error while creating config directory in classpath");
                            }
                            continue;
                        }
                        inputStream = jar.getInputStream(jarEntry);
                        outputStream = new FileOutputStream(destFile);
                        while (inputStream.available() > 0) {
                            outputStream.write(inputStream.read());
                        }
                    }
                }
            } catch (IOException e) {
                throw new MojoExecutionException("Error while opening feature jarfile", e);
            } finally {
                try {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    logger.error("Error while closing the input stream", e);
                }

            }
        }

        for (File file : pluginsList) {
            try (JarFile jar = new JarFile(file)) {
                JarEntry configDocs = jar.getJarEntry(CONFIG_DOCS_DIR);
                if (configDocs != null) {
                    Enumeration enumEntries = jar.entries();
                    while (enumEntries.hasMoreElements()) {
                        JarEntry jarEntry = (JarEntry) enumEntries.nextElement();
                        if (!jarEntry.getName().startsWith("config-docs/")) {
                            continue;
                        }
                        File destFile = new File(outputDirectory.getParent(), jarEntry.getName());
                        if (jarEntry.isDirectory()) {
                            if (!destFile.exists() && !destFile.mkdirs()) {
                                throw new MojoExecutionException("Error while creating config directory in classpath");
                            }
                            continue;
                        }
                        inputStream = jar.getInputStream(jarEntry);
                        outputStream = new FileOutputStream(destFile);
                        while (inputStream.available() > 0) {
                            outputStream.write(inputStream.read());
                        }
                    }
                }
            } catch (IOException e) {
                throw new MojoExecutionException("Error while opening feature jarfile", e);
            } finally {
                try {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    logger.error("Error while closing the input stream", e);
                }

            }
        }

        try {
            generateMenuFile(generateMenuList(new File(outputDirectory.getParent(), CONFIG_DOCS_DIR)));
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            throw new MojoExecutionException("Error while executing the method", e);
        }

        copyIndex(readIndex());

    }

    /**
     * generating the JSON files and
     * write them to the class output directory
     *
     * @throws MojoExecutionException, IOException
     */
    private List<MenuList> generateMenuList(File directory) throws MojoExecutionException,
            FileNotFoundException, UnsupportedEncodingException {
        File dir = new File(directory.getPath());
        File[] directoryListing = dir.listFiles();
        Gson gson = new Gson();
        List<MenuList> menuLists = new ArrayList<>();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                if (child.getName().endsWith(JSON_FILE_EXTENSION)) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader
                            (new FileInputStream(child.getPath()), "UTF-8"));
                    DataContext dataContext = gson.fromJson(bufferedReader, DataContext.class);
                    MenuList menuList = new MenuList();
                    menuList.setFilename(child.getName());
                    menuList.setDisplayName(dataContext.getDisplayName());
                    menuLists.add(menuList);
                }
            }
        }

        return menuLists;
    }

    private void generateMenuFile(List<MenuList> menuLists) throws MojoExecutionException {
        Gson gson = new Gson();
        MenuContext menuContext = new MenuContext();
        menuContext.setMenuLists(menuLists);
        Object json = gson.toJson(menuContext);

        File configDir = new File(outputDirectory.getParent(), CONFIG_DOCS_DIR);
        // create config directory inside project output directory to save config files
        if (!configDir.exists() && !configDir.mkdirs()) {
            throw new MojoExecutionException("Error while creating config directory in classpath");
        }

        try (PrintWriter out = new PrintWriter(new File(configDir.getPath(), "menu"
                + JSON_FILE_EXTENSION), UTF_8_CHARSET)) {
            out.println(json);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            throw new MojoExecutionException("Error while creating new resource file from the classpath", e);
        }

        // add configuration document to the project resources under config-docs/ directory.
        Resource resource = new Resource();
        resource.setDirectory(configDir.getAbsolutePath());
        resource.setTargetPath(CONFIG_DOCS_DIR);
        project.addResource(resource);

    }

    /**
     * read handlebars.js file in the resource folder.
     *
     * @throws MojoExecutionException
     */
    private String readIndex() throws MojoExecutionException {
        InputStream inputStream = ConfigDocumentMojo.class.getClassLoader().getResourceAsStream(INDEX_FILE);
        StringBuilder sb = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, UTF_8_CHARSET))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            logger.error("Error while reading the index file.", e);
        }

        return sb.toString();

    }

    /**
     * copy handlebars.js file in the resource folder.
     *
     * @throws MojoExecutionException
     */
    private void copyIndex(String js) throws MojoExecutionException {

        File configDir = new File(outputDirectory.getParent(), CONFIG_DOCS_DIR);
        // create config directory inside project output directory to save config files
        if (!configDir.exists() && !configDir.mkdirs()) {
            throw new MojoExecutionException("Error while creating config directory in classpath");
        }

        try (PrintWriter out = new PrintWriter(new File(configDir.getPath(), INDEX_FILE), UTF_8_CHARSET)) {
            out.println(js);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            throw new MojoExecutionException("Error while creating new resource file from the classpath", e);
        }

        // add configuration document to the project resources under config-docs/ directory.
        Resource resource = new Resource();
        resource.setDirectory(configDir.getAbsolutePath());
        resource.setTargetPath(CONFIG_DOCS_DIR);
        project.addResource(resource);
    }
}
