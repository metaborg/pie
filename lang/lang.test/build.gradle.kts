plugins {
    id("org.metaborg.gradle.config.java-library")
    id("org.metaborg.gradle.config.junit-testing")
    id("org.metaborg.spoofax.gradle.project")
}

group = "org.metaborg"

spoofaxProject {
    inputIncludePatterns.add("**/*.pie")
    outputIncludePatterns.add("**/*.java")
}

dependencies {
    api(platform(libs.metaborg.platform))
    compileLanguage(libs.metaborg.pie.lang)

    api(libs.dagger)
    annotationProcessor(libs.dagger.compiler)

    compileOnly(libs.checkerframework.android)

    testAnnotationProcessor(libs.dagger.compiler)

    testCompileOnly(libs.checkerframework.android)

    testImplementation(platform(libs.metaborg.resource.api))
    testImplementation(libs.metaborg.pie.runtime)
    testImplementation(libs.metaborg.pie.dagger)

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
