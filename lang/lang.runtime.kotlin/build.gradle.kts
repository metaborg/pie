plugins {
    id("org.metaborg.gradle.config.kotlin-library")
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
    api(platform(compositeBuild("pie.depconstraints")))

    api(compositeBuild("pie.api"))
}
