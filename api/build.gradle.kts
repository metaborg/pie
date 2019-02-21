plugins {
  id("org.metaborg.gradle.config.kotlin-library")
}

dependencies {
  api(project(":fs.api"))
  api(project(":fs.java"))
}
