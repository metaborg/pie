[![GitHub license](https://img.shields.io/github/license/metaborg/pie)](https://github.com/metaborg/pie/blob/master/LICENSE)
[![Jenkins](https://img.shields.io/jenkins/build/https/buildfarm.metaborg.org/job/metaborg/job/pie/job/master)](https://buildfarm.metaborg.org/job/metaborg/job/pie/job/master/lastBuild)
[![Jenkins Tests](https://img.shields.io/jenkins/tests/https/buildfarm.metaborg.org/job/metaborg/job/pie/job/master)](https://buildfarm.metaborg.org/job/metaborg/job/pie/job/master/lastBuild/testReport/)
![PIE API](https://img.shields.io/maven-metadata/v?label=pie.api&metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fpie.api%2Fmaven-metadata.xml)
![PIE Runtime](https://img.shields.io/maven-metadata/v?label=pie.runtime&metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fpie.runtime%2Fmaven-metadata.xml)

# PIE: Pipelines for Interactive Environments

PIE is an API and runtime for developing interactive software development pipelines and incremental build scripts.


## Copyright and License

Copyright © 2018-2019 Delft University of Technology

The code and files in this project are licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
You may use the files in this project in compliance with the license.


## Questions and Issues

If you have a question, enhancement, feature request, or bug report, please search the [issue tracker](https://github.com/metaborg/pie/issues) for a solution or workaround, or create a new issue.


## User's guide

### Installation

PIE is deployed as a set of Maven artifacts to the MetaBorg artifact server, which you can consume with Maven or Gradle.
To be able to get artifacts from the MetaBorg artifact servers, add the following to your `build.gradle` file:

```gradle
repositories {
  jcenter()
  maven { url "https://artifacts.metaborg.org/content/repositories/releases/" }
  maven { url "https://artifacts.metaborg.org/content/repositories/snapshots/" }
}
```

or `build.gradle.kts` file:

```gradle.kts
repositories {
  jcenter()
  maven { url = uri("https://artifacts.metaborg.org/content/repositories/releases/") }
  maven { url = uri("https://artifacts.metaborg.org/content/repositories/snapshots/") }
}
```

or add the following to your Maven `pom.xml` file:

```maven-pom
<repositories>
  <repository>
    <id>metaborg-release-repo</id>
    <url>https://artifacts.metaborg.org/content/repositories/releases/</url>
    <releases>
      <enabled>true</enabled>
    </releases>
    <snapshots>
      <enabled>false</enabled>
    </snapshots>
  </repository>
  <repository>
    <id>metaborg-snapshot-repo</id>
    <url>https://artifacts.metaborg.org/content/repositories/snapshots/</url>
    <releases>
      <enabled>false</enabled>
    </releases>
    <snapshots>
      <enabled>true</enabled>
    </snapshots>
  </repository>
</repositories>
```

To depend on version `0.6.0` of the runtime to build and execute pipelines, add the following Gradle dependency to your `build.gradle` file:

```gradle
implementation 'org.metaborg:pie.runtime:0.6.0'
```

or `build.gradle.kts` file:

```gradle.kts
implementation("org.metaborg:pie.runtime:0.6.0")
```

or add the following to your Maven `pom.xml` file:

```maven-pom
<dependency>
  <groupId>org.metaborg</groupId>
  <artifactId>pie.runtime</artifactId>
  <version>0.6.0</version>
</dependency>
```

The latest stable version is listed at the top of this file.
The development version in the `develop` branch is published on every commit with version `develop-SNAPSHOT` which you can depend on as follows:

```gradle
implementation 'org.metaborg:pie.runtime:develop-SNAPSHOT'
```

```gradle.kts
implementation("org.metaborg:pie.runtime:develop-SNAPSHOT")
```

```maven-pom
<dependency>
  <groupId>org.metaborg</groupId>
  <artifactId>pie.runtime</artifactId>
  <version>develop-SNAPSHOT</version>
</dependency>
```

### Components

PIE consists of several components:

* [pie.api](api): The PIE API for developing reusable interactive pipelines or incremental build scripts
* [pie.runtime](runtime): The PIE runtime for incrementally executing pipelines developed with the API
* [pie.taskdefs.guice](taskdefs.guice): Guice dependency injection support for task definitions
* [pie.dagger](dagger): Dagger dependency injection support

## Developer's guide

### Building

As prerequisites for building PIE, you need a [Java Development Kit (JDK)](https://adoptopenjdk.net/) of version 8 or higher.
To build PIE, run the Gradle wrapper as follows in the root directory of this repository:

```bash
./gradlew buildAll
```

### Development

PIE can developed by importing this repository into [IntelliJ IDEA](https://www.jetbrains.com/idea/) or [Eclipse](http://www.eclipse.org/) as Gradle projects.
Alternatively, any code editor in conjunction with local builds described above should work.

### Continuous integration

PIE is automatically built, tested, and published on [our buildfarm](https://buildfarm.metaborg.org/job/metaborg/job/pie/), configured in the [Jenkinsfile](Jenkinsfile).
It uses the [gradlePipeline](https://github.com/metaborg/jenkins.pipeline/blob/master/vars/gradlePipeline.groovy) shared pipeline from the `metaborg.jenkins.pipeline` shared pipeline library.

### Publishing

To publish PIE, run `./gradlew publishAll`.

To publish a new release version of PIE, first Git commit your changes and tag the commit in the form of `release-<version>`, e.g., `release-0.3.0`, and then run `./gradlew publishAll`.
