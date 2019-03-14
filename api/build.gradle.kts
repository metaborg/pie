plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(project(":fs.api"))
  api(project(":fs.java"))
  // Include annotations as API, because checker framework annotations have a runtime retention policy.
  api("org.checkerframework:checker-qual:2.6.0")
}
