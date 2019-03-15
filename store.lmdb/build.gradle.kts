plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.kotlin-testing-only")
  id("org.metaborg.gradle.config.junit-testing")
}

dependencies {
  api(project(":pie.api"))
  implementation("org.lmdbjava:lmdbjava:0.6.3")
  compileOnly("org.checkerframework:checker-qual-android:2.6.0") // Use android version: annotation retention policy is class instead of runtime.

  testImplementation(project(":pie.runtime.test"))
  testImplementation("com.nhaarman:mockito-kotlin:1.5.0")
  testImplementation(kotlin("reflect")) // Use correct version of reflection library; mockito-kotlin uses an old one.
}
