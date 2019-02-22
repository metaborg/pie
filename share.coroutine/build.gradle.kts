import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("org.metaborg.gradle.config.kotlin-library")
}

dependencies {
  api(project(":pie.api"))
  api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.1")
  implementation(kotlin("stdlib-jdk8"))

  testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
  testImplementation(project(":pie.runtime.test"))
  testImplementation(kotlin("reflect"))
  testImplementation("com.nhaarman:mockito-kotlin:1.5.0")
  testRuntime("org.junit.jupiter:junit-jupiter-engine:5.3.1")
}

tasks.withType<KotlinCompile>().all {
  kotlinOptions.apiVersion = "1.3"
  kotlinOptions.languageVersion = "1.3"
}

tasks.withType<Test> {
  useJUnitPlatform {}
}
