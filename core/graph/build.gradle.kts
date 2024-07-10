plugins {
    id("org.metaborg.gradle.config.java-library")
}

dependencies {
    api(platform(project(":pie.depconstraints")))
group = "org.metaborg"

    compileOnly("org.checkerframework:checker-qual-android")
}
