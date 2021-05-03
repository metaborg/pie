plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(platform(project(":pie.depconstraints")))

  api(project(":pie.task.java"))

  api("org.eclipse.jdt:org.eclipse.jdt.compiler.tool:1.2.1100")
  api("org.eclipse.jdt:org.eclipse.jdt.compiler.apt:1.3.1200")
  api("org.eclipse.jdt:org.eclipse.jdt.core:3.25.0")
}
