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

    api(libs.junit.api)
    api(libs.mockito.kotlin)
    implementation(kotlin("reflect")) // Use correct version of reflection library; mockito-kotlin uses an old one.
    implementation(libs.jimfs)

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
