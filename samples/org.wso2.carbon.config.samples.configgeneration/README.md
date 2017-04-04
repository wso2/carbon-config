# Carbon Configuration Sample

## Configuration annotations

@Configuration annotation is used to define that the annotated class is a
configuration.

```java
@Configuration(namespace = "demo.configuration", description = "This is a demo configuration")
public class DemoConfiguration {
    
}
```

`namespace` property is used to define a configuration section in the
generated configuration.

@Element annotation defines an element in the configuration file. 
However, even if this is not specified, all the private variables are
by default considered as elements in the configuration.

@ignore annotation specifically ignores the annotated variable being
written to the prepared configuration file.

## Example 1

###Dependencies

Add the following dependencies to the relevant pom file.

```xml
        <dependency>
            <groupId>org.wso2.carbon.config</groupId>
            <artifactId>org.wso2.carbon.config</artifactId>
            <version>${carbon.config.version}</version>
        </dependency>
```

This is the only dependency required when you want to create 
configuration POJO classes. This dependency will provide you with
the configuration annotations and is also required by the 
`org.wso2.carbon.config.maven.plugin` plugin when generating the
configuration files.

### Sample configuration POJO

```java
@Configuration(namespace = "demo.configuration", description = "This is a demo configuration")
public class DemoConfiguration {

    // String element
    @Element(description = "Property with element tag")
    private String propertyWithElement = "Property 1";

    // Integer element
    @Element(description = "Integer property")
    private int value = 20;

    // This value will not be visible in the configuration
    @Ignore
    private String ignoreProperty = "Property 2";

    // String element - event without @Element annotation private variables of the POJO class
    // will be taken as elements
    private String propertyWithoutElement = "Property 3";

    // Required element - when required is set to true, an additional comment
    // (# THIS IS A MANDATORY FIELD) will be written in the generated config stating that this
    // element is mandatory
    @Element(description = "Example required property", required = true)
    private String requiredProperty = "Property 4";

    public String getPropertyWithElement() {
        return propertyWithElement;
    }

    public int getValue() {
        return value;
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

### pom configuration

Add the below mentioned code segment to the \<build>/\<plugins> section
of the relevant pom. This will locate all the POJO classes **(within the pom's project only)** with @Configuration
annotation to build the configuration file.

```xml
            <plugin>
                <groupId>org.wso2.carbon.config</groupId>
                <artifactId>org.wso2.carbon.config.maven.plugin</artifactId>
                <version>${carbon.config.version}</version>
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

If you want to create the configuration files for specified POJO
classes specify the `<configclasses>` inside the `<configuration>` 
element within the maven config plugin

```xml
            <plugin>
                <groupId>org.wso2.carbon.config</groupId>
                <artifactId>org.wso2.carbon.config.maven.plugin</artifactId>
                <version>${carbon.config.version}</version>
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
                            org.wso2.carbon.config.samples.configgeneration.DemoConfiguration
                        </configclass>
                    </configclasses>
                </configuration>
            </plugin>
```

This will only generate the configuration file to `org.wso2.carbon.config.samples.configgeneration.DemoConfiguration`
class. You may specify multiple `configclass` elements inside the `configclasses` tag.

### Building the sample

Navigate to [Configuration sample](/) and execute the command below:

```bash
mvn clean install
```

### Result of the above configuration POJO class

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
    # Integer property
  value: 20
  propertyWithoutElement: Property 3
    # Example required property
    # THIS IS A MANDATORY FIELD
  requiredProperty: Property 4
```

## Example 2

### Dependencies

```xml
        <dependency>
            <groupId>org.wso2.carbon.config</groupId>
            <artifactId>org.wso2.carbon.config</artifactId>
            <version>${carbon.config.version}</version>
        </dependency>
```

### Configuration POJO classes

#### Parent Configuration

```java
@Configuration(namespace = "org.wso2.configuration", description = "Parent configuration")
public class ParentConfiguration {

    @Element(description = "An example element for this configuration")
    private String name = "WSO2";

    @Element(description = "Another example element in the config", required = true)
    private int value = 10;

    // This value will not be visible in the configuration
    @Ignore
    private String ignored = "Ignored String";

    @Element(description = "Second level configuration")
    private ChildConfiguration childConfiguration = new ChildConfiguration();

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    public String getIgnored() {
        return ignored;
    }

    public ChildConfiguration getChildConfiguration() {
        return childConfiguration;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "name : %s, value : %s, childConfiguration - %s",
                name, value, childConfiguration);
    }
}
```

#### Child Configuration

```java
// In here do not specify the namespace since this configuration is a part of the
// ParentConfiguration. Specifying the namespace will break this configuration to a separate
// configuration under the section of the specified namespace
@Configuration(description = "Child configuration")
public class ChildConfiguration {

    @Element(description = "A boolean field")
    private boolean isEnabled = false;

    @Element(description = "A string field")
    private String destination = "destination-name";

    public boolean isEnabled() {
        return isEnabled;
    }

    public String getDestination() {
        return destination;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "destination : %s, isEnabled : %s",
                destination, isEnabled);
    }
}
```

**Note:** Since we already identified that the `ChildConfiguration`
is a part of the `ParentConfiguration`, do not specify the `namespace` property
in the `@Configuration` annotation of the `ChildConfiguration`. Specifying the 
`namespace` property in the `@Configuration` annotation of the `ChildConfiguration`
will break the `ChildConfiguration` to a new configuration section under the
specified `namespace`.

### pom configuration

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
                            org.wso2.carbon.config.samples.common.ParentConfiguration
                        </configclass>
                    </configclasses>
                </configuration>
            </plugin>
```

### Building the sample

Navigate to [Configuration sample](/) and execute the command below:

```bash
mvn clean install
```

### Resulting configuration document

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

  # Parent configuration
org.wso2.configuration:
    # An example element for this configuration
  name: WSO2
    # Another example element in the config
    # THIS IS A MANDATORY FIELD
  value: 10
    # Child configuration
  childConfiguration:
      # A boolean field
    isEnabled: false
      # A string field
    destination: destination-name
```