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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
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
    private static final String CONFIG_DOCS_DIR = "config-docs/";

    @Parameter(defaultValue = "${project}", required = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        File outputDirectory = new File(project.getBuild().getOutputDirectory());
        Path featurePath = Paths.get(outputDirectory.getParent(), P2_REPO_DIR, FEATURES_DIR);
        File[] featuresList = featurePath.toFile().listFiles();
        if (featuresList == null) {
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
                        File destFile = new File(outputDirectory.getParent() , jarEntry.getName());
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
    }
}
