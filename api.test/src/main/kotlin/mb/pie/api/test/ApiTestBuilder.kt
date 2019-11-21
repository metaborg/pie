package mb.pie.api.test

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import mb.pie.api.*
import mb.pie.api.stamp.OutputStamper
import mb.pie.api.stamp.ResourceStamper
import mb.pie.api.stamp.output.EqualsOutputStamper
import mb.resource.ReadableResource
import mb.resource.hierarchical.HierarchicalResource
import org.junit.jupiter.api.DynamicTest
import java.nio.file.FileSystem
import java.util.stream.Stream

abstract class ApiTestBuilder<Ctx : ApiTestCtx>(
  val pieBuilderFactory: () -> PieBuilder,
  val loggerFactory: () -> Logger,
  val executorLoggerFactory: (Logger) -> ExecutorLogger,
  defaultResourceStampers: MutableList<ResourceStamper<ReadableResource>>,
  defaultHierarchicalStampers: MutableList<ResourceStamper<HierarchicalResource>>,
  open val testContextFactory: (FileSystem, MapTaskDefs, Pie) -> Ctx
) {
  var filesystemFactory: () -> FileSystem = { Jimfs.newFileSystem(Configuration.unix()) }

  var taskDefsFactory: () -> MapTaskDefs = { MapTaskDefs() }
  val storeFactories: MutableList<(Logger) -> Store> = mutableListOf()
  val shareFactories: MutableList<(Logger) -> Share> = mutableListOf()
  val defaultOutputStampers: MutableList<OutputStamper> = mutableListOf(EqualsOutputStamper())
  val defaultRequireReadableStampers: MutableList<ResourceStamper<ReadableResource>> = defaultResourceStampers
  val defaultProvideReadableStampers: MutableList<ResourceStamper<ReadableResource>> = defaultResourceStampers
  val defaultRequireHierarchicalStampers: MutableList<ResourceStamper<HierarchicalResource>> = defaultHierarchicalStampers
  val defaultProvideHierarchicalStampers: MutableList<ResourceStamper<HierarchicalResource>> = defaultHierarchicalStampers
  val layerFactories: MutableList<(TaskDefs, Logger) -> Layer> = mutableListOf()

  fun test(testFunc: Ctx.() -> Unit): Stream<out DynamicTest> {
    if(storeFactories.isEmpty()) error("Store factories list is empty")
    if(shareFactories.isEmpty()) error("Share factories list is empty")
    if(defaultOutputStampers.isEmpty()) error("Default output stampers list is empty")
    if(defaultRequireReadableStampers.isEmpty()) error("Default readable resource require stampers list is empty")
    if(defaultProvideReadableStampers.isEmpty()) error("Default readable resource provide stampers list is empty")
    if(defaultRequireHierarchicalStampers.isEmpty()) error("Default hierarchical resource require stampers list is empty")
    if(defaultProvideHierarchicalStampers.isEmpty()) error("Default hierarchical resource provide stampers list is empty")
    if(layerFactories.isEmpty()) error("Layer factories list is empty")

    return storeFactories.flatMap { storeFactory ->
      shareFactories.flatMap { shareFactory ->
        defaultOutputStampers.flatMap { defaultOutputStamper ->
          defaultRequireReadableStampers.flatMap { defaultRequireReadableStamper ->
            defaultProvideReadableStampers.flatMap { defaultProvideReadableStamper ->
              defaultRequireHierarchicalStampers.flatMap { defaultRequireHierarchicalStamper ->
                defaultProvideHierarchicalStampers.flatMap { defaultProvideHierarchicalStamper ->
                  layerFactories.map { layerFactory ->
                    val pieBuilder = pieBuilderFactory()
                    val filesystem = filesystemFactory()
                    val taskDefs = taskDefsFactory()
                    pieBuilder.withTaskDefs(taskDefs)
                    pieBuilder.withStore(storeFactory)
                    pieBuilder.withShare(shareFactory)
                    pieBuilder.withDefaultOutputStamper(defaultOutputStamper)
                    pieBuilder.withDefaultRequireReadableResourceStamper(defaultRequireReadableStamper)
                    pieBuilder.withDefaultProvideReadableResourceStamper(defaultProvideReadableStamper)
                    pieBuilder.withDefaultRequireHierarchicalResourceStamper(defaultRequireHierarchicalStamper)
                    pieBuilder.withDefaultProvideHierarchicalResourceStamper(defaultProvideHierarchicalStamper)
                    pieBuilder.withLayer(layerFactory)
                    pieBuilder.withLogger(loggerFactory())
                    pieBuilder.withExecutorLogger(executorLoggerFactory)
                    val pie = pieBuilder.build()
                    DynamicTest.dynamicTest("$pie") {
                      val context = testContextFactory(filesystem, taskDefs, pie)
                      context.testFunc()
                      context.close()
                    }
                  }
                }
              }
            }
          }
        }
      }
    }.stream()
  }
}
