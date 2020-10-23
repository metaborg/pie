plugins {
  id("org.metaborg.gradle.config.root-project") version "0.3.21"
  id("org.metaborg.gradle.config.java-application") version "0.3.21"
  id("org.metaborg.gitonium") version "0.1.3"
}

application {
  mainClassName = "org.openjdk.jmh.Main"
  if(org.gradle.internal.jvm.Jvm.current().javaVersion?.isJava9Compatible == true) {
    // Disable illegal reflective access (caused by JMH) warnings on JRE9+.
    applicationDefaultJvmArgs = listOf("--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED", "--add-opens", "java.base/java.io=ALL-UNNAMED")
  }
}

fun compositeBuild(name: String) = "$group:$name:$version"

val jmhVersion = "1.26"
val spoofax3Version = "0.5.1"

dependencies {
  implementation(platform(compositeBuild("pie.depconstraints")))

  implementation("org.openjdk.jmh:jmh-core:$jmhVersion")

  implementation(compositeBuild("pie.runtime"))

  implementation("org.metaborg:spoofax.compiler.spoofax3:$spoofax3Version")
  implementation("org.metaborg:spoofax.compiler.spoofax3.dagger:$spoofax3Version")

  implementation("org.metaborg:log.backend.noop")
  implementation("org.slf4j:slf4j-nop:1.7.30")

  compileOnly("org.checkerframework:checker-qual-android")
  annotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:$jmhVersion")
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
