plugins {
    id("org.metaborg.spoofax.gradle.langspec")
    `maven-publish`
    id("org.metaborg.convention.maven-publish")
}

group = "org.metaborg"

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
