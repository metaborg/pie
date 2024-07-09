# Metaborg PIE
[![Build][github-badge:build]][github:build]
[![License][license-badge]][license]
[![GitHub Release][github-badge:release]][github:release]

PIE is an API and runtime for developing interactive software development pipelines and incremental build scripts.

| Artifact                               | Latest Release                                                                                                |
|----------------------------------------|---------------------------------------------------------------------------------------------------------------|
| `org.metaborg:pie.api`                 | [![org.metaborg:pie.api][maven-badge:pie.api]][maven:pie.api]                                                 |
| `org.metaborg:pie.api.test`            | [![org.metaborg:pie.api.test][maven-badge:pie.api.test]][maven:pie.api.test]                                  |
| `org.metaborg:pie.dagger`              | [![org.metaborg:pie.dagger][maven-badge:pie.dagger]][maven:pie.dagger]                                        |
| `org.metaborg:pie.graph`               | [![org.metaborg:pie.graph][maven-badge:pie.graph]][maven:pie.graph]                                           |
| `org.metaborg:pie.runtime`             | [![org.metaborg:pie.runtime][maven-badge:pie.runtime]][maven:pie.runtime]                                     |
| `org.metaborg:pie.runtime.test`        | [![org.metaborg:pie.runtime.test][maven-badge:pie.runtime.test]][maven:pie.runtime.test]                      |
| `org.metaborg:pie.serde.fst`           | [![org.metaborg:pie.serde.fst][maven-badge:pie.serde.fst]][maven:pie.serde.fst]                               |
| `org.metaborg:pie.serde.kryo`          | [![org.metaborg:pie.serde.kryo][maven-badge:pie.serde.kryo]][maven:pie.serde.kryo]                            |
| `org.metaborg:pie.share.coroutine`     | [![org.metaborg:pie.share.coroutine][maven-badge:pie.share.coroutine]][maven:pie.share.coroutine]             |
| `org.metaborg:pie.store.lmdb`          | [![org.metaborg:pie.store.lmdb][maven-badge:pie.store.lmdb]][maven:pie.store.lmdb]                            |
| `org.metaborg:pie.task.archive`        | [![org.metaborg:pie.task.archive][maven-badge:pie.task.archive]][maven:pie.task.archive]                      |
| `org.metaborg:pie.task.java`           | [![org.metaborg:pie.task.java][maven-badge:pie.task.java]][maven:pie.task.java]                               |
| `org.metaborg:pie.task.java.ecj`       | [![org.metaborg:pie.task.java.ecj][maven-badge:pie.task.java.ecj]][maven:pie.task.java.ecj]                   |
| `org.metaborg:pie.taskdefs.guice`      | [![org.metaborg:pie.taskdefs.guice][maven-badge:pie.taskdefs.guice]][maven:pie.taskdefs.guice]                |
| `org.metaborg:pie.lang`                | [![org.metaborg:pie.lang][maven-badge:pie.lang]][maven:pie.lang]                                              |
| `org.metaborg:pie.lang.runtime.java`   | [![org.metaborg:pie.lang.runtime.java][maven-badge:pie.lang.runtime.java]][maven:pie.lang.runtime.java]       |
| `org.metaborg:pie.lang.runtime.kotlin` | [![org.metaborg:pie.lang.runtime.kotlin][maven-badge:pie.lang.runtime.kotlin]][maven:pie.lang.runtime.kotlin] |
| `org.metaborg:pie.lang.test`           | [![org.metaborg:pie.lang.test][maven-badge:pie.lang.test]][maven:pie.lang.test]                               |


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

To depend on a specific version of the runtime to build and execute pipelines, add the following Gradle dependency to your `build.gradle` file:

```gradle
implementation 'org.metaborg:pie.runtime:<version>'
```

or `build.gradle.kts` file:

