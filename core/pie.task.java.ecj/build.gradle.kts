plugins {
    `java-library`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
}

group = "org.metaborg"

dependencies {
    api(project(":pie.task.java"))

    api(libs.eclipse.jdt.core)
    api(libs.eclipse.jdt.compiler.tool)
    api(libs.eclipse.jdt.compiler.apt)

    compileOnly(libs.checkerframework.android)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
