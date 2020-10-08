plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
}

dependencies {
  api(platform(project(":pie.depconstraints")))

  api(project(":pie.api"))

  compileOnly("org.checkerframework:checker-qual-android")

  testImplementation(project(":pie.runtime"))
  testImplementation("com.google.jimfs:jimfs:1.1")
  testCompileOnly("org.checkerframework:checker-qual-android")
}
