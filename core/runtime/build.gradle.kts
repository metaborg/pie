plugins {
    id("org.metaborg.gradle.config.java-library")
}

group = "org.metaborg"

dependencies {
    api(platform(libs.metaborg.platform))
    api(libs.metaborg.common)

    api(project(":pie.api"))
    api(project(":pie.graph"))

    compileOnly(libs.checkerframework.android)
}
