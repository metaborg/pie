import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("org.metaborg.gradle.config.kotlin-library")
}

dependencies {
  api(project(":pie.api"))
  compile(kotlin("stdlib-jdk8"))
}

tasks.withType<KotlinCompile>().all {
  kotlinOptions.apiVersion = "1.2"
  kotlinOptions.languageVersion = "1.2"
}
