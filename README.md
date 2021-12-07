[![GitHub license](https://img.shields.io/github/license/metaborg/pie)](https://github.com/metaborg/pie/blob/master/LICENSE)
[![GitHub actions](https://img.shields.io/github/workflow/status/metaborg/pie/Build?label=GitHub%20actions)](https://github.com/metaborg/pie/actions/workflows/build.yml)
[![Jenkins](https://img.shields.io/jenkins/build/https/buildfarm.metaborg.org/job/metaborg/job/pie/job/master?label=Jenkins)](https://buildfarm.metaborg.org/job/metaborg/job/pie/job/master/lastBuild)
[![Jenkins Tests](https://img.shields.io/jenkins/tests/https/buildfarm.metaborg.org/job/metaborg/job/pie/job/master?label=Jenkins%20tests)](https://buildfarm.metaborg.org/job/metaborg/job/pie/job/master/lastBuild/testReport/)
[![PIE API](https://img.shields.io/maven-metadata/v?label=pie.api&metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fpie.api%2Fmaven-metadata.xml)](https://mvnrepository.com/artifact/org.metaborg/pie.api?repo=metaborg-releases)
[![PIE Runtime](https://img.shields.io/maven-metadata/v?label=pie.runtime&metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fpie.runtime%2Fmaven-metadata.xml)](https://mvnrepository.com/artifact/org.metaborg/pie.runtime?repo=metaborg-releases)

# PIE: Pipelines for Interactive Environments

PIE is an API and runtime for developing interactive software development pipelines and incremental build scripts.

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

The latest version is listed at the top of this file.

### Components

PIE consists of several components:

* [pie.api](core/api): The PIE API for developing reusable interactive pipelines or incremental build scripts
* [pie.runtime](core/runtime): The PIE runtime for incrementally executing pipelines developed with the API
* [pie.taskdefs.guice](core/taskdefs.guice): Guice dependency injection support for task definitions
* [pie.dagger](core/dagger): Dagger dependency injection support
* [task.java](core/task.java): Java compilation tasks
* [task.archive](core/task.archive): ZIP/Jar archiving and unarchiving tasks

## Development

This section details the development of this project.

### Building

The `master` branch of this repository can be built in isolation.
However, the `develop` branch must be built via the [devenv repository](https://github.com/metaborg/devenv), due to it depending on development versions of other projects.

This repository is built with Gradle, which requires a JDK of at least version 8 to be installed. Higher versions may work depending on [which version of Gradle is used](https://docs.gradle.org/current/userguide/compatibility.html).

To build this repository, run `./gradlew buildAll` on Linux and macOS, or `gradlew buildAll` on Windows.

### Automated Builds

This repository is built on:
- [GitHub actions](https://github.com/metaborg/pie/actions/workflows/build.yml) via `.github/workflows/build.yml`. Only the `master` branch is built here.
- Our [Jenkins buildfarm](https://buildfarm.metaborg.org/view/Devenv/job/metaborg/job/pie/) via `Jenkinsfile` which uses our [Jenkins pipeline library](https://github.com/metaborg/jenkins.pipeline/).

### Publishing

This repository is published via Gradle and Git with the [Gitonium](https://github.com/metaborg/gitonium) and [Gradle Config](https://github.com/metaborg/gradle.config) plugins.
It is published to our [artifact server](https://artifacts.metaborg.org) in the [releases repository](https://artifacts.metaborg.org/content/repositories/releases/).

First update `CHANGELOG.md` with your changes, create a new release entry, and update the release links at the bottom of the file.

Then, commit your changes and merge them from the `develop` branch into the `master` branch, and ensure that you depend on only released versions of other projects (i.e., no `SNAPSHOT` or development versions).
All dependencies are managed in the `depconstraints/build.gradle.kts` file.

To make a new release, create a tag in the form of `release-*` where `*` is the version of the release you'd like to make.
Then first build the project with `./gradlew buildAll` to check if building succeeds.

If you want our buildfarm to publish this release, just push the tag you just made, and our buildfarm will build the repository and publish the release.

If you want to publish this release locally, you will need an account with write access to our artifact server, and tell Gradle about this account.
Create the `./gradle/gradle.properties` file if it does not exist.
Add the following lines to it, replacing `<username>` and `<password>` with those of your artifact server account:
```
publish.repository.metaborg.artifacts.username=<username>
publish.repository.metaborg.artifacts.password=<password>
```
Then run `./gradlew publishAll` to publish all built artifacts.
You should also push the release tag you made such that this release is reproducible by others.

## Copyright and License

Copyright © 2018-2021 Delft University of Technology

The files in this repository are licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
You may use the files in this repository in compliance with the license.
