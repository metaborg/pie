rootProject.name = "pie"

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
includeProject("lang")
includeProject("lang.test")
includeProject("lang.runtime.kotlin")
includeProject("lang.runtime.java")
//includeProject("lang.javainstratego") // Disabled: we're not building a concrete syntax parse table right now.
includeProject("example/copyfile")
includeProject("example/helloworld.java")
includeProject("example/helloworld.kotlin")
includeProject("example/playground")
