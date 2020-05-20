[![Build Status](https://travis-ci.com/heremaps/here-artifact-maven-wagon.svg?token=qChpbefwyQKBzgjbCQ4s&branch=master)](https://travis-ci.com/heremaps/here-artifact-maven-wagon)
[![Maven Central](https://img.shields.io/maven-central/v/com.here.platform.artifact/artifact-wagon)](https://search.maven.org/artifact/com.here.platform.artifact/artifact-wagon)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

# HERE platform Maven Wagon plugin

##  Introduction
The HERE platform Maven Wagon plugin provides Java and Scala developers with access to HERE platform artifacts via Maven. It uses your HERE platform credentials to generate tokens so it can pull your Maven project dependencies from the HERE platform.

This way Marketplace and Workspace users may [fetch platform schemas](https://developer.here.com/olp/documentation/archetypes/dev_guide/topics/archetypes-schema.html). In addition, Marketplace users may [fetch the Java / Scala Data Client Library](https://developer.here.com/olp/documentation/marketplace-consumer/user-guide/topics/get_catalog_data.html) giving them access to data in the HERE Data API. 

Go to [the HERE Developer portal](https://developer.here.com/products/open-location-platform) to learn more about the HERE platform.

To learn more about Maven Wagon visit [this page](https://maven.apache.org/wagon/).

##  Prerequisites
To access libraries and schemas from the HERE platform, you need a HERE Workspace and/or a HERE Marketplace account. If you don’t have an account yet, go to [Pricing and Plans](https://developer.here.com/pricing/open-location-platform) to apply for a free trial.

Once you have enabled your account you need to create the credentials and prepare your environment. Workspace users can find corresponding guidance [in the documentation for Java and Scala developers]( https://developer.here.com/olp/documentation/sdk-developer-guide/dev_guide/topics/how-to-use-sdk.html). Marketplace users can find instructions [in the Marketplace Consumer user guide](https://developer.here.com/olp/documentation/marketplace-consumer/user-guide/topics/get_catalog_data.html#register-app).

## How to use it?
This Maven Wagon plugin is published on [Maven Central](https://search.maven.org/artifact/com.here.platform.artifact/artifact-wagon) so you can conveniently use it from your Maven POM.

For example, to fetch the HERE Map Content - Topology Geometry - Protocol Buffers schema and the related Java and Scala bindings set the following dependencies:

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

<repositories>
  <!-- The reference to the HERE repository with schemas -->
  <repository>
    <id>HERE_PLATFORM_ARTIFACT</id>
    <layout>default</layout>
    <url>here+artifact-service://artifact-service</url>
  </repository>
</repositories>

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

<repositories>
  <!-- The reference to the HERE repository with schemas -->
  <repository>
    <id>HERE_PLATFORM_ARTIFACT</id>
    <layout>default</layout>
    <url>here+artifact-service://artifact-service</url>
  </repository>
</repositories>

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

`here+artifact-service://artifact-service` is placeholder URL which will be replaced by plugin dynamically based on your credentials.
The latest versions of Data Client Library and Schemas can be found in [SDK documentation](https://developer.here.com/olp/documentation/java-scala-dev/dev_guide/sdk-libraries.html)


#### Proxy Setup
To enable Maven and the HERE Maven Wagon Plugin to work behind a corporate proxy, you need to add the following proxy
settings in the Maven settings file (`settings.xml`), which is normally located at `~/.m2/settings.xml`:

```xml
<settings>
  <proxies>
    <proxy>
      <id>proxy1</id>
      <active>true</active>
      <protocol>https</protocol>
      <host>{enter your proxy host here}</host>
      <port>{enter your proxy port here}</port>
      <username>{enter your proxy username here}</username>
      <password>{enter your proxy password here}</password>
    </proxy>
    <proxy>
      <id>proxy2</id>
      <active>true</active>
      <protocol>here+https</protocol>
      <host>{enter your proxy host here}</host>
      <port>{enter your proxy port here}</port>
      <username>{enter your proxy username here}</username>
      <password>{enter your proxy password here}</password>
    </proxy>
    <proxy>
      <id>proxy3</id>
      <active>true</active>
      <protocol>here+artifact-service</protocol>
      <host>{enter your proxy host here}</host>
      <port>{enter your proxy port here}</port>
      <username>{enter your proxy username here}</username>
      <password>{enter your proxy password here}</password>
    </proxy>
  </proxies>
</settings>
```

## License
Copyright (C) 2018-2020 HERE Europe B.V.

Unless otherwise noted in `LICENSE` files for specific files or directories, the [LICENSE](LICENSE) in the root applies to all content in this repository.
