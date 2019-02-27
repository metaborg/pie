plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(project(":fs.api"))
  compileOnly("org.checkerframework:checker-qual:2.6.0")
}
