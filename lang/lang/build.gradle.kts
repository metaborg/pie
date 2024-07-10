plugins {
    id("org.metaborg.spoofax.gradle.langspec")
    `maven-publish`
    id("org.metaborg.convention.maven-publish")
}

group = "org.metaborg"

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
