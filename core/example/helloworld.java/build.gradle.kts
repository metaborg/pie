plugins {
    id("org.metaborg.gradle.config.java-application")
}

application {
    mainClass.set("mb.pie.example.helloworld.java.Main")
}

dependencies {
    implementation(project(":pie.runtime"))
    implementation(project(":pie.taskdefs.guice"))

    compileOnly(libs.checkerframework.android)
}

metaborg {
    javaCreatePublication = false // Do not publish benchmark.
}
tasks {
    // Disable currently unused distribution tasks.
    distZip.configure { enabled = false }
    distTar.configure { enabled = false }
    startScripts.configure { enabled = false }
}
