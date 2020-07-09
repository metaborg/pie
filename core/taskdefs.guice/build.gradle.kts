plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
}

dependencies {
  api(platform(project(":pie.depconstraints")))

  api(project(":pie.api"))
  api("com.google.inject:guice:4.2.0")

  compileOnly("org.checkerframework:checker-qual-android")

  testCompile(project(":pie.runtime"))
  testCompileOnly("org.checkerframework:checker-qual-android")
}
