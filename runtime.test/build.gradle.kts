import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("org.metaborg.gradle.config.kotlin-library")
}

dependencies {
  api(project(":pie.api"))
  api(project(":pie.api.test"))
  api(project(":pie.runtime"))
  implementation(kotlin("stdlib-jdk8"))
  implementation(kotlin("reflect"))
  api("org.junit.jupiter:junit-jupiter-api:5.2.0")
  api("com.nhaarman:mockito-kotlin:1.5.0")

  testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
  testRuntime("org.junit.jupiter:junit-jupiter-engine:5.3.1")
}

tasks.withType<KotlinCompile>().all {
  kotlinOptions.apiVersion = "1.2"
  kotlinOptions.languageVersion = "1.2"
}

tasks.withType<Test> {
  useJUnitPlatform {}
}
