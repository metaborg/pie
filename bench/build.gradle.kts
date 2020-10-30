import ru.vyarus.gradle.plugin.python.task.PythonTask

plugins {
  id("org.metaborg.gradle.config.root-project") version "0.3.21"
  id("org.metaborg.gradle.config.java-application") version "0.3.21"
  id("org.metaborg.gitonium") version "0.1.3"
  id("ru.vyarus.use-python") version "2.2.0"
}

// JMH application configuration and benchmarking tasks.
dependencies {
  val jmhVersion = "1.26"
  val spoofax3Version = "0.5.2"
  fun compositeBuild(name: String) = "$group:$name:$version"

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

application {
  mainClassName = "org.openjdk.jmh.Main"
  if(org.gradle.internal.jvm.Jvm.current().javaVersion?.isJava9Compatible == true) {
    // Disable illegal reflective access (caused by JMH) warnings on JRE9+.
    applicationDefaultJvmArgs = listOf("--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED", "--add-opens", "java.base/java.io=ALL-UNNAMED")
  }
}

val reportDir = "$buildDir/reports/jmh/"
val reportFile = "$reportDir/result.json"
val commonArgs = listOf(
  "-foe", "true", // Fail early.
  "-gc", "true", // Run GC between iterations, lowering noise.
  "-prof", "mb.pie.bench.util.PieMetricsProfiler", // Enable PIE metrics profiler; required.
  "-rf", "json", "-rff", reportFile, // Write results to JSON.
  "Spoofax3Bench" // Benchmarks to run.
)
// Development settings
val runTask = tasks.getByName<JavaExec>("run") {
  description = "Runs benchmarks with quick development settings"
  args("-f", "0") // Do not fork to allow debugging.
  args("-wi", "3", "-i", "3") // 3 warmup and measurement iterations.
  args("-p language=chars") // Only benchmark with minimal 'chars' language.
  args(commonArgs)
  doFirst {
    mkdir(reportDir)
  }
}
// Full benchmarking settings
val layers = listOf("validation", "noop")
val languages = listOf("chars", "calc")
val runFullTask = tasks.register<JavaExec>("runFull") {
  // Copied from Gradle application plugin
  description = "Runs benchmarks with full benchmarking settings"
  group = ApplicationPlugin.APPLICATION_GROUP
  val pluginConvention = project.convention.getPlugin(ApplicationPluginConvention::class.java)
  val javaPluginConvention = project.convention.getPlugin(JavaPluginConvention::class.java)
  classpath = javaPluginConvention.sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).runtimeClasspath
  conventionMapping.map("main") { pluginConvention.mainClassName }
  conventionMapping.map("jvmArgs") { pluginConvention.applicationDefaultJvmArgs }

  args("-f", "1") // Enable forking.
  args("-wi", "5", "-i", "5") // 5 warmup and measurement iterations.
  args("-p layer=${layers.joinToString(",")}") // Benchmark with different layers.
  args("-p language=${languages.joinToString(",")}") // Benchmark with different languages.
  args(commonArgs)
  doFirst {
    mkdir(reportDir)
  }
}

// Python configuration and plotting tasks.
python {
  pip("pip:20.2.4")
  pip("plotly:4.12.0")
  pip("pandas:1.1.3")
  pip("dash:1.16.3")
  pip("dash-bootstrap-components:0.10.7")
}
tasks.register<PythonTask>("plotToHtml") {
  mustRunAfter(runTask, runFullTask)
  command = "src/main/python/plot.py --input-file $reportFile export-html --output-file $reportDir/result.html"
}
tasks.register<PythonTask>("plotInteractive") {
  mustRunAfter(runTask, runFullTask)
  command = "src/main/python/plot.py --input-file $reportFile dash"
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
