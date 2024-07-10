plugins {
    id("org.metaborg.gradle.config.kotlin-library")
    id("org.metaborg.gradle.config.junit-testing")
}

group = "org.metaborg"

dependencies {
    api(project(":pie.api"))

    api(libs.junit.api)
    api(libs.mockito.kotlin)
    implementation(kotlin("reflect")) // Use correct version of reflection library; mockito-kotlin uses an old one.
    implementation(libs.jimfs)
}
