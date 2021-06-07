import org.slf4j.LoggerFactory
import ru.vyarus.gradle.plugin.python.task.PythonTask

plugins {
  id("org.metaborg.gradle.config.root-project") version "0.4.4"
  id("org.metaborg.gradle.config.java-application") version "0.4.4"
  id("org.metaborg.gitonium") version "0.1.4"
  id("ru.vyarus.use-python") version "2.2.0"
}

// JMH application configuration and benchmarking tasks.
dependencies {
  val jmhVersion = "1.26"
  val spoofax3Version = "0.8.0"
  fun compositeBuild(name: String) = "$group:$name:$version"

  implementation(platform(compositeBuild("pie.depconstraints")))

  implementation("org.openjdk.jmh:jmh-core:$jmhVersion")

  implementation(compositeBuild("pie.runtime"))
  implementation(compositeBuild("pie.store.lmdb"))
  implementation(compositeBuild("pie.serde.kryo"))
  implementation(compositeBuild("pie.task.archive"))

  implementation("org.metaborg:spoofax.lwb.compiler.dagger:$spoofax3Version")

  implementation("com.google.jimfs:jimfs")
  implementation("org.metaborg:log.backend.slf4j")
  implementation("org.slf4j:slf4j-nop:1.7.30")
  //implementation("org.slf4j:slf4j-simple:1.7.30")

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

val jmhReportDir = "$buildDir/reports/jmh/"
val resultFile = "$jmhReportDir/result.json"

val benchBottomUpScheduling = registerRunTask(
  "benchBottomUpSchedulingDev",
  benchmarkRegex = "BottomUpSchedulingBench.*",
  forking = false,
  warmupIterations = 0,
  measurementIterations = 1,
  loggerFactory = "stdout_very_verbose",
  tracer = "metrics",
  additionalArgs = listOf("-p", "useDiskTemporaryDirectory=false")
)

val spoofax3CompilerBenchmarkRegex = "Spoofax3CompilerBench.*"
val spoofax3CompilerLayers = listOf("validation", "noop")
val spoofax3CompilerAdditionalArgs = listOf("-p", "language=${listOf("chars", "calc").joinToString(",")}")
val benchSpoofax3CompilerDev = registerRunTask(
  "benchSpoofax3CompilerDev",
  benchmarkRegex = spoofax3CompilerBenchmarkRegex,
  forking = false,
  warmupIterations = 0,
  measurementIterations = 1,
  layers = spoofax3CompilerLayers,
  additionalArgs = spoofax3CompilerAdditionalArgs
)
val benchSpoofax3CompilerFull = registerRunTask(
  "benchSpoofax3CompilerFull",
  benchmarkRegex = spoofax3CompilerBenchmarkRegex,
  layers = spoofax3CompilerLayers,
  additionalArgs = spoofax3CompilerAdditionalArgs
)

// Python configuration and plotting tasks.
python {
  pip("pip:20.2.4")
  pip("plotly:4.12.0")
  pip("pandas:1.1.3")
  pip("dash:1.16.3")
  pip("dash-bootstrap-components:0.10.7")
}
tasks.register<PythonTask>("plotToHtml") {
  mustRunAfter(benchSpoofax3CompilerDev, benchSpoofax3CompilerFull)
  command = "src/main/python/plot.py --input-file $resultFile export-html --output-file $jmhReportDir/result.html"
}
tasks.register<PythonTask>("plotInteractive") {
  mustRunAfter(benchSpoofax3CompilerDev, benchSpoofax3CompilerFull)
  command = "src/main/python/plot.py --input-file $resultFile dash"
}


fun registerRunTask(
  name: String,
  benchmarkRegex: String = "*",
  additionalArgs: List<String> = listOf(),
  description: String = "Runs benchmarks with certain settings",
  forking: Boolean = true,
  warmupIterations: Int = 5,
  measurementIterations: Int = 5,
  loggerFactory: String = "stdout_verbose",
  serdes: List<String> = listOf("java"),
  stores: List<String> = listOf("in_memory"),
  layers: List<String> = listOf("validation"),
  tracer: String = "metrics"
): TaskProvider<JavaExec> {
  return tasks.register<JavaExec>(name) {
    // Copied from Gradle application plugin
    this.description = description
    group = ApplicationPlugin.APPLICATION_GROUP
    val pluginConvention = project.convention.getPlugin(ApplicationPluginConvention::class.java)
    val javaPluginConvention = project.convention.getPlugin(JavaPluginConvention::class.java)
    classpath = javaPluginConvention.sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).runtimeClasspath
    conventionMapping.map("main") { pluginConvention.mainClassName }
    conventionMapping.map("jvmArgs") { pluginConvention.applicationDefaultJvmArgs }

    args("-f", if(forking) "1" else "0")
    args("-wi", warmupIterations, "-i", measurementIterations)
    args("-p", "loggerFactory=$loggerFactory")
    args("-p", "serde=${serdes.joinToString(",")}")
    args("-p", "store=${stores.joinToString(",")}")
    args("-p", "layer=${layers.joinToString(",")}")
    args("-p", "tracer=$tracer")
    args(
      "-foe", "true", // Fail early.
      "-gc", "true", // Run GC between iterations, lowering noise.
      "-prof", "mb.pie.bench.util.PieMetricsProfiler", // Enable PIE metrics profiler; required.
      "-rf", "json", "-rff", resultFile // Write results to JSON file.
    )
    args(additionalArgs)
    args(benchmarkRegex)
    doFirst {
      mkdir(jmhReportDir)
    }
  }
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
