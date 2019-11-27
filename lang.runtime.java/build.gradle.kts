plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  compile(platform(project(":pie.depconstraints")))

  compile(project(":pie.taskdefs.guice"))

  compile("com.google.inject:guice:4.2.0")

  compileOnly("org.checkerframework:checker-qual-android")
}
