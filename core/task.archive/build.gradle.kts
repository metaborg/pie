plugins {
    id("org.metaborg.gradle.config.java-library")
    id("org.metaborg.gradle.config.junit-testing")
}

group = "org.metaborg"

dependencies {
    api(project(":pie.api"))

    compileOnly(libs.checkerframework.android)

    testImplementation(project(":pie.runtime"))
    testImplementation(libs.jimfs)
    testCompileOnly(libs.checkerframework.android)
}
