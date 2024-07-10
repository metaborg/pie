rootProject.name = "pie.lang.root"

pluginManagement {
    repositories {
        maven("https://artifacts.metaborg.org/content/groups/public/")
    }
}

plugins {
    id("org.metaborg.convention.settings") version "0.0.11"
}


// Only include composite builds when this is the root project (it has no parent), for example when running Gradle tasks
// from the command-line. Otherwise, the parent project (pie.root) will include these composite builds.
if (gradle.parent == null) {
    includeBuild("../core")
}

fun includeProject(path: String, id: String = "pie.${path.replace('/', '.')}") {
    include(id)
    project(":$id").projectDir = file(path)
}

includeProject("lang")
//includeProject("lang.javainstratego") // Disabled: we're not building a concrete syntax parse table right now.
includeProject("lang.test")
includeProject("lang.runtime.kotlin")
includeProject("lang.runtime.java")
