plugins {
    id("org.metaborg.gradle.config.java-library")
}

dependencies {
    api(platform(project(":pie.depconstraints")))
group = "org.metaborg"

    api("org.metaborg:resource")
    api("org.metaborg:log.api")

    compileOnly("org.checkerframework:checker-qual-android")
}
