plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
  id("org.metaborg.spoofax.gradle.project")
}


dependencies {
  api(platform(project(":pie.depconstraints")))
  annotationProcessor(platform(project(":pie.depconstraints")))
  testAnnotationProcessor(platform(project(":pie.depconstraints")))

  api("com.google.dagger:dagger")
  annotationProcessor("com.google.dagger:dagger-compiler")

  compileLanguage(project(":pie.lang"))

  compileOnly("org.checkerframework:checker-qual-android")

  testAnnotationProcessor("com.google.dagger:dagger-compiler")

  testCompileOnly("org.checkerframework:checker-qual-android")

  testImplementation("org.metaborg:resource")
  testImplementation(project(":pie.runtime"))
  testImplementation(project(":pie.lang.runtime.java"))
  testImplementation(project(":pie.dagger"))
}

sourceSets {
  test {
    java {
      srcDir("build/generated/sources/")
    }
  }
}
