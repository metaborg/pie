plugins {
  id("org.metaborg.gradle.config.kotlin-library")
  id("org.metaborg.gradle.config.junit-testing")
}

dependencies {
  api(platform(project(":pie.depconstraints")))
  
  api(project(":pie.api"))
  api(project(":pie.api.test"))
  api(project(":pie.runtime"))
  
  api("org.junit.jupiter:junit-jupiter-api")
  api("com.nhaarman.mockitokotlin2:mockito-kotlin")
  implementation(kotlin("reflect")) // Use correct version of reflection library; mockito-kotlin uses an old one.
}
