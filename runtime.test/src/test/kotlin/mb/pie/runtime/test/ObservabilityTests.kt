package mb.pie.runtime.test

import mb.pie.api.*
import mb.pie.api.stamp.OutputStamper
import mb.pie.api.stamp.ResourceStamper
import mb.pie.api.test.ApiTestGenerator
import mb.pie.runtime.PieBuilderImpl
import mb.pie.runtime.PieImpl
import mb.pie.runtime.logger.StreamLogger
import mb.pie.runtime.logger.exec.LoggerExecutorLogger
import mb.resource.fs.FSResource
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.TestFactory
import java.nio.file.FileSystem
import java.util.stream.Stream

internal class ObservabilityTests {
  @TestFactory
  fun testExplicitObserved() = ObservabilityTestGenerator.generate("testExplicitObserved") {
    newSession().use { session ->
      session.requireTopDown(noopTask)
      assertTrue(pie.isObserved(noopTask))
      assertTrue(pie.isObserved(noopKey))
      pie.store.readTxn().use { txn ->
        val observability = txn.taskObservability(noopKey)
        assertEquals(Observability.ExplicitObserved, observability)
        assertTrue(observability.isObserved)
        assertFalse(observability.isUnobserved)
      }
    }
  }

  @TestFactory
  fun testImplicitObserved() = ObservabilityTestGenerator.generate("testImplicitObserved") {
    newSession().use { session ->
      session.requireTopDown(callNoopTask)

      assertTrue(pie.isObserved(noopTask))
      assertTrue(pie.isObserved(noopKey))
      assertTrue(pie.isObserved(callNoopTask))
      assertTrue(pie.isObserved(callNoopKey))

      pie.store.readTxn().use { txn ->
        // `callNoopTask` is explicitly required, therefore it is explicitly observed.
        val callNoopObservability = txn.taskObservability(callNoopKey)
        assertEquals(Observability.ExplicitObserved, callNoopObservability)
        assertTrue(callNoopObservability.isObserved)
        assertFalse(callNoopObservability.isUnobserved)

        // `noopTask` is required by `callNoopTask`, therefore it is implicitly observed.
        val noopObservability = txn.taskObservability(noopKey)
        assertEquals(Observability.ImplicitObserved, noopObservability)
        assertTrue(noopObservability.isObserved)
        assertFalse(noopObservability.isUnobserved)
      }
    }
  }

  @TestFactory
  fun testImplicitToExplicitObserved() = ObservabilityTestGenerator.generate("testImplicitToExplicitObserved") {
    newSession().use { session ->
      session.requireTopDown(callNoopTask)
      pie.store.readTxn().use { txn ->
        // `noopTask` is required by `callNoopTask`, therefore it is implicitly observed.
        assertEquals(Observability.ImplicitObserved, txn.taskObservability(noopKey))
      }

      // We now explicitly require `noopTask`.
      session.requireTopDown(noopTask)
      pie.store.readTxn().use { txn ->
        // `noopTask` is explicitly required, therefore it is explicitly observed.
        assertEquals(Observability.ExplicitObserved, txn.taskObservability(noopKey))
      }
    }
  }

  @TestFactory
  fun testImplicitToUnobserved() = ObservabilityTestGenerator.generate("testImplicitToUnobserved") {
    newSession().use { session ->
      // We call `callNoopMaybeTaskDef` with input `true`, therefore it requires `noopTask`, making `noopTask`
      // implicitly observed.
      session.requireTopDown(callNoopMaybeTaskDef.createTask(true))
      pie.store.readTxn().use { txn ->
        assertEquals(Observability.ImplicitObserved, txn.taskObservability(noopKey))
      }
    }

    // New session is required, as we are changing the input to `callNoopMaybeTaskDef`.
    newSession().use { session ->
      // We call `callNoopMaybeTaskDef` with input `false`, therefore it DOES NOT require `noopTask`, making `noopTask`
      // now unobserved.
      session.requireTopDown(callNoopMaybeTaskDef.createTask(false))
      pie.store.readTxn().use { txn ->
        val observability = txn.taskObservability(noopKey)
        assertEquals(Observability.Unobserved, observability)
        assertTrue(observability.isUnobserved)
        assertFalse(observability.isObserved)
      }
    }

    // New session is required, as we are changing the input to `callNoopMaybeTaskDef`.
    newSession().use { session ->
      // We call `callNoopMaybeTaskDef` with input `true` again, making it implicitly observed again.
      session.requireTopDown(callNoopMaybeTaskDef.createTask(true))
      pie.store.readTxn().use { txn ->
        assertEquals(Observability.ImplicitObserved, txn.taskObservability(noopKey))
      }
    }
  }

