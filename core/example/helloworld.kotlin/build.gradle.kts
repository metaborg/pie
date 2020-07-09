plugins {
  id("org.metaborg.gradle.config.kotlin-application")
}

application {
  mainClassName = "mb.pie.example.helloworld.kotlin.MainKt"
}

dependencies {
  compile(project(":pie.runtime"))
}

metaborg {
  javaCreatePublication = false // Do not publish example application.
}
