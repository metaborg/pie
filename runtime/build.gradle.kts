plugins {
  id("org.metaborg.gradle.config.kotlin-library")
}

dependencies {
  api(project(":pie.api"))
  compileOnly("org.checkerframework:checker-qual:2.6.0")
}
