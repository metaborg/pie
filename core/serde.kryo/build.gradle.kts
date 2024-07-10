plugins {
    id("org.metaborg.gradle.config.java-library")
    id("org.metaborg.gradle.config.junit-testing")
}

group = "org.metaborg"

dependencies {
    api(platform(project(":pie.depconstraints")))

    api(project(":pie.api"))
    api("com.esotericsoftware.kryo:kryo5:5.0.0")

    compileOnly("org.checkerframework:checker-qual-android")
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
