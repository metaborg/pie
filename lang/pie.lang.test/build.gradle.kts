plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.devenv.spoofax.gradle.project")
}

group = "org.metaborg"

spoofaxProject {
    inputIncludePatterns.add("**/*.pie")
    outputIncludePatterns.add("**/*.java")

    // We add the dependency manually and don't change the repositories
    // Eventually, this functionality should be removed from spoofax.gradle
    addSpoofaxCoreDependency.set(false)
    addSpoofaxRepository.set(false)
}

dependencies {
    api(platform(libs.metaborg.platform))
    compileLanguage(libs.metaborg.pie.lang)

    api(libs.dagger)
    annotationProcessor(libs.dagger.compiler)

    compileOnly(libs.checkerframework.android)
    compileOnly(libs.spoofax2.core)

    testAnnotationProcessor(libs.dagger.compiler)

    testCompileOnly(libs.checkerframework.android)

    testImplementation(libs.metaborg.resource.api)
    testImplementation(libs.metaborg.pie.runtime)
    testImplementation(libs.metaborg.pie.dagger)
    testImplementation(libs.junit)

    testImplementation(project(":pie.lang.runtime.java"))
}

val pieGenSourcesDir = "build/generated/piesources/"

sourceSets {
    test {
        java {
            srcDir(pieGenSourcesDir)
        }
    }
}

afterEvaluate {
    tasks.named("spoofaxBuild").configure {
        doFirst {
            project.file(pieGenSourcesDir).mkdirs()
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
