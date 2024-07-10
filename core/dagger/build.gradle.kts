plugins {
    id("org.metaborg.gradle.config.java-library")
    id("org.metaborg.gradle.config.junit-testing")
}

group = "org.metaborg"

dependencies {
    api(platform(project(":pie.depconstraints")))
    annotationProcessor(platform(project(":pie.depconstraints")))
    testAnnotationProcessor(platform(project(":pie.depconstraints")))

    api(project(":pie.api"))
    api("org.metaborg:log.dagger")
    api("org.metaborg:resource.dagger")
    api("com.google.dagger:dagger")

    annotationProcessor("com.google.dagger:dagger-compiler")
    compileOnly("org.checkerframework:checker-qual-android")

    testImplementation(project(":pie.runtime"))
    testAnnotationProcessor("com.google.dagger:dagger-compiler")
}
