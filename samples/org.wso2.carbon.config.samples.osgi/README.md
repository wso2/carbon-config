# Carbon Configuration - OSGi Sample

### Building the configuration

Navigate to [Config Samples Common](../org.wso2.carbon.config.samples.common)
and [Config OSGi Sample](/) locations respectively and execute the command
below:

```bash
mvn clean install
```

### Running the sample

* Download Carbon Kernel 5.2.0 from [http://wso2.com/products/carbon/](http://wso2.com/products/carbon/)
* Copy org.wso2.carbon.config.samples.common-{version}.jar in [Config Sample Common](../org.wso2.carbon.config.samples.common/target/)
and org.wso2.carbon.config.samples.osgi-{version}.jar in [Config OSGi Sample](/target) to
<Carbon Home>/lib location
* Start the WSO2 Carbon Server by executing <Carbon Home>/bin/carbon.sh script

Now you will notice the `Parent configuration - name : WSO2, value : 10, childConfiguration - destination : destination-name, isEnabled : false` 
log message.
 
 You can override the parent configuration values using the deployment.yaml configuration.
 In order to do so add the configurations you want to change under the `"org.wso2.configuration"`
 namespace is deployment.yaml.
 
 **Example deployment.yaml**
 
 ```yaml
  # Carbon Configuration Parameters
wso2.carbon:
    # value to uniquely identify a server
  id: carbon-kernel
    # server name
  name: WSO2 Carbon Kernel
    # ports used by this server
  ports:
      # port offset
    offset: 0

org.wso2.configuration:
  name: "SomeName"
```

The above configuration will give the below output:
`Parent configuration - name : SomeName, value : 10, childConfiguration - destination : destination-name, isEnabled : false`