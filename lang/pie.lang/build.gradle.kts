plugins {
    id("org.metaborg.devenv.spoofax.gradle.langspec")
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
