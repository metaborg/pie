import ru.vyarus.gradle.plugin.python.task.PythonTask

plugins {
    id("org.metaborg.gradle.config.root-project") version "0.5.6"
    id("org.metaborg.gradle.config.java-application") version "0.5.6"
    alias(libs.plugins.gitonium)
    id("ru.vyarus.use-python") version "2.3.0"
}

group = "org.metaborg"

dependencies {
    implementation(platform(libs.metaborg.platform))

    implementation(libs.metaborg.pie.runtime)
    implementation(libs.metaborg.pie.store.lmdb)
    implementation(libs.metaborg.pie.serde.kryo)
    implementation(libs.metaborg.pie.task.archive)
    implementation(libs.metaborg.log.backend.slf4j)
    implementation(libs.spoofax3.lwb.compiler.dagger)

    implementation(libs.jmh.core)
    implementation(libs.jimfs)
    implementation(libs.slf4j.nop)
    //implementation(libs.slf4j.simple)

    compileOnly(libs.checkerframework.android)
    annotationProcessor(libs.jmh.generator.annprocess)
}

application {
    mainClass.set("org.openjdk.jmh.Main")
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

fun runtimeClasspath(project: Project): FileCollection? {
    return project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).runtimeClasspath
}

fun jarsOnlyRuntimeClasspath(project: Project): FileCollection? {
    return project.tasks.getAt(JavaPlugin.JAR_TASK_NAME).outputs.files.plus(project.configurations.getByName(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME))
}

fun registerBenchTask(input: BenchInput): TaskProvider<JavaExec> {
    input.run {
        val task = tasks.register<JavaExec>(name) {
            // Copied from Gradle application plugin
            this.description = description
            group = ApplicationPlugin.APPLICATION_GROUP
            val pluginConvention = project.convention.getPlugin(ApplicationPluginConvention::class.java)
            val javaPluginConvention = project.convention.getPlugin(JavaPluginConvention::class.java)
            val runtimeClasspath: FileCollection = project.files().from(Callable<FileCollection> {
                if(mainModule.isPresent) {
                    jarsOnlyRuntimeClasspath(project)
                } else {
                    runtimeClasspath(project)
                }
            })
            classpath = runtimeClasspath
            mainModule.set(application.getMainModule())
            mainClass.set(application.getMainClass())
//      conventionMapping.map("main") { pluginConvention.mainClassName }
            conventionMapping.map("jvmArgs") { pluginConvention.applicationDefaultJvmArgs }
            // Increase heap size
            minHeapSize = "4G"
            maxHeapSize = "4G"
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
        measurementIterations = 1,
        tracer = "metrics_and_logging"
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
