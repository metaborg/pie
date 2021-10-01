plugins {
  `java-platform`
  `maven-publish`
}

val logVersion = "0.5.5"
val resourceVersion = "0.11.5"
val commonVersion = "0.9.7"

val checkerframeworkVersion = "3.16.0"

val immutablesVersion = "2.8.2"
val daggerVersion = "2.36" // Do not upgrade, causes Gradle/Kotlin compatibility issues due to upgrade to Kotlin 1.5.

dependencies {
  constraints {
    // Main
    api("org.metaborg:log.api:$logVersion")
    api("org.metaborg:log.dagger:$logVersion")
    api("org.metaborg:resource:$resourceVersion")
    api("org.metaborg:resource.dagger:$resourceVersion")
    api("org.metaborg:common:$commonVersion")
    api("org.checkerframework:checker-qual-android:$checkerframeworkVersion") // Use android version: annotation retention policy is class instead of runtime.

    // Test
    api("org.junit.jupiter:junit-jupiter-api:5.3.1")
    api("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    api("com.google.jimfs:jimfs:1.1")

    // Annotations
    api("javax.inject:javax.inject:1")
    api("org.immutables:value:$immutablesVersion")
    api("org.immutables:value-annotations:$immutablesVersion")
    api("com.google.dagger:dagger:$daggerVersion")
    api("com.google.dagger:dagger-compiler:$daggerVersion")
  }
}

publishing {
  publications {
    create<MavenPublication>("JavaPlatform") {
      from(components["javaPlatform"])
    }
  }
}
