plugins {
  id("org.metaborg.gradle.config.root-project") version "0.4.3"
  id("org.metaborg.gitonium") version "0.1.4"

  // Set versions for plugins to use, only applying them in subprojects (apply false here).
  id("org.metaborg.spoofax.gradle.langspec") version "0.4.8" apply false
  id("org.metaborg.spoofax.gradle.project") version "0.4.8" apply false
  kotlin("jvm") version "1.3.61" apply false
}

subprojects {
  metaborg {
    kotlinApiVersion = "1.2"
    kotlinLanguageVersion = "1.2"
    configureSubProject()
  }
}
