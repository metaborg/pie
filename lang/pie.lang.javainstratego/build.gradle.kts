plugins {
    id("org.metaborg.devenv.spoofax.gradle.langspec")
}

group = "org.metaborg"

spoofaxLanguageSpecification {
    addCompileDependenciesFromMetaborgYaml.set(false)
    addSourceDependenciesFromMetaborgYaml.set(false)

    // We add the dependency manually and don't change the repositories
    // Eventually, this functionality should be removed from spoofax.gradle
    addSpoofaxCoreDependency.set(false)
    addSpoofaxRepository.set(false)
}
dependencies {
    compileLanguage(libs.sdf3.lang)

    compileOnly(libs.spoofax.core)
}
