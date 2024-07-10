plugins {
    id("org.metaborg.gradle.config.java-library")
}

fun compositeBuild(name: String) = "$group:$name:$version"
group = "org.metaborg"

dependencies {
    api(platform(compositeBuild("pie.depconstraints")))

    api(compositeBuild("pie.api"))

    compileOnly("org.checkerframework:checker-qual-android")
}
