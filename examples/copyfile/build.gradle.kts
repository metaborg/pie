import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("org.metaborg.gradle.config.kotlin-application")
}

dependencies {
  api(project(":pie.runtime"))
  api(project(":pie.store.lmdb"))
  implementation(kotlin("stdlib-jdk8"))
}

tasks.withType<KotlinCompile>().all {
  kotlinOptions.apiVersion = "1.2"
  kotlinOptions.languageVersion = "1.2"
}
