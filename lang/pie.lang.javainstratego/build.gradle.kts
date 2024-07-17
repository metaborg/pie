plugins {
    id("org.metaborg.devenv.spoofax.gradle.langspec")
}

group = "org.metaborg"

spoofaxLanguageSpecification {
    // We add the dependency manually and don't change the repositories
    // Eventually, this functionality should be removed from spoofax.gradle
    addSpoofaxCoreDependency.set(false)
    addSpoofaxRepository.set(false)
}
dependencies {
    compileOnly(libs.spoofax2.core)
}
