plugins {
  id("org.metaborg.gradle.config.root-project") version "0.3.5"
  id("org.metaborg.gitonium") version "0.1.0"
  kotlin("jvm") version "1.3.21" apply false // Apply only in sub-projects.
}

subprojects {
  metaborg {
    kotlinApiVersion = "1.2"
    kotlinLanguageVersion = "1.2"
    configureSubProject()
  }
}
