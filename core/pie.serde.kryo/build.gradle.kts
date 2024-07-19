plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.convention.junit")
}

group = "org.metaborg"

dependencies {
    api(project(":pie.api"))
    api(libs.kryo)

    compileOnly(libs.checkerframework.android)
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

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
