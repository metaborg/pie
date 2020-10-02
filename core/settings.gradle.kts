rootProject.name = "pie.core.root"

pluginManagement {
  repositories {
    maven("https://artifacts.metaborg.org/content/groups/public/")
  }
}

if(org.gradle.util.VersionNumber.parse(gradle.gradleVersion).major < 6) {
  enableFeaturePreview("GRADLE_METADATA")
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
includeProject("task.java")
includeProject("task.archive")
includeProject("example/copyfile")
includeProject("example/helloworld.java")
includeProject("example/helloworld.kotlin")
includeProject("example/playground")
