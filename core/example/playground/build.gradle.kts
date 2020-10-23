plugins {
  id("org.metaborg.gradle.config.kotlin-application")
}

application {
  mainClassName = "mb.pie.example.playground.MainKt"
}

dependencies {
  compile(project(":pie.runtime"))
  compile(project(":pie.store.lmdb"))
}

metaborg {
  javaCreatePublication = false // Do not publish benchmark.
}
tasks {
  // Disable currently unused distribution tasks.
  distZip.configure { enabled = false }
  distTar.configure { enabled = false }
  startScripts.configure { enabled = false }
}
