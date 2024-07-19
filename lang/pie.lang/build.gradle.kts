plugins {
    `java-library`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
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
    compileLanguage(libs.esv.lang)
    compileLanguage(libs.sdf3.lang)
    compileLanguage(libs.statix.lang)
    compileLanguage(libs.sdf3.extstatix)

    sourceLanguage(libs.meta.lib.spoofax)
    sourceLanguage(libs.statix.runtime)
    sourceLanguage("org.metaborg:lang.java:1.0.0")

    compileOnly(libs.spoofax.core)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
