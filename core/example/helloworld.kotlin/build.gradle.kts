plugins {
  id("org.metaborg.gradle.config.kotlin-application")
}

application {
  mainClass.set("mb.pie.example.helloworld.kotlin.MainKt")
}

dependencies {
  implementation(project(":pie.runtime"))
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
