plugins {
  `java-platform`
  `maven-publish`
}

val resourceVersion = "0.8.0"

val checkerframeworkVersion = "3.6.0"

val daggerVersion = "2.27"

dependencies {
  constraints {
    // Main
    api("org.metaborg:resource:$resourceVersion")
    api("org.checkerframework:checker-qual-android:$checkerframeworkVersion") // Use android version: annotation retention policy is class instead of runtime.

    // Test
    api("org.junit.jupiter:junit-jupiter-api:5.3.1")
    api("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    api("com.google.jimfs:jimfs:1.1")

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
