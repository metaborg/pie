plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
}

group = "org.metaborg"

dependencies {
    api(project(":pie.api"))

    compileOnly(libs.checkerframework.android)

    testImplementation(project(":pie.runtime"))
    testImplementation(libs.jimfs)
    testImplementation(libs.junit)
    testCompileOnly(libs.checkerframework.android)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
