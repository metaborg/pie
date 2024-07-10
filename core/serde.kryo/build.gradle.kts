plugins {
    id("org.metaborg.gradle.config.java-library")
    id("org.metaborg.gradle.config.junit-testing")
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
