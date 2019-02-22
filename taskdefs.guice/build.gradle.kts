import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("org.metaborg.gradle.config.kotlin-library")
}

dependencies {
  api(project(":pie.api"))
  api(project(":pie.runtime"))
  api("com.google.inject:guice:4.2.0")
  implementation(kotlin("stdlib-jdk8"))

  testImplementation(project(":pie.runtime.test"))
  testImplementation("com.nhaarman:mockito-kotlin:1.5.0")
}

tasks.withType<KotlinCompile>().all {
  kotlinOptions.apiVersion = "1.2"
  kotlinOptions.languageVersion = "1.2"
}
