plugins {
  id("org.metaborg.gradle.config.root-project") version "0.2.4"
  id("org.metaborg.gitonium") version "0.1.0"
  kotlin("jvm") version "1.3.20" apply false // Apply only in sub-projects.
}

subprojects {
  metaborgConfig {
    configureSubProject()
  }
}
