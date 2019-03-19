plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
}

dependencies {
  api(project(":pie.api"))
  api("com.google.inject:guice:4.2.0")
  compileOnly("org.checkerframework:checker-qual-android:2.6.0") // Use android version: annotation retention policy is class instead of runtime.

  testCompile(project(":pie.runtime"))
  testCompileOnly("org.checkerframework:checker-qual-android:2.6.0")
}
