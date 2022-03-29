plugins {
  id("org.metaborg.gradle.config.root-project") version "0.4.7"
  id("org.metaborg.gitonium") version "0.1.5"

  // Set versions for plugins to use, only applying them in subprojects (apply false here).
  id("org.metaborg.spoofax.gradle.langspec") version "0.5.5" apply false
  id("org.metaborg.spoofax.gradle.project") version "0.5.5" apply false
  kotlin("jvm") version "1.3.61" apply false
}

subprojects {
  metaborg {
    configureSubProject()
  }
}
