plugins {
  id("org.metaborg.gradle.config.kotlin-library")
  id("org.metaborg.gradle.config.junit-testing")
}

metaborg {
  kotlinApiVersion = "1.3"
  kotlinLanguageVersion = "1.3"
}

dependencies {
  api(project(":pie.api"))
  api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.1")

  testImplementation(project(":pie.runtime.test"))
  testImplementation("com.nhaarman:mockito-kotlin:1.5.0")
  testImplementation(kotlin("reflect")) // Use correct version of reflection library; mockito-kotlin uses an old one.
}
