# Carbon Configuration Sample

## Configuration annotations

@Configuration annotation is used to define the annotated class is a
configuration POJO.

```java
@Configuration(namespace = "demo.configuration", description = "This is a demo configuration")
public class DemoConfiguration {
```

`namespace` property is used to define a configuration section in the
generated configuration.

@Element annotation defines an element in the configuration file. 
However, even if this is not specified, all the private variables are
by default considered as elements in the configuration file.

@ignore annotation specifically ignores the annotated variable being
written to the prepared configuration file

## Example

**Sample configuration POJO**

```java
@Configuration(namespace = "demo.configuration", description = "This is a demo configuration")
public class DemoConfiguration {

    @Element(description = "Property with element tag")
    private String propertyWithElement = "Property 1";

    @Ignore
    private String ignoreProperty = "Property 2";

    private String propertyWithoutElement = "Property 3";

    @Element(description = "Example required property", required = true)
    private String requiredProperty = "Property 4";

    public String getPropertyWithElement() {
        return propertyWithElement;
    }

    public String getIgnoreProperty() {
        return ignoreProperty;
    }

    public String getPropertyWithoutElement() {
        return propertyWithoutElement;
    }

    public String getRequiredProperty() {
        return requiredProperty;
    }
}
```

**Result of the above configuration POJO class**

```yaml
#   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved
#
#   Licensed under the Apache License, Version 2.0 (the \"License\");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an \"AS IS\" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.

  # This is a demo configuration
demo.configuration:
    # Property with element tag
  propertyWithElement: Property 1
  propertyWithoutElement: Property 3
    # Example required property
    # THIS IS A MANDATORY FIELD
  requiredProperty: Property 4
```

## Building the sample

Navigate to [Configuration sample](/) and execute the command below:

```bash
mvn clean install
```

## pom configuration

Add the below mentioned code segment to the \<build>/\<plugins> section
of the pom. This will location all the POJO classes with @Configuration
annotation to build the configuration file.

```xml
            <plugin>
                <groupId>org.wso2.carbon.config</groupId>
                <artifactId>org.wso2.carbon.config.maven.plugin</artifactId>
                <version>${project.version}</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>create-doc</goal>
                        </goals>
                        <phase>compile</phase>
                    </execution>
                </executions>
            </plugin>
```

If you want to create the configuration files for a specified POJO
classes specify the `<configclasses>` inside the `<configuration>` 
element within the maven config plugin

```xml
            <plugin>
                <groupId>org.wso2.carbon.config</groupId>
                <artifactId>org.wso2.carbon.config.maven.plugin</artifactId>
                <version>${project.version}</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>create-doc</goal>
                        </goals>
                        <phase>compile</phase>
                    </execution>
                </executions>
                <!-- Specifying the below configuration will create the configuration only
                for the CarbonConfiguration class -->
                <configuration>
                    <configclasses>
                        <configclass>
                            org.wso2.carbon.config.samples.configuration.carbon.CarbonConfiguration
                        </configclass>
                    </configclasses>
                </configuration>
            </plugin>
```