// Workaround for issue: https://youtrack.jetbrains.com/issue/KTIJ-19369
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("org.metaborg.convention.root-project")
    alias(libs.plugins.gitonium)

    // Set versions for plugins to use, only applying them in subprojects (apply false here).
    alias(libs.plugins.spoofax.gradle.langspec) apply false
    alias(libs.plugins.spoofax.gradle.project) apply false
    alias(libs.plugins.kotlin.jvm) apply false
}

// Workaround for issue: https://github.com/gradle/gradle/issues/20131
println("")
