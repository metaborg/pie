plugins {
    java
    application
    id("org.metaborg.convention.java")
}

group = "org.metaborg"

application {
    mainClass.set("mb.pie.example.helloworld.java.Main")
}

dependencies {
    implementation(project(":pie.runtime"))
    implementation(project(":pie.taskdefs.guice"))

    compileOnly(libs.checkerframework.android)
}

tasks {
    // Disable currently unused distribution tasks.
    distZip.configure { enabled = false }
    distTar.configure { enabled = false }
    startScripts.configure { enabled = false }
}
