// Workaround for issue: https://youtrack.jetbrains.com/issue/KTIJ-19369
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("org.metaborg.gradle.config.root-project") version "0.5.6"
    alias(libs.plugins.gitonium)

    // Set versions for plugins to use, only applying them in subprojects (apply false here).
    alias(libs.plugins.kotlin.jvm) apply false
}

subprojects {
    metaborg {
        kotlinApiVersion = "1.2"
        kotlinLanguageVersion = "1.2"
        configureSubProject()
    }
}
