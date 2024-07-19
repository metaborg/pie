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
    implementation(project(":pie.runtime"))
    implementation(libs.lmdbjava)

    compileOnly(libs.checkerframework.android)

    testImplementation(project(":pie.runtime.test"))
    testImplementation(kotlin("stdlib"))    // Explicitly include Kotlin only in the test source set
    testImplementation(kotlin("reflect"))   // Use correct version of reflection library; mockito-kotlin uses an old one.
    testImplementation(libs.mockito.kotlin)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
