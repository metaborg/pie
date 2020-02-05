plugins {
  id("org.metaborg.gradle.config.kotlin-library")
}

dependencies {
  api(platform(project(":pie.depconstraints")))
  
  api(project(":pie.api"))
}
