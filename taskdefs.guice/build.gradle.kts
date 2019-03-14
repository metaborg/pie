plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(project(":pie.api"))
  api("com.google.inject:guice:4.2.2")
  // Include annotations as API, because checker framework annotations have a runtime retention policy.
  api("org.checkerframework:checker-qual:2.6.0")
}
