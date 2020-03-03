[![Build Status](https://travis-ci.com/heremaps/here-artifact-maven-wagon.svg?token=qChpbefwyQKBzgjbCQ4s&branch=master)](https://travis-ci.com/heremaps/here-artifact-maven-wagon)

# HERE platform Maven Wagon plugin

##  Introduction
The HERE platform Maven Wagon plugin provides Java and Scala developers with access to platform artifacts via Maven. It uses your platform credentials to generate tokens so it can pull your Maven project dependencies from the HERE platform.

This way Marketplace and Workspace users may [fetch platform schemas](https://developer.here.com/olp/documentation/archetypes/dev_guide/topics/archetypes-schema.html). In addition, Marketplace users may [fetch the Java / Scala Data Client Library](https://developer.here.com/olp/documentation/marketplace-consumer/user-guide/topics/data.html) giving them access to data in the HERE Data API. 

Go to [HERE]( https://developer.here.com/products/open-location-platform) to learn more the platform.

To learn more about Maven Wagon visit [this page](https://maven.apache.org/wagon/).

## How to use it?
This Maven Wagon plugin is published on Maven Central so you can conveniently use it from your Maven POM.

For example, to fetch the HMC Topology Geometry schema set the following dependencies:

```xml
<dependencies>
  <dependency>
    <groupId>com.here.schema.rib</groupId>
    <artifactId>topology-geometry_v2_java</artifactId>
    <version>${topology-geometry.library.version}</version>
    <type>jar</type>
  </dependency>
  <dependency>
    <groupId>com.here.schema.rib</groupId>
    <artifactId>topology-geometry_v2_proto</artifactId>
    <version>${topology-geometry.library.version}</version>
    <type>zip</type>
  </dependency>
  <dependency>
    <groupId>com.here.schema.rib</groupId>
    <artifactId>topology-geometry_v2_scala</artifactId>
    <version>${topology-geometry.library.version}</version>
    <type>jar</type>
  </dependency>
</dependencies>

<build>
  <extensions>
    <extension>
      <groupId>com.here.platform.artifact</groupId>
      <artifactId>artifact-wagon</artifactId>
      <version>${artifact.wagon.version}</version>
    </extension>
  </extensions>
</build>
```

As a Marketplace user you can add this dependency for fetching the Java / Scala Data Client Library:

```xml
<dependencies>
  <dependency>
    <groupId>com.here.platform.data.client</groupId>
    <artifactId>data-client_2.11</artifactId>
    <version>${data.client.library.version}</version>
  </dependency>
</dependencies>

<build>
  <extensions>
    <extension>
      <groupId>com.here.platform.artifact</groupId>
      <artifactId>artifact-wagon</artifactId>
      <version>${artifact.wagon.version}</version>
    </extension>
  </extensions>
</build>
```


##  Prerequisites
To access libraries and schemas from HERE platform, you need a HERE Workspace and/or a HERE Marketplace account. If you don’t already have an account yet, go to [Pricing and Plans](https://developer.here.com/pricing/open-location-platform) to apply for a free trial.

You need to get install credentials and prepare your environment. Workspace users find corresponding guidance [in the documentation for Java and Scala developers]( https://developer.here.com/olp/documentation/sdk-developer-guide/dev_guide/topics/how-to-use-sdk.html). Marketplace user find instructions [in the Marketplace Consumer user guide](https://developer.here.com/olp/documentation/marketplace-consumer/user-guide/topics/data.html#register-app).

## License
Copyright (C) 2018-2020 HERE Europe B.V.

Unless otherwise noted in `LICENSE` files for specific files or directories, the [LICENSE](LICENSE) in the root applies to all content in this repository.
