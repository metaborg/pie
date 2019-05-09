package mb.pie.runtime.test

import mb.pie.api.*
import mb.pie.api.stamp.OutputStamper
import mb.pie.api.stamp.ResourceStamper
import mb.pie.api.stamp.output.EqualsOutputStamper
import mb.pie.api.stamp.resource.HashMatchResourceStamper
import mb.pie.api.stamp.resource.ModifiedMatchResourceStamper
import mb.pie.api.test.ApiTestGenerator
import mb.pie.runtime.PieBuilderImpl
import mb.pie.runtime.PieImpl
import mb.pie.runtime.layer.ValidationLayer
import mb.pie.runtime.logger.StreamLogger
import mb.pie.runtime.logger.exec.LoggerExecutorLogger
import mb.pie.runtime.share.NonSharingShare
import mb.pie.runtime.store.InMemoryStore
import mb.pie.api.MapTaskDefs
import mb.resource.fs.FSResource
import org.junit.jupiter.api.DynamicNode
import java.util.stream.Stream

object RuntimeTestGenerator {
  fun generate(
    name: String,
    storeGens: Array<(Logger) -> Store> = arrayOf({ _ -> InMemoryStore() }),
    shareGens: Array<(Logger) -> Share> = arrayOf({ _ -> NonSharingShare() }),
    layerGens: Array<(TaskDefs, Logger) -> Layer> = arrayOf({ td, l -> ValidationLayer(td, l) }),
    defaultOutputStampers: Array<OutputStamper> = arrayOf(EqualsOutputStamper()),
    defaultRequireFileSystemStampers: Array<ResourceStamper<FSResource>> = arrayOf(ModifiedMatchResourceStamper(), HashMatchResourceStamper()),
    defaultProvideFileSystemStampers: Array<ResourceStamper<FSResource>> = arrayOf(ModifiedMatchResourceStamper(), HashMatchResourceStamper()),
    executorLoggerGen: (Logger) -> ExecutorLogger = { l -> LoggerExecutorLogger(l) },
    logger: Logger = StreamLogger.onlyErrors(),
    testFunc: RuntimeTestCtx.() -> Unit
  ): Stream<out DynamicNode> {
    return ApiTestGenerator.generate(
      name,
      { PieBuilderImpl() },
      { MapTaskDefs() },
      storeGens,
      shareGens,
      layerGens,
      defaultOutputStampers,
      defaultRequireFileSystemStampers,
      defaultProvideFileSystemStampers,
      executorLoggerGen,
      logger,
      { pie, taskDefs, fs -> RuntimeTestCtx(pie as PieImpl, taskDefs as MapTaskDefs, fs) },
      testFunc
    )
  }
}
