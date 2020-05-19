plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
  id("org.metaborg.spoofax.gradle.project")
}

dependencies {
  api(platform(project(":pie.depconstraints")))

  compileLanguage(project(":pie.lang"))

  testImplementation("org.metaborg:resource")
  testImplementation(project(":pie.runtime"))
  testImplementation(project(":pie.lang.runtime.java"))

  testCompileOnly("org.checkerframework:checker-qual-android")
}
