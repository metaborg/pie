plugins {
    id("org.metaborg.gradle.config.root-project") version "0.7.3"
    id("org.metaborg.gitonium") version "1.2.0"

    // Set versions for plugins to use, only applying them in subprojects (apply false here).
    kotlin("jvm") version "1.3.61" apply false
}

subprojects {
    metaborg {
        kotlinApiVersion = "1.3"
        kotlinLanguageVersion = "1.3"
        configureSubProject()
    }
}
