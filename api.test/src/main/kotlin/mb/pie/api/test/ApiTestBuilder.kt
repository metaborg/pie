package mb.pie.api.test

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import mb.pie.api.*
import mb.pie.api.stamp.OutputStamper
import mb.pie.api.stamp.ResourceStamper
import mb.pie.api.stamp.output.EqualsOutputStamper
import mb.pie.api.stamp.resource.HashMatchResourceStamper
import mb.pie.api.stamp.resource.HashResourceStamper
import mb.pie.api.stamp.resource.ModifiedMatchResourceStamper
import mb.pie.api.stamp.resource.ModifiedResourceStamper
import mb.resource.ReadableResource
import mb.resource.hierarchical.HierarchicalResource
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import java.nio.file.FileSystem
import java.util.stream.Stream

abstract class ApiTestBuilder<Ctx : ApiTestCtx>(
  val pieBuilderFactory: () -> PieBuilder,
  val loggerFactory: () -> Logger,
  val executorLoggerFactory: (Logger) -> ExecutorLogger,
  open val testContextFactory: (FileSystem, MapTaskDefs, Pie) -> Ctx
) {
  var filesystemFactory: () -> FileSystem = { Jimfs.newFileSystem(Configuration.unix()) }

  var taskDefsFactory: () -> MapTaskDefs = { MapTaskDefs() }
  val storeFactories: MutableList<(Logger) -> Store> = mutableListOf()
  val shareFactories: MutableList<(Logger) -> Share> = mutableListOf()
  val defaultOutputStampers: MutableList<OutputStamper> = mutableListOf(EqualsOutputStamper())
  val defaultRequireReadableStampers: MutableList<ResourceStamper<ReadableResource>> = mutableListOf(ModifiedResourceStamper(), HashResourceStamper())
  val defaultProvideReadableStampers: MutableList<ResourceStamper<ReadableResource>> = mutableListOf(ModifiedResourceStamper(), HashResourceStamper())
  val defaultRequireHierarchicalStampers: MutableList<ResourceStamper<HierarchicalResource>> = mutableListOf(ModifiedMatchResourceStamper(), HashMatchResourceStamper())
  val defaultProvideHierarchicalStampers: MutableList<ResourceStamper<HierarchicalResource>> = mutableListOf(ModifiedMatchResourceStamper(), HashMatchResourceStamper())
  val layerFactories: MutableList<(TaskDefs, Logger) -> Layer> = mutableListOf()

  fun build(name: String, testFunc: Ctx.() -> Unit): Stream<out DynamicNode> {
    val tests =
      storeFactories.flatMap { storeFactory ->
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
    return Stream.of(DynamicContainer.dynamicContainer(name, tests))
  }
}
