package mb.pie.runtime.test

import mb.pie.api.*
import mb.pie.api.fs.stamp.*
import mb.pie.api.stamp.OutputStamper
import mb.pie.api.stamp.output.EqualsOutputStamper
import mb.pie.api.test.ApiTestGenerator
import mb.pie.runtime.PieBuilderImpl
import mb.pie.runtime.PieImpl
import mb.pie.runtime.layer.ValidationLayer
import mb.pie.runtime.logger.StreamLogger
import mb.pie.runtime.logger.exec.LoggerExecutorLogger
import mb.pie.runtime.share.NonSharingShare
import mb.pie.runtime.store.InMemoryStore
import mb.pie.runtime.taskdefs.MutableMapTaskDefs
import org.junit.jupiter.api.DynamicNode
import java.util.stream.Stream

object RuntimeTestGenerator {
  fun generate(
    name: String,
    storeGens: Array<(Logger) -> Store> = arrayOf({ _ -> InMemoryStore() }),
    shareGens: Array<(Logger) -> Share> = arrayOf({ _ -> NonSharingShare() }),
    layerGens: Array<(Logger) -> Layer> = arrayOf({ l -> ValidationLayer(l) }),
    defaultOutputStampers: Array<OutputStamper> = arrayOf(EqualsOutputStamper()),
    defaultRequireFileSystemStampers: Array<FileSystemStamper> = arrayOf(ModifiedResourceStamper(), HashResourceStamper()),
    defaultProvideFileSystemStampers: Array<FileSystemStamper> = arrayOf(ModifiedResourceStamper(), HashResourceStamper()),
    executorLoggerGen: (Logger) -> ExecutorLogger = { l -> LoggerExecutorLogger(l) },
    logger: Logger = StreamLogger.only_errors(),
    testFunc: RuntimeTestCtx.() -> Unit
  ): Stream<out DynamicNode> {
    return ApiTestGenerator.generate(
      name,
      { PieBuilderImpl() },
      { MutableMapTaskDefs() },
      storeGens,
      shareGens,
      layerGens,
      defaultOutputStampers,
      defaultRequireFileSystemStampers,
      defaultProvideFileSystemStampers,
      executorLoggerGen,
      logger,
      { pie, taskDefs, fs -> RuntimeTestCtx(pie as PieImpl, taskDefs as MutableMapTaskDefs, fs) },
      testFunc
    )
  }
}
