plugins {
  id("org.metaborg.spoofax.gradle.langspec") version "0.1.6"
}

spoofax {
  createPublication = false // Disable publications until PIE DSL generates Java code and depends on a stable Spoofax.
}