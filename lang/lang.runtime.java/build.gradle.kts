plugins {
  id("org.metaborg.gradle.config.java-library")
}

fun compositeBuild(name: String) = "$group:$name"

dependencies {
  api(platform(compositeBuild("pie.depconstraints")))

  api(compositeBuild("pie.api"))

  compileOnly("org.checkerframework:checker-qual-android")
}
