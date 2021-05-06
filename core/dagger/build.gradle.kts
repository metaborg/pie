plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
}

val daggerVersion = "2.32"

dependencies {
  api(platform(project(":pie.depconstraints")))

  api(project(":pie.api"))
  api("org.metaborg:log.dagger")
  api("org.metaborg:resource.dagger")
  api("com.google.dagger:dagger:$daggerVersion")

  annotationProcessor("com.google.dagger:dagger-compiler:$daggerVersion")
  compileOnly("org.checkerframework:checker-qual-android")

  testImplementation(project(":pie.runtime"))
  testAnnotationProcessor("com.google.dagger:dagger-compiler:$daggerVersion")
}
