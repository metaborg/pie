plugins {
  id("org.metaborg.gradle.config.java-application")
}

application {
  mainClassName = "mb.pie.example.helloworld.java.Main"
}

dependencies {
  implementation(platform(project(":pie.depconstraints")))

  implementation(project(":pie.runtime"))
  implementation(project(":pie.taskdefs.guice"))

  compileOnly("org.checkerframework:checker-qual-android")
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
