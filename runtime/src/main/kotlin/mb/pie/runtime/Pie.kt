package mb.pie.runtime

import mb.pie.api.*
import mb.pie.api.exec.BottomUpExecutor
import mb.pie.api.exec.TopDownExecutor
import mb.pie.api.stamp.*
import mb.pie.runtime.cache.NoopCache
import mb.pie.runtime.exec.BottomUpExecutorImpl
import mb.pie.runtime.exec.TopDownExecutorImpl
import mb.pie.runtime.layer.ValidationLayer
import mb.pie.runtime.logger.NoopLogger
import mb.pie.runtime.logger.exec.LoggerExecutorLogger
import mb.pie.runtime.share.NonSharingShare
import mb.pie.runtime.store.InMemoryStore

class PieBuilderImpl : PieBuilder {
  private var taskDefs: TaskDefs? = null
  private var store: (Logger) -> Store = { InMemoryStore() }
  private var cache: (Logger) -> Cache = { NoopCache() }
  private var share: (Logger) -> Share = { NonSharingShare() }
  private var defaultOutputStamper: OutputStamper = OutputStampers.equals
  private var defaultReqFileStamper: FileStamper = FileStampers.modified
  private var defaultGenFileStamper: FileStamper = FileStampers.modified
  private var layerFactory: ((Logger) -> Layer) = { logger -> ValidationLayer(logger) }
  private var logger: Logger = NoopLogger()
  private var executorLoggerFactory: (Logger) -> ExecutorLogger = { logger -> LoggerExecutorLogger(logger) }

  override fun withTaskDefs(taskDefs: TaskDefs): PieBuilder {
    this.taskDefs = taskDefs
    return this
  }

  override fun withStore(store: (Logger) -> Store): PieBuilder {
    this.store = store
    return this
  }

  override fun withCache(cache: (Logger) -> Cache): PieBuilder {
    this.cache = cache
    return this
  }

  override fun withShare(share: (Logger) -> Share): PieBuilder {
    this.share = share
    return this
  }

  override fun withDefaultOutputStamper(stamper: OutputStamper): PieBuilder {
    this.defaultOutputStamper = stamper
    return this
  }

  override fun withDefaultFileReqStamper(stamper: FileStamper): PieBuilder {
    this.defaultReqFileStamper = stamper
    return this
  }

  override fun withDefaultFileGenStamper(stamper: FileStamper): PieBuilder {
    this.defaultGenFileStamper = stamper
    return this
  }

  override fun withLayerFactory(layer: (Logger) -> Layer): PieBuilder {
    this.layerFactory = layer
    return this
  }

  override fun withLogger(logger: Logger): PieBuilder {
    this.logger = logger
    return this
  }

  override fun withExecutorLoggerFactory(executorLogger: (Logger) -> ExecutorLogger): PieBuilder {
    this.executorLoggerFactory = executorLogger
    return this
  }


  override fun build(): Pie {
    val taskDefs = this.taskDefs ?: throw RuntimeException("Task definitions were not set before building")
    val store = this.store(logger)
    val cache = this.cache(logger)
    val share = this.share(logger)
    val topDownExecutor = TopDownExecutorImpl(taskDefs, store, cache, share, defaultOutputStamper, defaultReqFileStamper, defaultGenFileStamper, layerFactory, logger, executorLoggerFactory)
    val bottomUpExecutor = BottomUpExecutorImpl(taskDefs, store, cache, share, defaultOutputStamper, defaultReqFileStamper, defaultGenFileStamper, layerFactory, logger, executorLoggerFactory)
    return PieImpl(topDownExecutor, bottomUpExecutor, store, cache)
  }
}

operator fun PieBuilder.invoke(): PieBuilderImpl {
  return PieBuilderImpl()
}

class PieImpl(
  override val topDownExecutor: TopDownExecutor,
  override val bottomUpExecutor: BottomUpExecutor,
  private val store: Store,
  private val cache: Cache
) : Pie {
  override fun dropStore() {
    store.writeTxn().drop()
  }

  override fun dropCache() {
    cache.drop()
  }

  override fun close() {
    store.close()
  }
}