  @TestFactory
  fun testExplicitObservedStays() = ObservabilityTestGenerator.generate("testExplicitObservedStays") {
    newSession().use { session ->
      // We call `callNoopMaybeTaskDef` with input `true`, therefore it requires `noopTask`, making `noopTask`
      // implicitly observed.
      session.requireTopDown(callNoopMaybeTaskDef.createTask(true))
      pie.store.readTxn().use { txn ->
        assertEquals(Observability.ImplicitObserved, txn.taskObservability(noopKey))
      }

      // We explicitly require `noopTask`, making `noopTask` explicitly observed
      session.requireTopDown(noopTask)
      pie.store.readTxn().use { txn ->
        assertEquals(Observability.ExplicitObserved, txn.taskObservability(noopKey))
      }
    }

    // New session is required, as we are changing the input to `callNoopMaybeTaskDef`.
    newSession().use { session ->
      // We call `callNoopMaybeTaskDef` with input `false`, therefore it DOES NOT require `noopTask`. However,
      // `noopTask` is explicitly observed, and it should stay that way.
      session.requireTopDown(callNoopMaybeTaskDef.createTask(false))
      pie.store.readTxn().use { txn ->
        assertEquals(Observability.ExplicitObserved, txn.taskObservability(noopKey))
      }
    }
  }
}


class ObservabilityTestCtx(
  pieImpl: PieImpl,
  taskDefs: MapTaskDefs,
  fs: FileSystem
) : RuntimeTestCtx(pieImpl, taskDefs, fs) {
  val noopTaskDef = taskDef<None, None>("noop") { None.instance }
  val noopTask = noopTaskDef.createTask(None.instance)
  val noopKey = noopTask.key()

  val callNoopTaskDef = taskDef<None, None>("callNoop") { require(noopTaskDef.createTask(None.instance)) }
  val callNoopTask = callNoopTaskDef.createTask(None.instance)
  val callNoopKey = callNoopTask.key()

  // The `callNoopMaybe` task has a key that is always `None.instance` despite its input being different. Therefore,
  // there is only a single instance of this task. This is intended to show the same task flipping its dependencies.
  val callNoopMaybeTaskDef = taskDef<Boolean, None>("callNoopMaybe", { _ -> None.instance }) { input ->
    if(input)
      require(noopTaskDef.createTask(None.instance))
    else
      None.instance
  }

  init {
    addTaskDef(noopTaskDef)
    addTaskDef(callNoopTaskDef)
    addTaskDef(callNoopMaybeTaskDef)
  }
}

object ObservabilityTestGenerator {
  fun generate(
    name: String,
    storeGens: Array<(Logger) -> Store> = RuntimeTestGenerator.defaultStoreGens,
    shareGens: Array<(Logger) -> Share> = RuntimeTestGenerator.defaultShareGens,
    layerGens: Array<(TaskDefs, Logger) -> Layer> = RuntimeTestGenerator.defaultLayerGens,
    defaultOutputStampers: Array<OutputStamper> = ApiTestGenerator.defaultDefaultOutputStampers,
    defaultRequireFileSystemStampers: Array<ResourceStamper<FSResource>> = ApiTestGenerator.defaultDefaultRequireFileSystemStampers,
    defaultProvideFileSystemStampers: Array<ResourceStamper<FSResource>> = ApiTestGenerator.defaultDefaultProvideFileSystemStampers,
    executorLoggerGen: (Logger) -> ExecutorLogger = { l -> LoggerExecutorLogger(l) },
    logger: Logger = StreamLogger.onlyErrors(),
    testFunc: ObservabilityTestCtx.() -> Unit
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
      { pie, taskDefs, fs -> ObservabilityTestCtx(pie as PieImpl, taskDefs as MapTaskDefs, fs) },
      testFunc
    )
  }
}