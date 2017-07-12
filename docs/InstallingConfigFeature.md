# Installing Configuration Provider Feature in OSGi 

Configuration Provider feature is depends on **carbon-secvault** feature and **carbon-utils** feature. If we need to install 
configuration provider feature, referred P2 repository needs to have both carbon-secvault feature and carbon-utils 
feature. So when we are generating P2 repository, we need to add features as below.

````xml
    <plugin>
        <groupId>org.wso2.carbon.maven</groupId>
        <artifactId>carbon-feature-plugin</artifactId>
        <executions>
            <execution>
                <id>p2-repo-generation</id>
                <phase>package</phase>
                <goals>
                    <goal>generate-repo</goal>
                </goals>
                <configuration>
                    <targetRepository>file:${basedir}/target/p2-repo</targetRepository>
                    <features>
                        <feature>
                            <id>org.wso2.carbon.config.feature</id>
                            <version>${carbon.config.version}</version>
                        </feature>
                        <feature>
                            <id>org.wso2.carbon.secvault.feature</id>
                            <version>${carbon.securevault.version}</version>
                        </feature>
                        <feature>
                            <id>org.wso2.carbon.utils.feature</id>
                            <version>${carbon.utils.version}</version>
                        </feature>
                    </features>
                </configuration>
            </execution>
            ...
        </executions>
    </plugin>
````

