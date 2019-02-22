plugins {
  id("org.metaborg.gradle.config.kotlin-library")
}

dependencies {
  api(project(":pie.api"))
  api(project(":pie.runtime"))
  api("com.google.inject:guice:4.2.0")
}
