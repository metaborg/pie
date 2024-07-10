plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
}

group = "org.metaborg"

dependencies {
    api(project(":pie.api"))
    api(libs.kryo)

    compileOnly(libs.checkerframework.android)

    testImplementation(libs.junit)
}

tasks.test {
    reporting {
        testLogging {
            lifecycle {
                events(org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_OUT)
            }
        }
    }
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
