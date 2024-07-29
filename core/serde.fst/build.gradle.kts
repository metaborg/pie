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
    jvmArgs(listOf(
        // Needed for Java 17
        "--add-opens", "java.base/java.io=ALL-UNNAMED",
        "--add-opens", "java.base/java.lang=ALL-UNNAMED",
        "--add-opens", "java.base/java.lang.invoke=ALL-UNNAMED",
        "--add-opens", "java.base/java.math=ALL-UNNAMED",
        "--add-opens", "java.base/java.net=ALL-UNNAMED",
        "--add-opens", "java.base/java.text=ALL-UNNAMED",
        "--add-opens", "java.base/java.util=ALL-UNNAMED",
        "--add-opens", "java.base/java.util.concurrent=ALL-UNNAMED"
    ))
    reporting {
        testLogging {
            lifecycle {
                events(org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_OUT)
            }
        }
    }
}
