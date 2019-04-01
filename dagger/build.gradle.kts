plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
  id("net.ltgt.apt") version "0.21"
  id("net.ltgt.apt-idea") version "0.21"
}

val daggerVersion = "2.21"

dependencies {
  api(project(":pie.runtime"))
  api("com.google.dagger:dagger:$daggerVersion")
  annotationProcessor("com.google.dagger:dagger-compiler:$daggerVersion")
  compileOnly("org.checkerframework:checker-qual-android:2.6.0") // Use android version: annotation retention policy is class instead of runtime.
}
