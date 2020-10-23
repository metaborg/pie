rootProject.name = "pie.root"

pluginManagement {
  repositories {
    maven("https://artifacts.metaborg.org/content/groups/public/")
  }
}

if(org.gradle.util.VersionNumber.parse(gradle.gradleVersion).major < 6) {
  enableFeaturePreview("GRADLE_METADATA")
}

// Only include composite builds when this is the root project (it has no parent). Otherwise, the parent project
// (devenv) will include these composite builds, as IntelliJ does not support nested composite builds.
if(gradle.parent == null) {
  includeBuild("core")
  includeBuild("lang")
  includeBuild("bench")
}
