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

val reportDir = "$buildDir/reports/jmh/"
val reportFile = "$buildDir/reports/jmh/result.json"
val pieMetricsProfiler = "mb.pie.bench.util.PieMetricsProfiler"
val benchmarkRegex = "Spoofax3Bench"
// Development settings
tasks.getByName<JavaExec>("run") {
  description = "Runs benchmarks with quick development settings"

  args("-f", "0") // Do not fork to allow debugging.
  args("-foe", "true") // Fail early.
  args("-gc", "true") // Run GC between iterations, lowering noise.
  args("-wi", "1", "-i", "1") // Only one warmup and measuring iteration.
  args("-prof", pieMetricsProfiler) // Enable PIE metrics profiler; required.
  args("-rf", "json", "-rff", reportFile) // Write results to JSON
  args(benchmarkRegex)

  doFirst {
    mkdir(reportDir)
  }
}
// Full benchmarking settings
tasks.register<JavaExec>("runFull") {
  // Copied from Gradle application plugin
  description = "Runs benchmarks with full benchmarking settings"
  group = ApplicationPlugin.APPLICATION_GROUP
  val pluginConvention = project.convention.getPlugin(ApplicationPluginConvention::class.java)
  val javaPluginConvention = project.convention.getPlugin(JavaPluginConvention::class.java)
  classpath = javaPluginConvention.sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).runtimeClasspath
  conventionMapping.map("main") { pluginConvention.mainClassName }
  conventionMapping.map("jvmArgs") { pluginConvention.applicationDefaultJvmArgs }

  args("-f", "1") // Enable forking.
  args("-foe", "true") // Fail early.
  args("-gc", "true") // Run GC between iterations, lowering noise.
  args("-wi", "5", "-i", "5") // 5 warmup and 5 measurement iterations
  args("-prof", pieMetricsProfiler) // Enable PIE metrics profiler; required.
  args("-rf", "json", "-rff", reportFile) // Write results to JSON
  args(benchmarkRegex)

  doFirst {
    mkdir(reportDir)
  }
}

fun compositeBuild(name: String) = "$group:$name:$version"

val jmhVersion = "1.26"
val spoofax3Version = "0.5.2"

dependencies {
  implementation(platform(compositeBuild("pie.depconstraints")))

  implementation("org.openjdk.jmh:jmh-core:$jmhVersion")

  implementation(compositeBuild("pie.runtime"))
  implementation(compositeBuild("pie.task.archive"))

  implementation("org.metaborg:spoofax.compiler.spoofax3:$spoofax3Version")
  implementation("org.metaborg:spoofax.compiler.spoofax3.dagger:$spoofax3Version")

  implementation("com.google.jimfs:jimfs")
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
