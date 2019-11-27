rootProject.name = "pie"

pluginManagement {
  repositories {
    // Get plugins from artifacts.metaborg.org, first.
    maven("https://artifacts.metaborg.org/content/repositories/releases/")
    maven("https://artifacts.metaborg.org/content/repositories/snapshots/")
    // Required by several Gradle plugins (Maven central).
    maven("https://artifacts.metaborg.org/content/repositories/central/") // Maven central mirror.
    mavenCentral() // Maven central as backup.
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
includeProject("lang.runtime")
includeProject("example/copyfile")
includeProject("example/helloworld.java")
includeProject("example/helloworld.kotlin")
includeProject("example/playground")
includeProject("util")
