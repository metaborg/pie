plugins {
    id("org.metaborg.gradle.config.java-library")
    id("org.metaborg.gradle.config.junit-testing")
}

group = "org.metaborg"

dependencies {
    api(platform(libs.metaborg.platform))
    api(libs.metaborg.log.dagger)
    api(libs.metaborg.resource.dagger)

    api(project(":pie.api"))
    api(libs.dagger)

    annotationProcessor(libs.dagger.compiler)
    compileOnly(libs.checkerframework.android)

    testImplementation(project(":pie.runtime"))
    testAnnotationProcessor(libs.dagger.compiler)
}
