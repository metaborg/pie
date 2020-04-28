plugins {
  id("org.metaborg.spoofax.gradle.langspec")
}

spoofax {
  metaborgVersion = "2.5.8"
  createPublication = false // Disable publications until PIE DSL generates Java code and depends on a stable Spoofax.
}
