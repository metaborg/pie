plugins {
    id("org.metaborg.gradle.config.java-library")
    id("org.metaborg.gradle.config.kotlin-testing-only")
    id("org.metaborg.gradle.config.junit-testing")
}

group = "org.metaborg"

dependencies {
    api(project(":pie.api"))
    implementation(project(":pie.runtime"))
    implementation(libs.lmdbjava)

    compileOnly(libs.checkerframework.android)

    testImplementation(project(":pie.runtime.test"))
    testImplementation(libs.mockito.kotlin)
    testImplementation(kotlin("reflect")) // Use correct version of reflection library; mockito-kotlin uses an old one.
}
