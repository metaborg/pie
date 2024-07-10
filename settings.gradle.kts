rootProject.name = "pie.root"

pluginManagement {
    repositories {
        maven("https://artifacts.metaborg.org/content/groups/public/")
    }
}

plugins {
    id("org.metaborg.convention.settings") version "0.0.10"
}

includeBuildWithName("core", "pie.core.root")
includeBuildWithName("lang", "pie.lang.root")

fun includeBuildWithName(dir: String, name: String) {
    includeBuild(dir) {
        try {
            ConfigurableIncludedBuild::class.java
                .getDeclaredMethod("setName", String::class.java)
                .invoke(this, name)
        } catch (e: NoSuchMethodException) {
            // Running Gradle < 6, no need to set the name, ignore.
        }
    }
}
