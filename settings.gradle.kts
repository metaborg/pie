rootProject.name = "pie.root"

pluginManagement {
  repositories {
    maven("https://artifacts.metaborg.org/content/groups/public/")
  }
}


// Only include composite builds when this is the root project (it has no parent). Otherwise, the parent project
// (devenv) will include these composite builds, as IntelliJ does not support nested composite builds.
if(gradle.parent == null) {
  includeBuildWithName("core", "pie.core.root")
  includeBuildWithName("lang", "pie.lang.root")
}

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
