package mb.pie.api.test

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import mb.pie.api.*
import mb.pie.api.stamp.OutputStamper
import mb.pie.api.stamp.ResourceStamper
import mb.pie.api.stamp.output.EqualsOutputStamper
import mb.pie.api.stamp.resource.HashMatchResourceStamper
import mb.pie.api.stamp.resource.ModifiedMatchResourceStamper
import mb.resource.hierarchical.HierarchicalResource
import org.junit.jupiter.api.*
import java.nio.file.FileSystem
import java.util.stream.Stream

object ApiTestGenerator {
  val defaultDefaultOutputStampers: Array<OutputStamper> = arrayOf(EqualsOutputStamper())
  val defaultDefaultRequireHierarchicalStampers: Array<ResourceStamper<HierarchicalResource>> = arrayOf(ModifiedMatchResourceStamper(), HashMatchResourceStamper())
  val defaultDefaultProvideHierarchicalStampers: Array<ResourceStamper<HierarchicalResource>> = arrayOf(ModifiedMatchResourceStamper(), HashMatchResourceStamper())

  fun <Ctx : ApiTestCtx> generate(
    name: String,
    pieBuilderGen: () -> PieBuilder,
    taskDefsGen: () -> TaskDefs,
    storeGens: Array<(Logger) -> Store>,
    shareGens: Array<(Logger) -> Share>,
    layerGens: Array<(TaskDefs, Logger) -> Layer>,
    defaultOutputStampers: Array<OutputStamper> = defaultDefaultOutputStampers,
    defaultRequireFileSystemStampers: Array<ResourceStamper<HierarchicalResource>> = defaultDefaultRequireHierarchicalStampers,
    defaultProvideFileSystemStampers: Array<ResourceStamper<HierarchicalResource>> = defaultDefaultProvideHierarchicalStampers,
    executorLoggerGen: (Logger) -> ExecutorLogger,
    logger: Logger,
    testCtxGen: (Pie, TaskDefs, FileSystem) -> Ctx,
    testFunc: Ctx.() -> Unit
  ): Stream<out DynamicNode> {
    val javaFSGen = { Jimfs.newFileSystem(Configuration.unix()) }
    val tests =
      storeGens.flatMap { storeGen ->
        shareGens.flatMap { shareGen ->
          layerGens.flatMap { layerGen ->
            defaultOutputStampers.flatMap { defaultOutputStamper ->
              defaultRequireFileSystemStampers.flatMap { defaultFileReqStamper ->
                defaultProvideFileSystemStampers.map { defaultFileGenStamper ->
                  val javaFs = javaFSGen()
                  val taskDefs = taskDefsGen()
                  val pieBuilder = pieBuilderGen()
                    .withTaskDefs(taskDefs)
                    .withStore(storeGen)
                    .withShare(shareGen)
                    .withDefaultOutputStamper(defaultOutputStamper)
                    .withDefaultRequireHierarchicalResourceStamper(defaultFileReqStamper)
                    .withDefaultProvideHierarchicalResourceStamper(defaultFileGenStamper)
                    .withLayer(layerGen)
                    .withLogger(logger)
                    .withExecutorLogger(executorLoggerGen)
                  val pie = pieBuilder.build()
                  DynamicTest.dynamicTest("$pie") {
                    val context = testCtxGen(pie, taskDefs, javaFs)
                    context.testFunc()
                    context.close()
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
