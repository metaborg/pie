plugins {
    id("org.metaborg.gradle.config.java-library")
}

dependencies {
    api(platform(project(":pie.depconstraints")))

    api(project(":pie.api"))
    api(project(":pie.graph"))
    api("org.metaborg:common")

    compileOnly("org.checkerframework:checker-qual-android")
}
