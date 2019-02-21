rootProject.name = "pie"

pluginManagement {
  repositories {
    maven(url = "https://artifacts.metaborg.org/content/repositories/releases/")
    maven(url = "https://artifacts.metaborg.org/content/repositories/snapshots/")
    gradlePluginPortal()
  }
}

include("fs.api")
include("fs.java")

fun includeProject(path: String, id: String = "pie.$path") {
  include(id)
  project(":$id").projectDir = file(path)
}

includeProject("api")
includeProject("api.test")
