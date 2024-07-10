plugins {
    id("org.metaborg.gradle.config.java-library")
}

group = "org.metaborg"

dependencies {
    api(project(":pie.task.java"))

    api(libs.eclipse.jdt.core)
    api(libs.eclipse.jdt.compiler.tool)
    api(libs.eclipse.jdt.compiler.apt)

    compileOnly(libs.checkerframework.android)
}
