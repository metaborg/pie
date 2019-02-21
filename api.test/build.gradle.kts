plugins {
  id("org.metaborg.gradle.config.kotlin-library")
}

dependencies {
  api(project(":pie.api"))
  api("org.junit.jupiter:junit-jupiter-api:5.2.0")
  api("com.nhaarman:mockito-kotlin:1.5.0")
  implementation("com.google.jimfs:jimfs:1.1")
}
