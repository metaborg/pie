plugins {
  id("org.metaborg.gradle.config.kotlin-library")
  id("org.metaborg.gradle.config.junit-testing")
}

dependencies {
  api(project(":pie.api"))
  api(project(":pie.api.test"))
  api(project(":pie.runtime"))
  api("org.junit.jupiter:junit-jupiter-api:5.2.0")
  api("com.nhaarman:mockito-kotlin:1.5.0")
  implementation(kotlin("reflect")) // Use correct version of reflection library; mockito-kotlin uses an old one.
}
