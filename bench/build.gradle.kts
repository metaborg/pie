import ru.vyarus.gradle.plugin.python.task.PythonTask

plugins {
  id("org.metaborg.gradle.config.root-project") version "0.4.4"
  id("org.metaborg.gradle.config.java-application") version "0.4.4"
  id("org.metaborg.gitonium") version "0.1.4"
  id("ru.vyarus.use-python") version "2.3.0"
}

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


// Benchmark tasks
val jmhReportDir = "$buildDir/reports/jmh"
val resultFile = "$jmhReportDir/result.json"
val benchTasks = mutableListOf<TaskProvider<JavaExec>>()
registerBenchTasks(BenchInput(
  "benchBottomUpScheduling",
  benchmarkRegex = "BottomUpSchedulingBench.*",
  layers = listOf("noop"),
  stores = listOf("in_memory", "in_memory_naive"),
  additionalArgs = listOf("-p", "useDiskTemporaryDirectory=false")
))
registerBenchTasks(BenchInput(
  "benchSpoofax3Compiler",
  benchmarkRegex = "Spoofax3CompilerBench.*",
  layers = listOf("validation", "noop"),
  additionalArgs = listOf("-p", "language=${listOf("chars", "calc").joinToString(",")}")
))


// Helper function to register a benchmark task
data class BenchInput(
  val name: String,
  val benchmarkRegex: String = "*",
  val additionalArgs: List<String> = listOf(),
  val description: String = "Runs benchmarks with certain settings",
  val forking: Boolean = true,
  val warmupIterations: Int = 5,
  val measurementIterations: Int = 5,
  val loggerFactory: String = "stdout_verbose",
  val serdes: List<String> = listOf("java"),
  val stores: List<String> = listOf("in_memory"),
  val layers: List<String> = listOf("validation"),
  val tracer: String = "metrics"
)

fun registerBenchTask(input: BenchInput): TaskProvider<JavaExec> {
  input.run {
    val task = tasks.register<JavaExec>(name) {
      // Copied from Gradle application plugin
      this.description = description
      group = ApplicationPlugin.APPLICATION_GROUP
      val pluginConvention = project.convention.getPlugin(ApplicationPluginConvention::class.java)
      val javaPluginConvention = project.convention.getPlugin(JavaPluginConvention::class.java)
      classpath = javaPluginConvention.sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).runtimeClasspath
      conventionMapping.map("main") { pluginConvention.mainClassName }
      conventionMapping.map("jvmArgs") { pluginConvention.applicationDefaultJvmArgs }
      // Increase heap size
      minHeapSize="4G"
      maxHeapSize="4G"
      // Arguments
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
    benchTasks.add(task)
    return task
  }
}

fun registerBenchTasks(input: BenchInput) {
  registerBenchTask(input.copy(
    name = "${input.name}Dev",
    forking = false,
    warmupIterations = 0,
    measurementIterations = 1
  ))
  registerBenchTask(input.copy(
    name = "${input.name}Fast",
    forking = true,
    warmupIterations = 2,
    measurementIterations = 3
  ))
  registerBenchTask(input.copy(
    name = "${input.name}Full",
    forking = true,
    warmupIterations = 5,
    measurementIterations = 5
  ))
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
  mustRunAfter(benchTasks)
  command = "src/main/python/plot.py --input-file $resultFile export-html --output-file $jmhReportDir/result.html"
}
tasks.register<PythonTask>("plotInteractive") {
  mustRunAfter(benchTasks)
  command = "src/main/python/plot.py --input-file $resultFile dash"
}


// Disable publishing and other unnecessary tasks.
metaborg {
  javaCreatePublication = false // Do not publish benchmark.
}
tasks {
  // Disable currently unused distribution tasks.
  distZip.configure { enabled = false }
  distTar.configure { enabled = false }
  startScripts.configure { enabled = false }
}
