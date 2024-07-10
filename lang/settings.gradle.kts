rootProject.name = "pie.lang.root"

pluginManagement {
    repositories {
        maven("https://artifacts.metaborg.org/content/groups/public/")
    }
}

plugins {
    id("org.metaborg.convention.settings") version "0.0.11"
}


include(":pie.lang")
//include(":pie.lang.javainstratego") // Disabled: we're not building a concrete syntax parse table right now.
include(":pie.lang.test")
include(":pie.lang.runtime.kotlin")
include(":pie.lang.runtime.java")
