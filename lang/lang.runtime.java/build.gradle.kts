plugins {
    id("org.metaborg.gradle.config.java-library")
}

group = "org.metaborg"

dependencies {
    api(platform(libs.metaborg.platform))
    api(libs.metaborg.pie.api)

    compileOnly(libs.checkerframework.android)
}
