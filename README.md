<!--
!! THIS FILE WAS GENERATED USING repoman !!
Modify `repo.yaml` instead and use `repoman` to update this file
See: https://github.com/metaborg/metaborg-gradle/
-->

# Metaborg PIE
[![Build][github-badge:build]][github:build]
[![License][license-badge]][license]
[![GitHub Release][github-badge:release]][github:release]

PIE is an API and runtime for developing interactive software development pipelines and incremental build scripts.


## Spoofax 3 Artifacts


| Maven Artifact | Latest Release | Latest Snapshot |
|----------|----------------|-----------------|
| `org.metaborg:pie.api` | [![Release][mvn-rel-badge:org.metaborg:pie.api]][mvn:org.metaborg:pie.api] | [![Snapshot][mvn-snap-badge:org.metaborg:pie.api]][mvn:org.metaborg:pie.api] |
| `org.metaborg:pie.api.test` | [![Release][mvn-rel-badge:org.metaborg:pie.api.test]][mvn:org.metaborg:pie.api.test] | [![Snapshot][mvn-snap-badge:org.metaborg:pie.api.test]][mvn:org.metaborg:pie.api.test] |
| `org.metaborg:pie.dagger` | [![Release][mvn-rel-badge:org.metaborg:pie.dagger]][mvn:org.metaborg:pie.dagger] | [![Snapshot][mvn-snap-badge:org.metaborg:pie.dagger]][mvn:org.metaborg:pie.dagger] |
| `org.metaborg:pie.graph` | [![Release][mvn-rel-badge:org.metaborg:pie.graph]][mvn:org.metaborg:pie.graph] | [![Snapshot][mvn-snap-badge:org.metaborg:pie.graph]][mvn:org.metaborg:pie.graph] |
| `org.metaborg:pie.runtime` | [![Release][mvn-rel-badge:org.metaborg:pie.runtime]][mvn:org.metaborg:pie.runtime] | [![Snapshot][mvn-snap-badge:org.metaborg:pie.runtime]][mvn:org.metaborg:pie.runtime] |
| `org.metaborg:pie.runtime.test` | [![Release][mvn-rel-badge:org.metaborg:pie.runtime.test]][mvn:org.metaborg:pie.runtime.test] | [![Snapshot][mvn-snap-badge:org.metaborg:pie.runtime.test]][mvn:org.metaborg:pie.runtime.test] |
| `org.metaborg:pie.serde.fst` | [![Release][mvn-rel-badge:org.metaborg:pie.serde.fst]][mvn:org.metaborg:pie.serde.fst] | [![Snapshot][mvn-snap-badge:org.metaborg:pie.serde.fst]][mvn:org.metaborg:pie.serde.fst] |
| `org.metaborg:pie.serde.kryo` | [![Release][mvn-rel-badge:org.metaborg:pie.serde.kryo]][mvn:org.metaborg:pie.serde.kryo] | [![Snapshot][mvn-snap-badge:org.metaborg:pie.serde.kryo]][mvn:org.metaborg:pie.serde.kryo] |
| `org.metaborg:pie.share.coroutine` | [![Release][mvn-rel-badge:org.metaborg:pie.share.coroutine]][mvn:org.metaborg:pie.share.coroutine] | [![Snapshot][mvn-snap-badge:org.metaborg:pie.share.coroutine]][mvn:org.metaborg:pie.share.coroutine] |
| `org.metaborg:pie.store.lmdb` | [![Release][mvn-rel-badge:org.metaborg:pie.store.lmdb]][mvn:org.metaborg:pie.store.lmdb] | [![Snapshot][mvn-snap-badge:org.metaborg:pie.store.lmdb]][mvn:org.metaborg:pie.store.lmdb] |
| `org.metaborg:pie.task.archive` | [![Release][mvn-rel-badge:org.metaborg:pie.task.archive]][mvn:org.metaborg:pie.task.archive] | [![Snapshot][mvn-snap-badge:org.metaborg:pie.task.archive]][mvn:org.metaborg:pie.task.archive] |
| `org.metaborg:pie.task.java` | [![Release][mvn-rel-badge:org.metaborg:pie.task.java]][mvn:org.metaborg:pie.task.java] | [![Snapshot][mvn-snap-badge:org.metaborg:pie.task.java]][mvn:org.metaborg:pie.task.java] |
| `org.metaborg:pie.task.java.ecj` | [![Release][mvn-rel-badge:org.metaborg:pie.task.java.ecj]][mvn:org.metaborg:pie.task.java.ecj] | [![Snapshot][mvn-snap-badge:org.metaborg:pie.task.java.ecj]][mvn:org.metaborg:pie.task.java.ecj] |
| `org.metaborg:pie.taskdefs.guice` | [![Release][mvn-rel-badge:org.metaborg:pie.taskdefs.guice]][mvn:org.metaborg:pie.taskdefs.guice] | [![Snapshot][mvn-snap-badge:org.metaborg:pie.taskdefs.guice]][mvn:org.metaborg:pie.taskdefs.guice] |
| `org.metaborg:pie.lang` | [![Release][mvn-rel-badge:org.metaborg:pie.lang]][mvn:org.metaborg:pie.lang] | [![Snapshot][mvn-snap-badge:org.metaborg:pie.lang]][mvn:org.metaborg:pie.lang] |
| `org.metaborg:pie.lang.runtime.java` | [![Release][mvn-rel-badge:org.metaborg:pie.lang.runtime.java]][mvn:org.metaborg:pie.lang.runtime.java] | [![Snapshot][mvn-snap-badge:org.metaborg:pie.lang.runtime.java]][mvn:org.metaborg:pie.lang.runtime.java] |
| `org.metaborg:pie.lang.runtime.kotlin` | [![Release][mvn-rel-badge:org.metaborg:pie.lang.runtime.kotlin]][mvn:org.metaborg:pie.lang.runtime.kotlin] | [![Snapshot][mvn-snap-badge:org.metaborg:pie.lang.runtime.kotlin]][mvn:org.metaborg:pie.lang.runtime.kotlin] |
| `org.metaborg:pie.lang.test` | [![Release][mvn-rel-badge:org.metaborg:pie.lang.test]][mvn:org.metaborg:pie.lang.test] | [![Snapshot][mvn-snap-badge:org.metaborg:pie.lang.test]][mvn:org.metaborg:pie.lang.test] |




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
Copyright 2017-2024 [Programming Languages Group](https://pl.ewi.tudelft.nl/), [Delft University of Technology](https://www.tudelft.nl/)

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at <https://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an **"as is" basis, without warranties or conditions of any kind**, either express or implied. See the License for the specific language governing permissions and limitations under the License.

[github-badge:build]: https://img.shields.io/github/actions/workflow/status/metaborg/pie/build.yaml
[github:build]: https://github.com/metaborg/pie/actions
[license-badge]: https://img.shields.io/github/license/metaborg/pie
[license]: https://github.com/metaborg/pie/blob/master/LICENSE.md
[github-badge:release]: https://img.shields.io/github/v/release/metaborg/pie?display_name=release
[github:release]: https://github.com/metaborg/pie/releases
[mvn:org.metaborg:pie.api]: https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~pie.api~~~
[mvn:org.metaborg:pie.api.test]: https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~pie.api.test~~~
[mvn:org.metaborg:pie.dagger]: https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~pie.dagger~~~
[mvn:org.metaborg:pie.graph]: https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~pie.graph~~~
[mvn:org.metaborg:pie.lang]: https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~pie.lang~~~
[mvn:org.metaborg:pie.lang.runtime.java]: https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~pie.lang.runtime.java~~~
[mvn:org.metaborg:pie.lang.runtime.kotlin]: https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~pie.lang.runtime.kotlin~~~
[mvn:org.metaborg:pie.lang.test]: https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~pie.lang.test~~~
[mvn:org.metaborg:pie.runtime]: https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~pie.runtime~~~
[mvn:org.metaborg:pie.runtime.test]: https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~pie.runtime.test~~~
[mvn:org.metaborg:pie.serde.fst]: https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~pie.serde.fst~~~
[mvn:org.metaborg:pie.serde.kryo]: https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~pie.serde.kryo~~~
[mvn:org.metaborg:pie.share.coroutine]: https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~pie.share.coroutine~~~
[mvn:org.metaborg:pie.store.lmdb]: https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~pie.store.lmdb~~~
[mvn:org.metaborg:pie.task.archive]: https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~pie.task.archive~~~
[mvn:org.metaborg:pie.task.java]: https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~pie.task.java~~~
[mvn:org.metaborg:pie.task.java.ecj]: https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~pie.task.java.ecj~~~
[mvn:org.metaborg:pie.taskdefs.guice]: https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~pie.taskdefs.guice~~~
[mvn-rel-badge:org.metaborg:pie.api]: https://img.shields.io/nexus/r/org.metaborg/pie.api?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-rel-badge:org.metaborg:pie.api.test]: https://img.shields.io/nexus/r/org.metaborg/pie.api.test?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-rel-badge:org.metaborg:pie.dagger]: https://img.shields.io/nexus/r/org.metaborg/pie.dagger?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-rel-badge:org.metaborg:pie.graph]: https://img.shields.io/nexus/r/org.metaborg/pie.graph?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-rel-badge:org.metaborg:pie.lang]: https://img.shields.io/nexus/r/org.metaborg/pie.lang?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-rel-badge:org.metaborg:pie.lang.runtime.java]: https://img.shields.io/nexus/r/org.metaborg/pie.lang.runtime.java?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-rel-badge:org.metaborg:pie.lang.runtime.kotlin]: https://img.shields.io/nexus/r/org.metaborg/pie.lang.runtime.kotlin?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-rel-badge:org.metaborg:pie.lang.test]: https://img.shields.io/nexus/r/org.metaborg/pie.lang.test?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-rel-badge:org.metaborg:pie.runtime]: https://img.shields.io/nexus/r/org.metaborg/pie.runtime?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-rel-badge:org.metaborg:pie.runtime.test]: https://img.shields.io/nexus/r/org.metaborg/pie.runtime.test?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-rel-badge:org.metaborg:pie.serde.fst]: https://img.shields.io/nexus/r/org.metaborg/pie.serde.fst?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-rel-badge:org.metaborg:pie.serde.kryo]: https://img.shields.io/nexus/r/org.metaborg/pie.serde.kryo?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-rel-badge:org.metaborg:pie.share.coroutine]: https://img.shields.io/nexus/r/org.metaborg/pie.share.coroutine?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-rel-badge:org.metaborg:pie.store.lmdb]: https://img.shields.io/nexus/r/org.metaborg/pie.store.lmdb?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-rel-badge:org.metaborg:pie.task.archive]: https://img.shields.io/nexus/r/org.metaborg/pie.task.archive?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-rel-badge:org.metaborg:pie.task.java]: https://img.shields.io/nexus/r/org.metaborg/pie.task.java?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-rel-badge:org.metaborg:pie.task.java.ecj]: https://img.shields.io/nexus/r/org.metaborg/pie.task.java.ecj?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-rel-badge:org.metaborg:pie.taskdefs.guice]: https://img.shields.io/nexus/r/org.metaborg/pie.taskdefs.guice?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-snap-badge:org.metaborg:pie.api]: https://img.shields.io/nexus/s/org.metaborg/pie.api?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-snap-badge:org.metaborg:pie.api.test]: https://img.shields.io/nexus/s/org.metaborg/pie.api.test?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-snap-badge:org.metaborg:pie.dagger]: https://img.shields.io/nexus/s/org.metaborg/pie.dagger?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-snap-badge:org.metaborg:pie.graph]: https://img.shields.io/nexus/s/org.metaborg/pie.graph?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-snap-badge:org.metaborg:pie.lang]: https://img.shields.io/nexus/s/org.metaborg/pie.lang?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-snap-badge:org.metaborg:pie.lang.runtime.java]: https://img.shields.io/nexus/s/org.metaborg/pie.lang.runtime.java?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-snap-badge:org.metaborg:pie.lang.runtime.kotlin]: https://img.shields.io/nexus/s/org.metaborg/pie.lang.runtime.kotlin?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-snap-badge:org.metaborg:pie.lang.test]: https://img.shields.io/nexus/s/org.metaborg/pie.lang.test?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-snap-badge:org.metaborg:pie.runtime]: https://img.shields.io/nexus/s/org.metaborg/pie.runtime?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-snap-badge:org.metaborg:pie.runtime.test]: https://img.shields.io/nexus/s/org.metaborg/pie.runtime.test?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-snap-badge:org.metaborg:pie.serde.fst]: https://img.shields.io/nexus/s/org.metaborg/pie.serde.fst?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-snap-badge:org.metaborg:pie.serde.kryo]: https://img.shields.io/nexus/s/org.metaborg/pie.serde.kryo?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-snap-badge:org.metaborg:pie.share.coroutine]: https://img.shields.io/nexus/s/org.metaborg/pie.share.coroutine?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-snap-badge:org.metaborg:pie.store.lmdb]: https://img.shields.io/nexus/s/org.metaborg/pie.store.lmdb?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-snap-badge:org.metaborg:pie.task.archive]: https://img.shields.io/nexus/s/org.metaborg/pie.task.archive?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-snap-badge:org.metaborg:pie.task.java]: https://img.shields.io/nexus/s/org.metaborg/pie.task.java?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-snap-badge:org.metaborg:pie.task.java.ecj]: https://img.shields.io/nexus/s/org.metaborg/pie.task.java.ecj?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-snap-badge:org.metaborg:pie.taskdefs.guice]: https://img.shields.io/nexus/s/org.metaborg/pie.taskdefs.guice?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
