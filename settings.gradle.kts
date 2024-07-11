rootProject.name = "pie.root"

pluginManagement {
    repositories {
        maven("https://artifacts.metaborg.org/content/groups/public/")
    }
}

plugins {
    id("org.metaborg.convention.settings") version "0.0.13"
}

includeBuild("core/")
includeBuild("lang/")
