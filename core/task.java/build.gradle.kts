plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
}

dependencies {
  api(platform(project(":pie.depconstraints")))

  api(project(":pie.api"))

  compileOnly("org.checkerframework:checker-qual-android")

  testImplementation(project(":pie.runtime"))
  testImplementation("com.google.jimfs:jimfs:1.1")
  testCompileOnly("org.checkerframework:checker-qual-android")
}

// Additional dependencies which are injected into tests.
val classPathInjection = configurations.create("classPathInjection")
dependencies {
  classPathInjection("org.metaborg:log.api:0.3.0")
  classPathInjection("org.metaborg:log.backend.slf4j:0.3.0")
}
val annotationProcessorPathInjection = configurations.create("annotationProcessorPathInjection")
dependencies {
  annotationProcessorPathInjection("org.immutables:value:2.8.2")
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
