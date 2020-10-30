plugins {
  id("org.metaborg.gradle.config.root-project") version "0.3.21"
  id("org.metaborg.gitonium") version "0.1.3"
}

tasks {
  register("benchmark") {
    val includedBuild = gradle.includedBuild("pie.bench")
    dependsOn(includedBuild.task(":runFull"), includedBuild.task(":plotToHtml"))
  }
}
