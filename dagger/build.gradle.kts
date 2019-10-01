plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
  id("net.ltgt.apt") version "0.21"
  id("net.ltgt.apt-idea") version "0.21"
  id("net.ltgt.apt-eclipse") version "0.21"
}

val daggerVersion = "2.24"

dependencies {
  api(platform(project(":pie.depconstraints")))

  // Main
  api(project(":pie.api"))

  api("com.google.dagger:dagger:$daggerVersion")
  annotationProcessor("com.google.dagger:dagger-compiler:$daggerVersion")

  compileOnly("org.checkerframework:checker-qual-android")

  // Test
  testImplementation(project(":pie.runtime"))

  testAnnotationProcessor("com.google.dagger:dagger-compiler:$daggerVersion")
}
