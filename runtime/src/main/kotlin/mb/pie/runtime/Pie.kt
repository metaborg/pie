package mb.pie.runtime

import mb.fs.java.JavaFileSystem
import mb.pie.api.*
import mb.pie.api.exec.BottomUpExecutor
import mb.pie.api.exec.TopDownExecutor
import mb.pie.api.fs.FileSystemResourceSystem
import mb.pie.api.fs.stamp.FileSystemStamper
import mb.pie.api.fs.stamp.FileSystemStampers
import mb.pie.api.stamp.OutputStamper
import mb.pie.api.stamp.output.OutputStampers
import mb.pie.runtime.exec.BottomUpExecutorImpl
import mb.pie.runtime.exec.TopDownExecutorImpl
import mb.pie.runtime.layer.ValidationLayer
import mb.pie.runtime.logger.NoopLogger
import mb.pie.runtime.logger.exec.LoggerExecutorLogger
import mb.pie.runtime.resourcesystems.MapResourceSystems
import mb.pie.runtime.share.NonSharingShare
import mb.pie.runtime.store.InMemoryStore

class PieBuilderImpl : PieBuilder {
  private var taskDefs: TaskDefs? = null
  private var resourceSystems: ResourceSystems? = null
  private var store: (Logger) -> Store = { InMemoryStore() }
  private var share: (Logger) -> Share = { NonSharingShare() }
  private var defaultOutputStamper: OutputStamper = OutputStampers.equals
  private var defaultRequireFileSystemStamper: FileSystemStamper = FileSystemStampers.modified
  private var defaultProvideFileSystemStamper: FileSystemStamper = FileSystemStampers.modified
  private var layerFactory: ((Logger) -> Layer) = { logger -> ValidationLayer(logger) }
  private var logger: Logger = NoopLogger()
  private var executorLoggerFactory: (Logger) -> ExecutorLogger = { logger -> LoggerExecutorLogger(logger) }

  override fun withTaskDefs(taskDefs: TaskDefs): PieBuilderImpl {
    this.taskDefs = taskDefs
    return this
  }

  override fun withResourceSystems(resourceSystems: ResourceSystems): PieBuilder {
    this.resourceSystems = resourceSystems
    return this
  }

  override fun withStore(store: (Logger) -> Store): PieBuilderImpl {
    this.store = store
    return this
  }

  override fun withShare(share: (Logger) -> Share): PieBuilderImpl {
    this.share = share
    return this
  }

  override fun withDefaultOutputStamper(stamper: OutputStamper): PieBuilderImpl {
    this.defaultOutputStamper = stamper
    return this
  }

  override fun withDefaultRequireFileSystemStamper(stamper: FileSystemStamper): PieBuilderImpl {
    this.defaultRequireFileSystemStamper = stamper
    return this
  }

  override fun withDefaultProvideFileSystemStamper(stamper: FileSystemStamper): PieBuilderImpl {
    this.defaultProvideFileSystemStamper = stamper
    return this
  }

  override fun withLayer(layer: (Logger) -> Layer): PieBuilderImpl {
    this.layerFactory = layer
    return this
  }

  override fun withLogger(logger: Logger): PieBuilderImpl {
    this.logger = logger
    return this
  }

  override fun withExecutorLogger(executorLogger: (Logger) -> ExecutorLogger): PieBuilderImpl {
    this.executorLoggerFactory = executorLogger
    return this
  }


  override fun build(): PieImpl {
    val taskDefs = this.taskDefs ?: throw RuntimeException("Task definitions were not set before building")
    val resourceSystems = run {
      if(resourceSystems != null) {
        resourceSystems!!
      } else {
        val resourceSystems = MapResourceSystems(mapOf(
          Pair(JavaFileSystem.id, FileSystemResourceSystem(JavaFileSystem.instance))
        ))
        resourceSystems
      }
    }
    val store = this.store(logger)
    val share = this.share(logger)
    val topDownExecutor = TopDownExecutorImpl(taskDefs, resourceSystems, store, share, defaultOutputStamper, defaultRequireFileSystemStamper, defaultProvideFileSystemStamper, layerFactory, logger, executorLoggerFactory)
    val bottomUpExecutor = BottomUpExecutorImpl(taskDefs, resourceSystems, store, share, defaultOutputStamper, defaultRequireFileSystemStamper, defaultProvideFileSystemStamper, layerFactory, logger, executorLoggerFactory)
    return PieImpl(topDownExecutor, bottomUpExecutor, taskDefs, resourceSystems, store, share, defaultOutputStamper, defaultRequireFileSystemStamper, defaultProvideFileSystemStamper, layerFactory, logger, executorLoggerFactory)
  }
}

operator fun PieBuilder.invoke(): PieBuilderImpl {
  return PieBuilderImpl()
}

@Suppress("unused")
class PieImpl(
  override val topDownExecutor: TopDownExecutor,
  override val bottomUpExecutor: BottomUpExecutor,
  val taskDefs: TaskDefs,
  val resourceSystems: ResourceSystems,
  val store: Store,
  val share: Share,
  val defaultOutputStamper: OutputStamper,
  val defaultRequireFileSystemStamper: FileSystemStamper,
  val defaultProvideFileSystemStamper: FileSystemStamper,
  val layerFactory: (Logger) -> Layer,
  val logger: Logger,
  val executorLoggerFactory: (Logger) -> ExecutorLogger
) : Pie {

  override fun dropOutput(key: TaskKey) {
    dropOutput(store.writeTxn(),key)
  }

  override fun addOutput(key: TaskKey) {
    addOutput(store.writeTxn(),key)
    bottomUpExecutor.requireTopDown(key.toTask(taskDefs,store.readTxn()))
  }


  override fun dropStore() {
    store.writeTxn().use { it.drop() }
  }

  override fun close() {
    store.close()
  }

  override fun toString() =
    "PieImpl($store, $share, $defaultOutputStamper, $defaultRequireFileSystemStamper, $defaultProvideFileSystemStamper, ${layerFactory(logger)})"
}
