plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(platform(project(":pie.depconstraints")))
  
  api(project(":fs.api"))
  
  compileOnly("org.checkerframework:checker-qual-android")
}
