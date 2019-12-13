plugins {
  id("org.metaborg.spoofax.gradle.langspec")
}

spoofax {
  createPublication = false // Disable publications until PIE DSL generates Java code and depends on a stable Spoofax.
}
