package mb.pie.runtime.test

import com.nhaarman.mockitokotlin2.*
import mb.pie.api.*
import mb.pie.api.exec.NullCancelled
import mb.pie.api.stamp.OutputStamper
import mb.pie.api.stamp.ResourceStamper
import mb.pie.api.test.*
import mb.pie.runtime.PieBuilderImpl
import mb.pie.runtime.PieImpl
import mb.pie.runtime.logger.StreamLogger
import mb.pie.runtime.logger.exec.LoggerExecutorLogger
import mb.resource.fs.FSResource
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.TestFactory
import java.io.Serializable
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
  fun testImplicitUnobserve() = ObservabilityTestGenerator.generate("testImplicitUnobserve") {
    newSession().use { session ->
      // We call `callNoopMaybeTaskDef` with input `true`, therefore it requires `noopTask`, making `noopTask`
      // implicitly observed.
      session.requireTopDown(callNoopMaybeDef.createTask(true))
      pie.store.readTxn().use { txn ->
        assertEquals(Observability.ImplicitObserved, txn.taskObservability(noopKey))
      }
    }

    // New session is required, as we are changing the input to `callNoopMaybeTaskDef`.
    newSession().use { session ->
      // We call `callNoopMaybeTaskDef` with input `false`, therefore it DOES NOT require `noopTask`, making `noopTask`
      // now unobserved.
      session.requireTopDown(callNoopMaybeDef.createTask(false))
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
      session.requireTopDown(callNoopMaybeDef.createTask(true))
      pie.store.readTxn().use { txn ->
        assertEquals(Observability.ImplicitObserved, txn.taskObservability(noopKey))
      }
    }
  }

  @TestFactory
  fun testImplicitUnobserveExplicitObservedStays() = ObservabilityTestGenerator.generate("testImplicitUnobserveExplicitObservedStays") {
    newSession().use { session ->
      // We call `callNoopMaybeTaskDef` with input `true`, therefore it requires `noopTask`, making `noopTask`
      // implicitly observed.
      session.requireTopDown(callNoopMaybeDef.createTask(true))
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
      session.requireTopDown(callNoopMaybeDef.createTask(false))
      pie.store.readTxn().use { txn ->
        assertEquals(Observability.ExplicitObserved, txn.taskObservability(noopKey))
      }
    }
  }

  @TestFactory
  fun testExplicitUnobserveRoot() = ObservabilityTestGenerator.generate("testExplicitUnobserveRoot") {
    newSession().use { session ->
      session.requireTopDown(callNoopTask)
      pie.store.readTxn().use { txn ->
        assertEquals(Observability.ExplicitObserved, txn.taskObservability(callNoopKey))
        assertEquals(Observability.ImplicitObserved, txn.taskObservability(noopKey))
      }
      session.setUnobserved(callNoopTask)
      pie.store.readTxn().use { txn ->
        // After explicitly unobserving `callNoop`, the entire spine is unobserved.
        assertEquals(Observability.Unobserved, txn.taskObservability(callNoopKey))
        assertEquals(Observability.Unobserved, txn.taskObservability(noopKey))
      }
    }
  }

  @TestFactory
  fun testExplicitUnobserveLeaf() = ObservabilityTestGenerator.generate("testExplicitUnobserveLeaf") {
    newSession().use { session ->
      session.requireTopDown(callNoopTask)
      pie.store.readTxn().use { txn ->
        assertEquals(Observability.ExplicitObserved, txn.taskObservability(callNoopKey))
        assertEquals(Observability.ImplicitObserved, txn.taskObservability(noopKey))
      }
      session.setUnobserved(noopTask)
      pie.store.readTxn().use { txn ->
        // Explicitly unobserving `noop` does nothing, as it is still observed by `callNoop`.
        assertEquals(Observability.ExplicitObserved, txn.taskObservability(callNoopKey))
        assertEquals(Observability.ImplicitObserved, txn.taskObservability(noopKey))
      }
    }
  }

  @TestFactory
  fun testExplicitUnobserveBothExplicitObservedRoot() = ObservabilityTestGenerator.generate("testExplicitUnobserveBothExplicitObservedRoot") {
    newSession().use { session ->
      session.requireTopDown(callNoopTask)
      session.requireTopDown(noopTask)
      pie.store.readTxn().use { txn ->
        assertEquals(Observability.ExplicitObserved, txn.taskObservability(callNoopKey))
        assertEquals(Observability.ExplicitObserved, txn.taskObservability(noopKey))
      }
      session.setUnobserved(callNoopTask)
      pie.store.readTxn().use { txn ->
        // After explicitly unobserving `callNoop`, only `callNoop` is unobserved, as `noop` is still explicitly observed.
        assertEquals(Observability.Unobserved, txn.taskObservability(callNoopKey))
        assertEquals(Observability.ExplicitObserved, txn.taskObservability(noopKey))
      }
    }
  }

  @TestFactory
  fun testExplicitUnobserveBothExplicitObservedLeaf() = ObservabilityTestGenerator.generate("testExplicitUnobserveBothExplicitObservedLeaf") {
    newSession().use { session ->
      session.requireTopDown(callNoopTask)
      session.requireTopDown(noopTask)
      pie.store.readTxn().use { txn ->
        assertEquals(Observability.ExplicitObserved, txn.taskObservability(callNoopKey))
        assertEquals(Observability.ExplicitObserved, txn.taskObservability(noopKey))
      }
      session.setUnobserved(noopTask)
      pie.store.readTxn().use { txn ->
        // Explicitly unobserving `noop` turns it into an implicitly observed task, as `callNoop` still observes it.
        assertEquals(Observability.ExplicitObserved, txn.taskObservability(callNoopKey))
        assertEquals(Observability.ImplicitObserved, txn.taskObservability(noopKey))
      }
    }
  }


  @TestFactory
  fun testBottomUpExecutesObserved() = ObservabilityTestGenerator.generate("testBottomUpExecutesObserved") {
    val resource = resource("/file")
    write("Hello, world!", resource)
    val readTask = readDef.createTask(resource)
    val readSTask = readTask.toSerializableTask()
    val readKey = readTask.key()
    val callTask = callDef.createTask(readSTask)
    val callKey = callTask.key()

    newSession().use { session ->
      session.requireTopDown(callTask)
    }

    // Change the resource and perform a bottom-up build.
    write("Hello, galaxy!", resource)
    newSession().use { pieSession ->
      val session = spy(pieSession.bottomUpSession)
      session.requireInitial(setOf(resource.key), NullCancelled())
      // Both tasks are executed because they are observable.
      inOrder(session) {
        verify(session).exec(eq(readKey), eq(readTask), anyER(), anyC())
        verify(session).exec(eq(callKey), eq(callTask), anyER(), anyC())
      }
    }
  }

  @TestFactory
  fun testBottomUpSkipsUnobservedRequiree() = ObservabilityTestGenerator.generate("testBottomUpSkipsUnobservedRequiree") {
    val resource = resource("/file")
    write("Hello, world!", resource)
    val readTask = readDef.createTask(resource)
    val readSTask = readTask.toSerializableTask()
    val readKey = readTask.key()
    val callTask = callDef.createTask(readSTask)
    val callKey = callTask.key()

    newSession().use { session ->
      session.requireTopDown(callTask)
      // Unobserve `callTask`, making both `callTask` and `readTask` unobservable.
      session.setUnobserved(callTask)
    }

    // Change the resource and perform a bottom-up build.
    write("Hello, galaxy!", resource)
    newSession().use { pieSession ->
      val session = spy(pieSession.bottomUpSession)
      session.requireInitial(setOf(resource.key), NullCancelled())
      // Both tasks are NOT executed because they are unobservable.
      verify(session, never()).exec(eq(readKey), eq(readTask), anyER(), anyC())
      verify(session, never()).exec(eq(callKey), eq(callTask), anyER(), anyC())
    }
  }

  @TestFactory
  fun testBottomUpSkipsUnobservedProvider() = ObservabilityTestGenerator.generate("testBottomUpSkipsUnobservedProvider") {
    val resource = resource("/file")
    val writeTask = writeDef.createTask(ObservabilityTestCtx.Write(resource, "Hello, world!"))
    val writeSTask = writeTask.toSerializableTask()
    val writeKey = writeTask.key()
    val callTask = callDef.createTask(writeSTask)
    val callKey = callTask.key()

    newSession().use { session ->
      session.requireTopDown(callTask)
      // Unobserve `callTask`, making both `callTask` and `writeTask` unobservable.
      session.setUnobserved(callTask)
    }

    // Change the resource and perform a bottom-up build.
    write("Hello, galaxy!", resource)
    newSession().use { pieSession ->
      val session = spy(pieSession.bottomUpSession)
      session.requireInitial(setOf(resource.key), NullCancelled())
      // Both tasks are NOT executed because they are unobservable.
      verify(session, never()).exec(eq(writeKey), eq(writeTask), anyER(), anyC())
      verify(session, never()).exec(eq(callKey), eq(callTask), anyER(), anyC())
    }
  }

  @TestFactory
  fun testBottomUpRequireUnobserved() = ObservabilityTestGenerator.generate("testBottomUpRequireUnobserved") {
    val resource1 = resource("/file1")
    write("Hello, world 1!", resource1)
    val read1Task = readDef.createTask(resource1)
    val read1STask = read1Task.toSerializableTask()
    val read1Key = read1Task.key()

    val resource2 = resource("/file2")
    write("Hello, world 2!", resource2)
    val read2Task = readDef.createTask(resource2)
    val read2STask = read2Task.toSerializableTask()
    val read2Key = read2Task.key()

    val callRead2Task = callDef.createTask(read2STask)
    val callRead2STask = callRead2Task.toSerializableTask()
    val callRead2Key = callRead2Task.key()

    val callMainTask = call2IfContainsGalaxyDef.createTask(ObservabilityTestCtx.Call(read1STask, callRead2STask))
    val callMainKey = callMainTask.key()

    newSession().use { session ->
      session.requireTopDown(callMainTask)
      session.requireTopDown(callRead2Task)
      // Unobserve `callRead2Task`, making it and `read2Task` unobserved.
      session.setUnobserved(callRead2Task)
      pie.store.readTxn().use { txn ->
        assertEquals(Observability.Unobserved, txn.taskObservability(callRead2Key))
        assertEquals(Observability.Unobserved, txn.taskObservability(read2Key))
      }
    }

    // Change resources and perform a bottom-up build.
    write("Hello, galaxy 1!", resource1)
    write("Hello, galaxy 2!", resource2)
    newSession().use { pieSession ->
      val session = spy(pieSession.bottomUpSession)
      session.requireInitial(setOf(resource1.key, resource2.key), NullCancelled())
      inOrder(session) {
        // `read2Task` is not scheduled nor executed yet, despite its resource being changed, because it is unobserved. Consequently, `callRead2Task` will also not be scheduled yet.
        // `read1Task` gets executed because it is observed and its resource changed.
        verify(session).exec(eq(read1Key), eq(read1Task), anyER(), anyC())
        // This in turn affects `callMainTask`, so it gets executed.
        verify(session).exec(eq(callMainKey), eq(callMainTask), anyER(), anyC())
        // `callMainTask` requires `read1Task`.
        verify(session).require(eq(read1Key), eq(read1Task), anyC())
        // But `read1Task` has already been executed, so it will not be executed again.
        // `callMainTask` now requires `callRead2Task`, because `read1Task` returns a string with 'galaxy' in it.
        verify(session).require(eq(callRead2Key), eq(callRead2Task), anyC())
        // While checking if `callRead2Task` must be executed, it requires unobserved task `read2Task`.
        verify(session).require(eq(read2Key), eq(read2Task), anyC())
        // `read2Task` then gets executed despite being unobserved, because it is required and not consistent yet because `resource2` has changed.
        verify(session).exec(eq(read2Key), eq(read2Task), anyER(), anyC())
        // `callRead2Task` then gets executed despite being unobserved, because the result of required task `read2Task` changed.
        verify(session).exec(eq(callRead2Key), eq(callRead2Task), anyER(), anyC())
      }
      pie.store.readTxn().use { txn ->
        // Because `callMainTask` depends on `callRead2Task`, which depends on `read2Task`, they become implicitly observed.
        assertEquals(Observability.ImplicitObserved, txn.taskObservability(callRead2Key))
        assertEquals(Observability.ImplicitObserved, txn.taskObservability(read2Key))
      }
    }

    // Rollback: change resource1 to not have the trigger word 'galaxy' any more.
    write("Hello, world 1!", resource1)
    newSession().use { pieSession ->
      val session = spy(pieSession.bottomUpSession)
      session.requireInitial(setOf(resource1.key), NullCancelled())
      pie.store.readTxn().use { txn ->
        // Because `callMainTask` does not depend on `callRead2Task` any more, it and `read2Task` become unobserved again.
        assertEquals(Observability.Unobserved, txn.taskObservability(callRead2Key))
        assertEquals(Observability.Unobserved, txn.taskObservability(read2Key))
      }
    }

    // Now we only change resource1.
    write("Hello, galaxy 1!", resource1)
    newSession().use { pieSession ->
      val session = spy(pieSession.bottomUpSession)
      session.requireInitial(setOf(resource1.key, resource2.key), NullCancelled())
      inOrder(session) {
        // `read2Task` is not scheduled or executed because its resource did not change. Consequently, `callRead2Task` is also not scheduled.
        // `read1Task` gets executed because it is observed and its resource changed.
        verify(session).exec(eq(read1Key), eq(read1Task), anyER(), anyC())
        // This in turn affects `callMainTask`, so it gets executed.
        verify(session).exec(eq(callMainKey), eq(callMainTask), anyER(), anyC())
        // `callMainTask` now requires `callRead2Task`, because `read1Task` returns a string with 'galaxy' in it.
        verify(session).require(eq(callRead2Key), eq(callRead2Task), anyC())
        // While checking if `callRead2Task` must be executed, it requires unobserved task `read2Task`.
        verify(session).require(eq(read2Key), eq(read2Task), anyC())
      }
      // However, `read2Task` does not get executed, because none of its dependencies are inconsistent.
      verify(session, never()).exec(eq(read2Key), eq(read2Task), anyER(), anyC())
      // Consequently, `callRead2Task` also does not get executed.
      verify(session, never()).exec(eq(callRead2Key), eq(callRead2Task), anyER(), anyC())
      pie.store.readTxn().use { txn ->
        // Despite not being executed, because `callTask` depends on `callRead2Task`, which depends on `read2Task`, it does become implicitly observed again.
        assertEquals(Observability.ImplicitObserved, txn.taskObservability(callRead2Key))
        assertEquals(Observability.ImplicitObserved, txn.taskObservability(read2Key))
      }
    }
  }
}


