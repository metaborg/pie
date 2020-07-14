plugins {
  `java-platform`
  `maven-publish`
}

dependencies {
  constraints {
    // Main
    api("org.metaborg:resource:0.7.3")
    api("org.checkerframework:checker-qual-android:3.0.0") // Use android version: annotation retention policy is class instead of runtime.

    // Test
    api("org.junit.jupiter:junit-jupiter-api:5.2.0")
    api("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    api("com.google.jimfs:jimfs:1.1")

    val daggerVersion = "2.25.2"
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
