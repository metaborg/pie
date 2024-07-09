rootProject.name = "pie.core.root"

// This allows us to use plugins from Metaborg Artifacts
pluginManagement {
    repositories {
        maven("https://artifacts.metaborg.org/content/groups/public/")
    }
}

// This allows us to use the catalog in dependencies
dependencyResolutionManagement {
    repositories {
        maven("https://artifacts.metaborg.org/content/groups/public/")
    }
    versionCatalogs {
        create("libs") {
            from("org.metaborg.spoofax3:catalog:0.3.3")
        }
    }
}

// This downloads an appropriate JVM if not already available
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
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
