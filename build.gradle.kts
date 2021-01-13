plugins {
  id("org.metaborg.gradle.config.root-project") version "0.4.3"
  id("org.metaborg.gitonium") version "0.1.4"
}

tasks {
  register("benchmark") {
    val includedBuild = gradle.includedBuild("pie.bench")
    dependsOn(includedBuild.task(":runFull"), includedBuild.task(":plotToHtml"))
  }
}
