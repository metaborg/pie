plugins {
    id("org.metaborg.gradle.config.java-library")
}

dependencies {
    api(platform(project(":pie.depconstraints")))

    compileOnly("org.checkerframework:checker-qual-android")
}
