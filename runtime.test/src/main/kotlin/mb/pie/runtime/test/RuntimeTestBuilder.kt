package mb.pie.runtime.test

import com.nhaarman.mockitokotlin2.*
import mb.pie.api.ExecutorLogger
import mb.pie.api.Layer
import mb.pie.api.Logger
import mb.pie.api.MapTaskDefs
import mb.pie.api.Pie
import mb.pie.api.Share
import mb.pie.api.Store
import mb.pie.api.TaskData
import mb.pie.api.TaskDefs
import mb.pie.api.TaskKey
import mb.pie.api.stamp.ResourceStamper
import mb.pie.api.stamp.resource.HashMatchResourceStamper
import mb.pie.api.stamp.resource.HashResourceStamper
import mb.pie.api.stamp.resource.ModifiedMatchResourceStamper
import mb.pie.api.stamp.resource.ModifiedResourceStamper
import mb.pie.api.test.ApiTestBuilder
import mb.pie.runtime.Callbacks
import mb.pie.runtime.DefaultStampers
import mb.pie.runtime.MapCallbacks
import mb.pie.runtime.MixedSessionImpl
import mb.pie.runtime.PieBuilderImpl
import mb.pie.runtime.PieImpl
import mb.pie.runtime.exec.BottomUpRunner
import mb.pie.runtime.exec.RequireShared
import mb.pie.runtime.exec.TaskExecutor
import mb.pie.runtime.exec.TopDownRunner
import mb.pie.runtime.layer.ValidationLayer
import mb.pie.runtime.logger.StreamLogger
import mb.pie.runtime.logger.exec.LoggerExecutorLogger
import mb.pie.runtime.share.NonSharingShare
import mb.pie.runtime.store.InMemoryStore
import mb.resource.ReadableResource
import mb.resource.ResourceService
import mb.resource.hierarchical.HierarchicalResource
import java.nio.file.FileSystem
import java.util.*
import java.util.function.BiFunction
import java.util.function.Function

open class RuntimeTestBuilder<Ctx : RuntimeTestCtx>(
  shouldSpy: Boolean = true,
  multipleResourceStampers: Boolean = true,
  testContextFactory: (FileSystem, MapTaskDefs, Pie) -> Ctx
) : ApiTestBuilder<Ctx>(
  pieBuilderFactory = { TestPieBuilderImpl(shouldSpy) },
  loggerFactory = { StreamLogger.onlyErrors() },
  executorLoggerFactory = { l -> LoggerExecutorLogger(l) },
  defaultResourceStampers = if(multipleResourceStampers) mutableListOf(ModifiedResourceStamper(), HashResourceStamper()) else mutableListOf<ResourceStamper<ReadableResource>>(ModifiedResourceStamper()),
  defaultHierarchicalStampers = if(multipleResourceStampers) mutableListOf(ModifiedMatchResourceStamper(), HashMatchResourceStamper()) else mutableListOf<ResourceStamper<HierarchicalResource>>(ModifiedMatchResourceStamper()),
  testContextFactory = testContextFactory
) {
  init {
    storeFactories.add { _, _ -> InMemoryStore() }
    shareFactories.add { _ -> NonSharingShare() }
    layerFactories.add { td, l -> ValidationLayer(td, l) }
  }
}

open class TestPieBuilderImpl(private val shouldSpy: Boolean) : PieBuilderImpl() {
  override fun build(): TestPieImpl {
    val store = storeFactory.apply(logger, resourceService)
    val share = shareFactory.apply(logger)
    val defaultStampers = DefaultStampers(defaultOutputStamper, defaultRequireReadableStamper, defaultProvideReadableStamper,
      defaultRequireHierarchicalStamper, defaultProvideHierarchicalStamper)
    return TestPieImpl(
      shouldSpy,
      taskDefs ?: error("Task definitions have not been set. Call PieBuilder#withTaskDefs to set task definitions"),
      resourceService,
      store,
      share,
      defaultStampers,
      layerFactory,
      logger,
      executorLoggerFactory,
      MapCallbacks()
    )
  }
}

open class TestPieImpl(
  private val shouldSpy: Boolean,
  taskDefs: TaskDefs,
  resourceService: ResourceService,
  store: Store,
  share: Share,
  defaultStampers: DefaultStampers,
  layerFactory: BiFunction<TaskDefs, Logger, Layer>,
  logger: Logger,
  executorLoggerFactory: Function<Logger, ExecutorLogger>,
  callbacks: Callbacks
) : PieImpl(taskDefs, resourceService, store, share, defaultStampers, layerFactory, logger, executorLoggerFactory, callbacks) {
  val store: Store get() = super.store // Make store available for testing.

  override fun newSession(): TestMixedSessionImpl {
    val layer = layerFactory.apply(taskDefs, logger)
    val executorLogger = executorLoggerFactory.apply(logger)
    val visited = HashMap<TaskKey, TaskData>()

    val taskExecutor = TaskExecutor(taskDefs, resourceService, super.store, share, defaultStampers, layer, logger, executorLogger,
      callbacks, visited)
    val requireShared = RequireShared(taskDefs, resourceService, super.store, executorLogger, visited)

    var topDownSession = TopDownRunner(super.store, layer, executorLogger, taskExecutor, requireShared, callbacks, visited)
    if(shouldSpy) {
      topDownSession = spy(topDownSession)
    }

    var bottomUpSession = BottomUpRunner(taskDefs, resourceService, super.store, layer, logger, executorLogger, taskExecutor,
      requireShared, callbacks, visited)
    if(shouldSpy) {
      bottomUpSession = spy(bottomUpSession)
    }

    var session = TestMixedSessionImpl(topDownSession, bottomUpSession, taskDefs, resourceService, super.store)
    if(shouldSpy) {
      session = spy(session)
    }

    return session
  }
}

open class TestMixedSessionImpl(
  topDownRunner: TopDownRunner,
  bottomUpRunner: BottomUpRunner,
  taskDefs: TaskDefs,
  resourceService: ResourceService,
  store: Store
) : MixedSessionImpl(topDownRunner, bottomUpRunner, taskDefs, resourceService, store) {
  // Make store available for testing.
  val store: Store get() = super.store

  // Make (possibly spy-ed) sessions visible for testing.
  val topDownRunner: TopDownRunner get() = super.topDownRunner
  val bottomUpRunner: BottomUpRunner get() = super.bottomUpRunner
}


open class DefaultRuntimeTestBuilder(
  shouldSpy: Boolean = true,
  multipleResourceStampers: Boolean = true
) : RuntimeTestBuilder<RuntimeTestCtx>(
  shouldSpy,
  multipleResourceStampers,
  { fs, taskDefs, pie -> RuntimeTestCtx(fs, taskDefs, pie as TestPieImpl) }
)
