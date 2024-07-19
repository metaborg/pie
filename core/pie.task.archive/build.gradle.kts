plugins {
    `java-library`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.convention.junit")
}

group = "org.metaborg"

dependencies {
    api(project(":pie.api"))

    compileOnly(libs.checkerframework.android)

    testImplementation(project(":pie.runtime"))
    testImplementation(libs.jimfs)
    testCompileOnly(libs.checkerframework.android)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
