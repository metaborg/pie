plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(project(":fs.api"))
  compileOnly("com.google.code.findbugs:jsr305:3.0.2")
}
