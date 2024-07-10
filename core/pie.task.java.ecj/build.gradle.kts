plugins {
    `java-library`
    `maven-publish`
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

mavenPublishConvention {
    repoOwner.set("metaborg")
    repoName.set("pie")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
