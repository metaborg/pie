plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(project(":pie.api"))
  // Include annotations as API, because checker framework annotations have a runtime retention policy.
  api("org.checkerframework:checker-qual:2.6.0")
}
