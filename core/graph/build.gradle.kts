plugins {
    id("org.metaborg.gradle.config.java-library")
}

group = "org.metaborg"

dependencies {
    compileOnly(libs.checkerframework.android)
}
