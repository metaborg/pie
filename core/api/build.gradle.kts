plugins {
    id("org.metaborg.gradle.config.java-library")
}

group = "org.metaborg"

dependencies {
    api(platform(libs.metaborg.platform))
    api(libs.metaborg.resource.api)
    api(libs.metaborg.log.api)

    compileOnly(libs.checkerframework.android)
}
