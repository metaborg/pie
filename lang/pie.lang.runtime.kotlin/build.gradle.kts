// Workaround for issue: https://youtrack.jetbrains.com/issue/KTIJ-19369
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    `java-library`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    alias(libs.plugins.kotlin.jvm)
}

group = "org.metaborg"

dependencies {
    api(platform(libs.metaborg.platform)) { version { require("latest.integration") } }
    api(libs.metaborg.pie.api)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
