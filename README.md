[![GitHub license](https://img.shields.io/github/license/metaborg/pie.svg)](https://github.com/metaborg/pie/blob/master/LICENSE)

# PIE: Pipelines for Interactive Environments

PIE is a DSL, API, and runtime for developing interactive software development pipelines and incremental build scripts.

## Copyright and License

Copyright © 2018-2019 Delft University of Technology

The code and files in this project are licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
You may use the files in this project in compliance with the license.

## Questions and Issues

If you have a question, enhancement, feature request, or bug report, please search the [issue tracker](https://github.com/Virtlink/commons-configuration2-jackson/issues) for a solution or workaround, or create a new issue.


## User's guide

### Installation

PIE is deployed as a set of Maven artifacts, which you can consume with Maven or Gradle.
For example, to depend on the runtime to build and execute pipelines, add the following Gradle dependency to your `build.gradle` file:

```gradle
implementation 'org.metaborg:pie.runtime:0.2.0'
```

or add the following to your Maven `pom.xml` file:

```maven-pom
<dependency>
    <groupId>org.metaborg</groupId>
    <artifactId>pie.runtime</artifactId>
    <version>0.2.0</version>
</dependency>
```

### Components

PIE consists of several components:

* [API](api): The PIE API for developing reusable interactive pipelines or incremental build scripts (artifact ID: `pie.api`)
* [Runtime](runtime): The PIE runtime for incrementally executing pipelines developed with the API (artifact ID: `pie.runtime`)
  * [LMDB storage support](store.lmdb): Storage/persistence support for the runtime using the LMDB embedded database (artifact ID: `pie.store.lmdb`)
* [Guice support for task definitions](taskdefs.guice): Guice dependency injection support for task definitions (artifact ID: `pie.taskdefs.guice`)
* [DSL](lang): The PIE DSL for concisely writing pipelines (`org.metaborg:pie.lang`)


## Developer's guide

### Building

As prerequisites for building PIE, you need a [Java Development Kit (JDK)](http://www.oracle.com/technetwork/java/javase/downloads/index.html) of version 8 or higher.
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

* 
* Run `./gradlew publishAll`.
