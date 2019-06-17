plugins {
  id("org.metaborg.spoofax.gradle.langspec") version "develop-SNAPSHOT"
}

spoofax {
  createPublication = false // Disable publications until PIE DSL generates Java code and depends on a stable Spoofax.
}
