plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.kotlin-testing-only")
  id("org.metaborg.gradle.config.junit-testing")
}

dependencies {
  api(platform(project(":pie.depconstraints")))

  api(project(":pie.api"))
  implementation(project(":pie.runtime"))
  implementation("org.lmdbjava:lmdbjava:0.8.1")

  compileOnly("org.checkerframework:checker-qual-android")

  testImplementation(project(":pie.runtime.test"))
  testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin")
  testImplementation(kotlin("reflect")) // Use correct version of reflection library; mockito-kotlin uses an old one.
}
