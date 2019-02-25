plugins {
  id("org.metaborg.gradle.config.kotlin-library")
}

dependencies {
  api(project(":fs.api"))
  api(project(":fs.java"))
  compileOnly("org.checkerframework:checker-qual:2.6.0")
}
