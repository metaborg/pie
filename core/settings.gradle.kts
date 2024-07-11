rootProject.name = "pie.core.root"

pluginManagement {
    repositories {
        maven("https://artifacts.metaborg.org/content/groups/public/")
    }
}

plugins {
    id("org.metaborg.convention.settings") version "0.0.13"
}


include(":pie.api")
include(":pie.graph")
include(":pie.runtime")

include(":pie.api.test")
include(":pie.runtime.test")

include(":pie.share.coroutine")

include(":pie.serde.kryo")
include(":pie.serde.fst")

include(":pie.store.lmdb")

include(":pie.taskdefs.guice")
include(":pie.dagger")

include(":pie.task.java")
include(":pie.task.java.ecj")
include(":pie.ask.archive")

include(":pie.example.copyfile")
project(":pie.example.copyfile").projectDir = file("example/copyfile/")
include(":pie.example.helloworld.java")
project(":pie.example.helloworld.java").projectDir = file("example/helloworld.java/")
include(":pie.example.helloworld.kotlin")
project(":pie.example.helloworld.kotlin").projectDir = file("example/helloworld.kotlin/")
include(":pie.example.playground")
project(":pie.example.playground").projectDir = file("example/playground/")
