// Workaround for issue: https://youtrack.jetbrains.com/issue/KTIJ-19369
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
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
    testImplementation(libs.junit)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
