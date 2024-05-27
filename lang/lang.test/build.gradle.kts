plugins {
    id("org.metaborg.gradle.config.java-library")
    id("org.metaborg.gradle.config.junit-testing")
    id("org.metaborg.spoofax.gradle.project")
}

spoofaxProject {
    inputIncludePatterns.add("**/*.pie")
    outputIncludePatterns.add("**/*.java")
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
    api(platform(compositeBuild("pie.depconstraints")))
    annotationProcessor(platform(compositeBuild("pie.depconstraints")))
    testAnnotationProcessor(platform(compositeBuild("pie.depconstraints")))

    api("com.google.dagger:dagger")
    annotationProcessor("com.google.dagger:dagger-compiler")

    compileLanguage(compositeBuild("pie.lang"))

    compileOnly("org.checkerframework:checker-qual-android")

    testAnnotationProcessor("com.google.dagger:dagger-compiler")

    testCompileOnly("org.checkerframework:checker-qual-android")

    testImplementation("org.metaborg:resource")
    testImplementation(compositeBuild("pie.runtime"))
    testImplementation(compositeBuild("pie.dagger"))
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
