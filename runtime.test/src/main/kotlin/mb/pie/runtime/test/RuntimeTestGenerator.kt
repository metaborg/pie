package mb.pie.runtime.test

import mb.pie.api.*
import mb.pie.api.stamp.OutputStamper
import mb.pie.api.stamp.ResourceStamper
import mb.pie.api.test.ApiTestGenerator
import mb.pie.runtime.PieBuilderImpl
import mb.pie.runtime.PieImpl
import mb.pie.runtime.layer.ValidationLayer
import mb.pie.runtime.logger.StreamLogger
import mb.pie.runtime.logger.exec.LoggerExecutorLogger
import mb.pie.runtime.share.NonSharingShare
import mb.pie.runtime.store.InMemoryStore
import mb.resource.fs.FSResource
import mb.resource.hierarchical.HierarchicalResource
import org.junit.jupiter.api.DynamicNode
import java.util.stream.Stream

object RuntimeTestGenerator {
  val defaultStoreGens: Array<(Logger) -> Store> = arrayOf({ _ -> InMemoryStore() })
  val defaultShareGens: Array<(Logger) -> Share> = arrayOf({ _ -> NonSharingShare() })
  val defaultLayerGens: Array<(TaskDefs, Logger) -> Layer> = arrayOf({ td, l -> ValidationLayer(td, l) })

  fun generate(
    name: String,
    storeGens: Array<(Logger) -> Store> = defaultStoreGens,
    shareGens: Array<(Logger) -> Share> = defaultShareGens,
    layerGens: Array<(TaskDefs, Logger) -> Layer> = defaultLayerGens,
    defaultOutputStampers: Array<OutputStamper> = ApiTestGenerator.defaultDefaultOutputStampers,
    defaultRequireFileSystemStampers: Array<ResourceStamper<HierarchicalResource>> = ApiTestGenerator.defaultDefaultRequireHierarchicalStampers,
    defaultProvideFileSystemStampers: Array<ResourceStamper<HierarchicalResource>> = ApiTestGenerator.defaultDefaultProvideHierarchicalStampers,
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