```gradle.kts
implementation("org.metaborg:pie.runtime:<version>")
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


## License
Copyright 2018-2024 Delft University of Technology

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at <https://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an **"as is" basis, without warranties or conditions of any kind**, either express or implied. See the License for the specific language governing permissions and limitations under the License.

[github-badge:build]: https://img.shields.io/github/actions/workflow/status/metaborg/pie/build.yaml
[github:build]: https://github.com/metaborg/pie/actions
[license-badge]: https://img.shields.io/github/license/metaborg/pie
[license]: https://github.com/metaborg/pie/blob/master/LICENSE
[github-badge:release]: https://img.shields.io/github/v/release/metaborg/pie
[github:release]: https://github.com/metaborg/pie/releases

[maven:pie.api]:                  https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~pie.api~~~
[maven:pie.api.test]:             https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~pie.api.test~~~
[maven:pie.dagger]:               https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~pie.dagger~~~
[maven:pie.graph]:                https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~pie.graph~~~
[maven:pie.runtime]:              https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~pie.runtime~~~
[maven:pie.runtime.test]:         https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~pie.runtime.test~~~
[maven:pie.serde.fst]:            https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~pie.serde.fst~~~
[maven:pie.serde.kryo]:           https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~pie.serde.kryo~~~
[maven:pie.share.coroutine]:      https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~pie.share.coroutine~~~
[maven:pie.store.lmdb]:           https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~pie.store.lmdb~~~
[maven:pie.task.archive]:         https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~pie.task.archive~~~
[maven:pie.task.java]:            https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~pie.task.java~~~
[maven:pie.task.java.ecj]:        https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~pie.task.java.ecj~~~
[maven:pie.taskdefs.guice]:       https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~pie.taskdefs.guice~~~
[maven:pie.lang]:                 https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~pie.lang~~~
[maven:pie.lang.runtime.java]:    https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~pie.lang.runtime.java~~~
[maven:pie.lang.runtime.kotlin]:  https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~pie.lang.runtime.kotlin~~~
[maven:pie.lang.test]:            https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~pie.lang.test~~~

[maven-badge:pie.api]:                  https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fpie.api%2Fmaven-metadata.xml
[maven-badge:pie.api.test]:             https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fpie.api.test%2Fmaven-metadata.xml
[maven-badge:pie.dagger]:               https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fpie.dagger%2Fmaven-metadata.xml
[maven-badge:pie.graph]:                https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fpie.graph%2Fmaven-metadata.xml
[maven-badge:pie.runtime]:              https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fpie.runtime%2Fmaven-metadata.xml
[maven-badge:pie.runtime.test]:         https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fpie.runtime.test%2Fmaven-metadata.xml
[maven-badge:pie.serde.fst]:            https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fpie.serde.fst%2Fmaven-metadata.xml
[maven-badge:pie.serde.kryo]:           https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fpie.serde.kryo%2Fmaven-metadata.xml
[maven-badge:pie.share.coroutine]:      https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fpie.share.coroutine%2Fmaven-metadata.xml
[maven-badge:pie.store.lmdb]:           https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fpie.store.lmdb%2Fmaven-metadata.xml
[maven-badge:pie.task.archive]:         https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fpie.task.archive%2Fmaven-metadata.xml
[maven-badge:pie.task.java]:            https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fpie.task.java%2Fmaven-metadata.xml
[maven-badge:pie.task.java.ecj]:        https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fpie.task.java.ecj%2Fmaven-metadata.xml
[maven-badge:pie.taskdefs.guice]:       https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fpie.taskdefs.guice%2Fmaven-metadata.xml
[maven-badge:pie.lang]:                 https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fpie.lang%2Fmaven-metadata.xml
[maven-badge:pie.lang.runtime.java]:    https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fpie.lang.runtime.java%2Fmaven-metadata.xml
[maven-badge:pie.lang.runtime.kotlin]:  https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fpie.lang.runtime.kotlin%2Fmaven-metadata.xml
[maven-badge:pie.lang.test]:            https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fpie.lang.test%2Fmaven-metadata.xml
