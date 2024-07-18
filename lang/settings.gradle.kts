import org.metaborg.convention.MavenPublishConventionExtension

rootProject.name = "pie.lang.root"

dependencyResolutionManagement {
    repositories {
        maven("https://artifacts.metaborg.org/content/groups/public/")
        mavenCentral()
    }
}

pluginManagement {
    repositories {
        maven("https://artifacts.metaborg.org/content/groups/public/")
        gradlePluginPortal()
    }
}

plugins {
    id("org.metaborg.convention.settings") version "latest.integration"
}


include(":pie.lang")
//include(":pie.lang.javainstratego") // Disabled: we're not building a concrete syntax parse table right now.
include(":pie.lang.test")
include(":pie.lang.runtime.kotlin")
include(":pie.lang.runtime.java")
