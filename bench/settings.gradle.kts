rootProject.name = "pie.bench"

pluginManagement {
    repositories {
        maven("https://artifacts.metaborg.org/content/groups/public/")
    }
}

plugins {
    id("org.metaborg.convention.settings") version "0.0.13"
}

includeBuildWithName("../core", "pie.core.root")

fun includeBuildWithName(dir: String, name: String) {
  includeBuild(dir) {
    try {
      ConfigurableIncludedBuild::class.java
        .getDeclaredMethod("setName", String::class.java)
        .invoke(this, name)
    } catch(e: NoSuchMethodException) {
      // Running Gradle < 6, no need to set the name, ignore.
    }
  }
}
