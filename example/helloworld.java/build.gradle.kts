plugins {
  id("org.metaborg.gradle.config.java-application")
}

application {
  mainClassName = "mb.pie.example.helloworld.java.Main"
}

dependencies {
  compile(platform(project(":pie.depconstraints")))

  compile(project(":pie.runtime"))
  compile(project(":pie.util"))
  compile(project(":pie.store.lmdb"))
  compile(project(":pie.taskdefs.guice"))

  compile("com.google.inject:guice:4.2.0")

  compileOnly("org.checkerframework:checker-qual-android")
}

metaborg {
  javaCreatePublication = false // Do not publish example application.
}
