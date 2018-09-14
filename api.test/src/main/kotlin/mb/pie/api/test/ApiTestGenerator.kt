package mb.pie.api.test

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import mb.pie.api.*
import mb.pie.api.stamp.FileStamper
import mb.pie.api.stamp.OutputStamper
import mb.pie.api.stamp.output.EqualsOutputStamper
import mb.pie.api.stamp.path.HashFileStamper
import mb.pie.api.stamp.path.ModifiedFileStamper
import org.junit.jupiter.api.*
import java.nio.file.FileSystem
import java.util.stream.Stream

object ApiTestGenerator {
  fun <Ctx : ApiTestCtx> generate(
    name: String,
    pieBuilderGen: () -> PieBuilder,
    taskDefsGen: () -> TaskDefs,
    storeGens: Array<(Logger) -> Store>,
    shareGens: Array<(Logger) -> Share>,
    layerGens: Array<(Logger) -> Layer>,
    defaultOutputStampers: Array<OutputStamper> = arrayOf(EqualsOutputStamper()),
    defaultFileReqStampers: Array<FileStamper> = arrayOf(ModifiedFileStamper(), HashFileStamper()),
    defaultFileGenStampers: Array<FileStamper> = arrayOf(ModifiedFileStamper(), HashFileStamper()),
    executorLoggerGen: (Logger) -> ExecutorLogger,
    logger: Logger,
    testCtxGen: (Pie, TaskDefs, FileSystem) -> Ctx,
    testFunc: Ctx.() -> Unit
  ): Stream<out DynamicNode> {
    val fsGen = { Jimfs.newFileSystem(Configuration.unix()) }
    val tests =
      storeGens.flatMap { storeGen ->
        shareGens.flatMap { shareGen ->
          layerGens.flatMap { layerGen ->
            defaultOutputStampers.flatMap { defaultOutputStamper ->
              defaultFileReqStampers.flatMap { defaultFileReqStamper ->
                defaultFileGenStampers.map { defaultFileGenStamper ->
                  val fs = fsGen()
                  val taskDefs = taskDefsGen()
                  val pieBuilder = pieBuilderGen()
                    .withTaskDefs(taskDefs)
                    .withStore(storeGen)
                    .withShare(shareGen)
                    .withDefaultOutputStamper(defaultOutputStamper)
                    .withDefaultFileReqStamper(defaultFileReqStamper)
                    .withDefaultFileGenStamper(defaultFileGenStamper)
                    .withLayer(layerGen)
                    .withLogger(logger)
                    .withExecutorLogger(executorLoggerGen)
                  val pie = pieBuilder.build()
                  DynamicTest.dynamicTest("$pie", {
                    val context = testCtxGen(pie, taskDefs, fs)
                    context.testFunc()
                    context.close()
                  })
                }
              }
            }
          }

        }
      }.stream()
    return Stream.of(DynamicContainer.dynamicContainer(name, tests))
  }
}
