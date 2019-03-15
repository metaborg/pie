plugins {
  id("org.metaborg.gradle.config.java-application")
}

application {
  mainClassName = "mb.pie.example.helloworld.java.Main"
}

dependencies {
  compile(project(":pie.runtime"))
  compile(project(":pie.store.lmdb"))
  compileOnly("org.checkerframework:checker-qual:2.6.0")
}
