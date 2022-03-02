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
includeProject("graph")
includeProject("runtime")

includeProject("api.test")
includeProject("runtime.test")

includeProject("share.coroutine")

includeProject("serde.kryo")
includeProject("serde.fst")

includeProject("store.lmdb")

includeProject("taskdefs.guice")
includeProject("dagger")

includeProject("task.java")
includeProject("task.java.ecj")
includeProject("task.archive")

includeProject("example/copyfile")
includeProject("example/helloworld.java")
includeProject("example/helloworld.kotlin")
includeProject("example/playground")
