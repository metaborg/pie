// Workaround for issue: https://youtrack.jetbrains.com/issue/KTIJ-19369
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    `java-library`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.convention.junit")
    alias(libs.plugins.kotlin.jvm)
}

group = "org.metaborg"

dependencies {
    api(project(":pie.api"))
    api(project(":pie.api.test"))
    api(project(":pie.runtime"))

    api(libs.junit.api)
    api(libs.mockito.kotlin)
    implementation(kotlin("reflect")) // Use correct version of reflection library; mockito-kotlin uses an old one.

    testImplementation(libs.jimfs)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
