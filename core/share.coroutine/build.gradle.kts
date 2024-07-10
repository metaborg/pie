plugins {
    id("org.metaborg.gradle.config.kotlin-library")
    id("org.metaborg.gradle.config.junit-testing")
}

group = "org.metaborg"

dependencies {
    api(project(":pie.api"))
    api(libs.kotlinx.coroutines.core)

    testImplementation(project(":pie.runtime.test"))
    testImplementation(libs.mockito.kotlin)
    testImplementation(kotlin("reflect")) // Use correct version of reflection library; mockito-kotlin uses an old one.
}
