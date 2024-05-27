plugins {
    id("org.metaborg.gradle.config.java-library")
    id("org.metaborg.gradle.config.junit-testing")
}

dependencies {
    api(platform(project(":pie.depconstraints")))

    api(project(":pie.api"))
    api("de.ruedigermoeller:fst:2.56")

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