class ObservabilityTestCtx(
  pieImpl: PieImpl,
  taskDefs: MapTaskDefs,
  fs: FileSystem
) : RuntimeTestCtx(pieImpl, taskDefs, fs) {
  val noopDef = taskDef<None, None>("noop") { None.instance }
  val noopTask = noopDef.createTask(None.instance)
  val noopKey = noopTask.key()

  val callNoopDef = taskDef<None, None>("callNoop") { require(noopDef.createTask(None.instance)) }
  val callNoopTask = callNoopDef.createTask(None.instance)
  val callNoopKey = callNoopTask.key()

  /* `callNoopMaybeDef` has a key that is always `None.instance` despite its input being different. Therefore, there is
  only a single instance of this task. This is intended to show the same task flipping its dependencies. */
  val callNoopMaybeDef = taskDef<Boolean, None>("callNoopMaybe", { _ -> None.instance }) { input ->
    if(input)
      require(noopDef.createTask(None.instance))
    else
      None.instance
  }

  val readDef = taskDef<FSResource, String>("read") { resource ->
    require(resource)
    resource.newInputStream().buffered().use {
      String(it.readBytes())
    }
  }

  data class Write(val resource: FSResource, val text: String): Serializable
  val writeDef = taskDef<Write, None>("write") { (resource, text) ->
    resource.newOutputStream().buffered().use {
      it.write(text.toByteArray())
      it.flush()
    }
    provide(resource)
    None.instance
  }

  val callDef = taskDef<STask, Serializable?>("call") {
    require(it)
  }

  data class Call(val task1: STask, val task2: STask): Serializable
  val call2IfContainsGalaxyDef = taskDef<Call, None>("call2IfContainsGalaxyDef") {(task1, task2) ->
    val result1 = require(task1) as String
    if(result1.contains("galaxy")) {
      require(task2)
    }
    None.instance
  }

  init {
    addTaskDef(noopDef)
    addTaskDef(callNoopDef)
    addTaskDef(callNoopMaybeDef)
    addTaskDef(readDef)
    addTaskDef(writeDef)
    addTaskDef(callDef)
    addTaskDef(call2IfContainsGalaxyDef)
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