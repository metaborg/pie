plugins {
    id("org.metaborg.gradle.config.kotlin-library")
    id("org.metaborg.gradle.config.junit-testing")
}

group = "org.metaborg"

dependencies {
    api(project(":pie.api"))
    api(project(":pie.api.test"))
    api(project(":pie.runtime"))

    api(libs.junit.api)
    api(libs.mockito.kotlin)
    implementation(kotlin("reflect")) // Use correct version of reflection library; mockito-kotlin uses an old one.

    testImplementation(libs.jimfs)
}
