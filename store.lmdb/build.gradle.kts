plugins {
  id("org.metaborg.gradle.config.kotlin-library") // TODO: turn into Java library (with Kotlin testing)
  id("org.metaborg.gradle.config.junit-testing")
}

dependencies {
  api(project(":pie.api"))
  implementation("org.lmdbjava:lmdbjava:0.6.1") // TODO: update to 0.6.3.
  // Include annotations as API, because checker framework annotations have a runtime retention policy.
  api("org.checkerframework:checker-qual:2.6.0")

  testImplementation(project(":pie.runtime.test"))
  testImplementation("com.nhaarman:mockito-kotlin:1.5.0")
  testImplementation(kotlin("reflect")) // Use correct version of reflection library; mockito-kotlin uses an old one.
}
