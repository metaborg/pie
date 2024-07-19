plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.convention.junit")
}

group = "org.metaborg"

dependencies {
    api(platform(libs.metaborg.platform)) { version { require("latest.integration") } }
    api(libs.metaborg.log.dagger)
    api(libs.metaborg.resource.dagger)

    api(project(":pie.api"))
    api(libs.dagger)

    annotationProcessor(libs.dagger.compiler)
    compileOnly(libs.checkerframework.android)

    testImplementation(project(":pie.runtime"))
    testAnnotationProcessor(libs.dagger.compiler)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
