rootProject.name = "pie"

pluginManagement {
  repositories {
    // Get plugins from artifacts.metaborg.org, first.
    maven("https://artifacts.metaborg.org/content/repositories/releases/")
    maven("https://artifacts.metaborg.org/content/repositories/snapshots/")
    // Required by several Gradle plugins (Maven central).
    maven("https://artifacts.metaborg.org/content/repositories/central/") // Maven central mirror.
    mavenCentral() // Maven central as backup.
    // Required by spoofax.gradle plugin.
    maven("https://pluto-build.github.io/mvnrepository/")
    maven("https://sugar-lang.github.io/mvnrepository/")
    maven("http://nexus.usethesource.io/content/repositories/public/")
    // Get plugins from Gradle plugin portal.
    gradlePluginPortal()
  }
}

fun includeProject(path: String, id: String = "pie.${path.replace('/', '.')}") {
  include(id)
  project(":$id").projectDir = file(path)
}

includeProject("depconstraints")
includeProject("api")
includeProject("api.test")
includeProject("runtime")
includeProject("runtime.test")
includeProject("share.coroutine")
includeProject("store.lmdb")
includeProject("taskdefs.guice")
includeProject("dagger")
includeProject("lang")
includeProject("lang.test")
includeProject("lang.runtime.kotlin")
includeProject("lang.runtime.java")
//includeProject("lang.javainstratego") // Disabled: we're not building a concrete syntax parse table right now.
includeProject("example/copyfile")
includeProject("example/helloworld.java")
includeProject("example/helloworld.kotlin")
includeProject("example/playground")
