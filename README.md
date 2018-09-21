[![Travis](https://img.shields.io/travis/metaborg/pie.svg)](https://travis-ci.org/metaborg/pie)
[![Bintray](https://api.bintray.com/packages/metaborg/maven/pie/images/download.svg)](https://bintray.com/metaborg/maven/pie/_latestVersion)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.metaborg/pie.runtime/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.metaborg/pie.runtime)
[![GitHub license](https://img.shields.io/github/license/metaborg/pie.svg)](https://github.com/metaborg/pie/blob/master/LICENSE)

# PIE: Pipelines for Interactive Environments

PIE is a DSL, API, and runtime for developing interactive software development pipelines and incremental build scripts.

## Copyright and License

Copyright © 2018 Delft University of Technology

The code and files in this project are licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
You may use the files in this project in compliance with the license.

## Questions and Issues

If you have a question, enhancement, feature request, or bug report, please search the [issue tracker](https://github.com/Virtlink/commons-configuration2-jackson/issues) for a solution or workaround, or create a new issue.


## User's guide

### Installation

PIE is deployed as a set of Maven artifacts, which you can consume with Maven or Gradle.
For example, to depend on the runtime to build and execute pipelines, add the following Maven dependency to your `pom.xml` file:

```maven-pom
<dependency>
    <groupId>org.metaborg</groupId>
    <artifactId>pie.runtime</artifactId>
    <version>0.2.0</version>
</dependency>
```

or add the following to your `build.gradle` file:

```gradle
compile 'org.metaborg:pie.runtime:0.2.0'
```

### Examples

See the example projects in the [examples](examples) directory.


## Developer's guide

### Building

As requisites for building PIE, you need a [Java Development Kit (JDK)](http://www.oracle.com/technetwork/java/javase/downloads/index.html) of version 8 or higher, and [Maven](https://maven.apache.org/download.cgi) 3.5.4 or higher.
To build PIE, run Maven as follows in the root directory of this repository:

```bash
mvn clean verify
```

### Development

PIE can developed by importing this repository into [IntelliJ IDEA](https://www.jetbrains.com/idea/) or [Eclipse](http://www.eclipse.org/) as Maven projects.
Alternatively, any code editor in conjunction with local builds described above should work.

### Continuous integration

PIE is automatically built and tested on [Travis](https://travis-ci.org/metaborg/pie), configured in the [.travis.yml](.travis.yml) file.
It uses the [.travis.settings.xml](.travis.settings.xml) file as the Maven configuration file, which adds the required MetaBorg maven repositories, and supplies deployment settings through encrypted environment variables.

### Deployment

To deploy PIE, perform the following steps:

* Change the version to a release version (without `-SNAPSHOT`) with the `set_pie_version.sh` script. For example: `./set_pie_version.sh 0.3.0-SNAPSHOT 0.3.0`.
* Build locally to check if PIE still builds correctly.
* Commit and push the version change to `develop`.
* Merge that commit into `master`.
* Tag the merge commit with a release version tag, such as `v0.3.0`.
* Travis will then build and automatically deploy the new version, and notify you about the result.
* In the `develop` branch, bump the version to the next development version as follows: `./set_pie_version.sh 0.3.0 0.4.0-SNAPSHOT`.
* Commit and push this version change to `develop`.
