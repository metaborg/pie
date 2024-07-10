// Workaround for issue: https://youtrack.jetbrains.com/issue/KTIJ-19369
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("org.metaborg.gradle.config.root-project") version "0.7.1"
    alias(libs.plugins.gitonium)

    // Set versions for plugins to use, only applying them in subprojects (apply false here).
    alias(libs.plugins.spoofax.gradle.langspec) apply false
    alias(libs.plugins.spoofax.gradle.project) apply false
    alias(libs.plugins.kotlin.jvm) apply false
}

subprojects {
    metaborg {
        configureSubProject()
    }
}
