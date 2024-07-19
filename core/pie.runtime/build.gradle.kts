plugins {
    `java-library`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
}

group = "org.metaborg"

dependencies {
    api(platform(libs.metaborg.platform)) { version { require("latest.integration") } }
    api(libs.metaborg.common)

    api(project(":pie.api"))
    api(project(":pie.graph"))

    compileOnly(libs.checkerframework.android)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
