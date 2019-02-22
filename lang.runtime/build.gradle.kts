plugins {
  id("org.metaborg.gradle.config.kotlin-library")
}

dependencies {
  api(project(":fs.java"))
  implementation(project(":pie.api"))
}
