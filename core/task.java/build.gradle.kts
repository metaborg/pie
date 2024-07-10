plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
}

group = "org.metaborg"

// Additional dependencies which are injected into tests.
val classPathInjection = configurations.create("classPathInjection")
val annotationProcessorPathInjection = configurations.create("annotationProcessorPathInjection")

dependencies {
    api(platform(libs.metaborg.platform))
    api(libs.metaborg.common)

    api(project(":pie.api"))

    compileOnly(libs.checkerframework.android)
    compileOnly(libs.immutables.value.annotations)

    annotationProcessor(libs.immutables.value)

    testImplementation(project(":pie.runtime"))
    testImplementation(libs.junit)
    testImplementation(libs.jimfs)
    testCompileOnly(libs.checkerframework.android)

    classPathInjection(platform(libs.metaborg.platform))
    classPathInjection(libs.metaborg.log.api)
    classPathInjection(libs.metaborg.log.backend.slf4j)
    classPathInjection(libs.immutables.value.annotations)

    annotationProcessorPathInjection(libs.immutables.value)
}

tasks.test {
    // Pass classPathInjection and annotationProcessorPathInjection to tests in the form of system properties
    dependsOn(classPathInjection)
    doFirst {
        // Wrap in doFirst to properly defer dependency resolution to the task execution phase.
        systemProperty("classPath", classPathInjection.resolvedConfiguration.resolvedArtifacts.map { it.file }.joinToString(File.pathSeparator))
        systemProperty("annotationProcessorPath", annotationProcessorPathInjection.resolvedConfiguration.resolvedArtifacts.map { it.file }.joinToString(File.pathSeparator))
    }

    //debugOptions.enabled.set(true)
    testLogging {
        lifecycle {
            events.add(org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_OUT)
            events.add(org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR)
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
