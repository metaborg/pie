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
    api(project(":pie.api.test"))
    api(project(":pie.runtime"))

    api(libs.junit.api)
    api(libs.mockito.kotlin)
    implementation(kotlin("reflect")) // Use correct version of reflection library; mockito-kotlin uses an old one.

    testImplementation(libs.jimfs)
    testImplementation(libs.junit)
}

mavenPublishConvention {
    repoOwner.set("metaborg")
    repoName.set("pie")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
