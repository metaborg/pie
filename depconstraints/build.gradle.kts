plugins {
  `java-platform`
}

dependencies {
  constraints {
    // Main
    api("org.metaborg:resource:0.2.0")
    api("org.checkerframework:checker-qual-android:2.6.0") // Use android version: annotation retention policy is class instead of runtime.

    // Test
    api("org.junit.jupiter:junit-jupiter-api:5.2.0")
    api("com.nhaarman.mockitokotlin2:mockito-kotlin:2.1.0")
    api("com.google.jimfs:jimfs:1.1")
  }
}
