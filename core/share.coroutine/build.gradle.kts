plugins {
    id("org.metaborg.gradle.config.kotlin-library")
    id("org.metaborg.gradle.config.junit-testing")
}

dependencies {
    api(platform(project(":pie.depconstraints")))

    api(project(":pie.api"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.1")

    testImplementation(project(":pie.runtime.test"))
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin")
    testImplementation(kotlin("reflect")) // Use correct version of reflection library; mockito-kotlin uses an old one.
}
