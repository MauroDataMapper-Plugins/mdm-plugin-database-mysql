# mdm-plugin-database-mysql

| Branch | Build Status |
| ------ | ------------ |
| main | [![Build Status](https://jenkins.cs.ox.ac.uk/buildStatus/icon?job=Mauro+Data+Mapper+Plugins%2Fmdm-plugin-database-mysql%2Fmain)](https://jenkins.cs.ox.ac.uk/blue/organizations/jenkins/Mauro%20Data%20Mapper%20Plugins%2Fmdm-plugin-database-mysql/branches) |
| develop | [![Build Status](https://jenkins.cs.ox.ac.uk/buildStatus/icon?job=Mauro+Data+Mapper+Plugins%2Fmdm-plugin-database-mysql%2Fdevelop)](https://jenkins.cs.ox.ac.uk/blue/organizations/jenkins/Mauro%20Data%20Mapper%20Plugins%2Fmdm-plugin-database-mysql/branches) |

## Requirements

* Java 17 (Temurin)
* Grails 5.1.9+
* Gradle 7.3.3+

All of the above can be installed and easily maintained by using [SDKMAN!](https://sdkman.io/install).

## Applying the Plugin

The preferred way of running Mauro Data Mapper is using the [mdm-docker](https://github.com/MauroDataMapper/mdm-docker) deployment. However you can
also run the backend on its own from [mdm-application-build](https://github.com/MauroDataMapper/mdm-application-build).

### mdm-docker

In the `docker-compose.yml` file add:

```yml
mauro-data-mapper:
    build:
        args:
            ADDITIONAL_PLUGINS: "uk.ac.ox.softeng.maurodatamapper.plugins:mdm-plugin-database-mysql:7.1.0"
```

Please note, if adding more than one plugin, this is a semicolon-separated list

### mdm-application-build

In the `dependencies.gradle` file add:

```groovy
dependencies {
    runtimeOnly 'uk.ac.ox.softeng.maurodatamapper.plugins:mdm-plugin-database-mysql:7.1.0'
}
```

## Development

To run tests:
```./gradlew --build-cache integrationTest```

With debug:
```./gradlew --build-cache integrationTest --debug-jvm```  
