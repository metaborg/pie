// Workaround for issue: https://youtrack.jetbrains.com/issue/KTIJ-19369
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    java
    application
    alias(libs.plugins.kotlin.jvm)
    id("org.metaborg.convention.java")
}

group = "org.metaborg"

application {
    mainClass.set("mb.pie.example.playground.MainKt")
}

dependencies {
    implementation(project(":pie.runtime"))
    implementation(project(":pie.store.lmdb"))
}

tasks {
    // Disable currently unused distribution tasks.
    distZip.configure { enabled = false }
    distTar.configure { enabled = false }
    startScripts.configure { enabled = false }
}
