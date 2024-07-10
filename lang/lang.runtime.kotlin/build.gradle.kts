plugins {
    id("org.metaborg.gradle.config.kotlin-library")
}

group = "org.metaborg"

dependencies {
    api(platform(libs.metaborg.platform))
    api(libs.metaborg.pie.api)
}
