rootProject.name = "pie"

pluginManagement {
  repositories {
    maven(url = "https://artifacts.metaborg.org/content/repositories/releases/")
    maven(url = "https://artifacts.metaborg.org/content/repositories/snapshots/")
    // Following repositories are required for Spoofax Gradle plugin.
    maven("https://pluto-build.github.io/mvnrepository/")
    maven("https://sugar-lang.github.io/mvnrepository/")
    maven("http://nexus.usethesource.io/content/repositories/public/")
    gradlePluginPortal()
  }
}

include("fs.api")
include("fs.java")

fun includeProject(path: String, id: String = "pie.${path.replace('/', '.')}") {
  include(id)
  project(":$id").projectDir = file(path)
}

includeProject("api")
includeProject("api.test")
includeProject("runtime")
includeProject("runtime.test")
includeProject("share.coroutine")
includeProject("store.lmdb")
includeProject("taskdefs.guice")
includeProject("lang")
includeProject("lang.runtime")
includeProject("example/copyfile")
includeProject("example/helloworld.java")
includeProject("example/helloworld.kotlin")
