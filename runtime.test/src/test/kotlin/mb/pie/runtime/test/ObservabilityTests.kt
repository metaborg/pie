package mb.pie.runtime.test

import com.nhaarman.mockitokotlin2.*
import mb.pie.api.MapTaskDefs
import mb.pie.api.None
import mb.pie.api.Observability
import mb.pie.api.STask
import mb.pie.api.test.anyC
import mb.pie.api.test.anyER
import mb.resource.fs.FSResource
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestFactory
import java.io.Serializable
import java.nio.file.FileSystem

class ObservabilityTests {
  private val builder = ObservabilityTestBuilder()


  /*** Observability in top-down require ***/

  @TestFactory
  fun testExplicitObserved() = builder.test {
    newSession().use { session ->
      session.require(noopTask)
      assertTrue(pie.isObserved(noopTask))
      assertTrue(pie.isObserved(noopKey))
      session.store.readTxn().use { txn ->
        val observability = txn.taskObservability(noopKey)
        assertEquals(Observability.ExplicitObserved, observability)
        assertTrue(observability.isObserved)
        assertFalse(observability.isUnobserved)
      }
    }
  }

  @TestFactory
  fun testImplicitObserved() = builder.test {
    newSession().use { session ->
      session.require(callNoopTask)

      assertTrue(pie.isObserved(noopTask))
      assertTrue(pie.isObserved(noopKey))
      assertTrue(pie.isObserved(callNoopTask))
      assertTrue(pie.isObserved(callNoopKey))

      session.store.readTxn().use { txn ->
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
  fun testImplicitToExplicitObserved() = builder.test {
    newSession().use { session ->
      session.require(callNoopTask)
      session.store.readTxn().use { txn ->
        // `noopTask` is required by `callNoopTask`, therefore it is implicitly observed.
        assertEquals(Observability.ImplicitObserved, txn.taskObservability(noopKey))
      }

      // We now explicitly require `noopTask`.
      session.require(noopTask)
      session.store.readTxn().use { txn ->
        // `noopTask` is explicitly required, therefore it is explicitly observed.
        assertEquals(Observability.ExplicitObserved, txn.taskObservability(noopKey))
      }
    }
  }

  @TestFactory
  fun testImplicitUnobserve() = builder.test {
    newSession().use { session ->
      // We call `callNoopMaybeTaskDef` with input `true`, therefore it requires `noopTask`, making `noopTask`
      // implicitly observed.
      session.require(callNoopMaybeDef.createTask(true))
      session.store.readTxn().use { txn ->
        assertEquals(Observability.ImplicitObserved, txn.taskObservability(noopKey))
      }
    }

    // New session is required, as we are changing the input to `callNoopMaybeTaskDef`.
    newSession().use { session ->
      // We call `callNoopMaybeTaskDef` with input `false`, therefore it DOES NOT require `noopTask`, making `noopTask`
      // now unobserved.
      session.require(callNoopMaybeDef.createTask(false))
      session.store.readTxn().use { txn ->
        val observability = txn.taskObservability(noopKey)
        assertEquals(Observability.Unobserved, observability)
        assertTrue(observability.isUnobserved)
        assertFalse(observability.isObserved)
      }
    }

    // New session is required, as we are changing the input to `callNoopMaybeTaskDef`.
    newSession().use { session ->
      // We call `callNoopMaybeTaskDef` with input `true` again, making `noopTask` implicitly observed again.
      session.require(callNoopMaybeDef.createTask(true))
      session.store.readTxn().use { txn ->
        assertEquals(Observability.ImplicitObserved, txn.taskObservability(noopKey))
      }
    }
  }

  @TestFactory
  fun testImplicitUnobserveExplicitObservedStays() = builder.test {
    newSession().use { session ->
      // We call `callNoopMaybeTaskDef` with input `true`, therefore it requires `noopTask`, making `noopTask`
      // implicitly observed.
      session.require(callNoopMaybeDef.createTask(true))
      session.store.readTxn().use { txn ->
        assertEquals(Observability.ImplicitObserved, txn.taskObservability(noopKey))
      }

      // We explicitly require `noopTask`, making `noopTask` explicitly observed
      session.require(noopTask)
      session.store.readTxn().use { txn ->
        assertEquals(Observability.ExplicitObserved, txn.taskObservability(noopKey))
      }
    }

    // New session is required, as we are changing the input to `callNoopMaybeTaskDef`.
    newSession().use { session ->
      // We call `callNoopMaybeTaskDef` with input `false`, therefore it DOES NOT require `noopTask`. However,
      // `noopTask` is explicitly observed, and it should stay that way.
      session.require(callNoopMaybeDef.createTask(false))
      session.store.readTxn().use { txn ->
        assertEquals(Observability.ExplicitObserved, txn.taskObservability(noopKey))
      }
    }
  }


  /*** Observability in top-down require without observing ***/

  @TestFactory
  fun testRequireWithoutObserveNewTaskStaysUnobserved() = builder.test {
    newSession().use { session ->
      session.requireWithoutObserving(callNoopTask)

      // Because we are requiring without observing, everything is unobserved.
      assertFalse(pie.isObserved(noopTask))
      assertFalse(pie.isObserved(noopKey))
      assertFalse(pie.isObserved(callNoopTask))
      assertFalse(pie.isObserved(callNoopKey))

      session.store.readTxn().use { txn ->
        val callNoopObservability = txn.taskObservability(callNoopKey)
        assertEquals(Observability.Unobserved, callNoopObservability)
        assertTrue(callNoopObservability.isUnobserved)
        assertFalse(callNoopObservability.isObserved)

        val noopObservability = txn.taskObservability(noopKey)
        assertEquals(Observability.Unobserved, noopObservability)
        assertTrue(noopObservability.isUnobserved)
        assertFalse(noopObservability.isObserved)
      }
    }
  }

  @TestFactory
  fun testRequireWithoutObserveNewTaskTreeStaysUnobserved() = builder.test {
    newSession().use { session ->
      // We call `callNoopMaybeTaskDef` with input `true`, therefore it requires `noopTask`. However, we are not
      // observing, so nothing is observed.
      session.requireWithoutObserving(callNoopMaybeDef.createTask(true))
    }

    // New session is required, as we are changing the input to `callNoopMaybeTaskDef`.
    newSession().use { session ->
      // We call `callNoopMaybeTaskDef` with input `false`, therefore it DOES NOT require `noopTask`. Everything stays
      // unobserved.
      session.requireWithoutObserving(callNoopMaybeDef.createTask(false))
      session.store.readTxn().use { txn ->
        val observability = txn.taskObservability(noopKey)
        assertEquals(Observability.Unobserved, observability)
        assertTrue(observability.isUnobserved)
        assertFalse(observability.isObserved)
      }
    }

    // New session is required, as we are changing the input to `callNoopMaybeTaskDef`.
    newSession().use { session ->
      // We call `callNoopMaybeTaskDef` with input `true` again, but everything stays unobserved.
      session.requireWithoutObserving(callNoopMaybeDef.createTask(true))
      session.store.readTxn().use { txn ->
        assertEquals(Observability.Unobserved, txn.taskObservability(noopKey))
      }
    }
  }

  @TestFactory
  fun testRequireWithoutObserveDoesNotChangeObservabilityStatus() = builder.test {
    newSession().use { session ->
      // We first require with observing, explicitly observing `callNoopTask`, and implicitly observing `noopTask`.
      session.require(callNoopTask)
      session.store.readTxn().use { txn ->
        val callNoopObservability = txn.taskObservability(callNoopKey)
        assertEquals(Observability.ExplicitObserved, callNoopObservability)
        val noopObservability = txn.taskObservability(noopKey)
        assertEquals(Observability.ImplicitObserved, noopObservability)
      }
    }

    newSession().use { session ->
      // Then we require without observing: observability status should stay the same.
      session.require(callNoopTask)
      session.store.readTxn().use { txn ->
        val callNoopObservability = txn.taskObservability(callNoopKey)
        assertEquals(Observability.ExplicitObserved, callNoopObservability)
        val noopObservability = txn.taskObservability(noopKey)
        assertEquals(Observability.ImplicitObserved, noopObservability)
      }
    }

    newSession().use { session ->
      // When we require `noopTask` without observing, it stays implicitly observed.
      session.requireWithoutObserving(callNoopTask)
      session.store.readTxn().use { txn ->
        val noopObservability = txn.taskObservability(noopKey)
        assertEquals(Observability.ImplicitObserved, noopObservability)
      }
    }
  }

  @TestFactory
  fun testRequireWithoutObserveImplicitlyUnobservesAndObserves() = builder.test {
    newSession().use { session ->
      // We call `callNoopMaybeTaskDef` with input `true`, therefore it requires `noopTask`, making `noopTask`
      // implicitly observed.
      session.require(callNoopMaybeDef.createTask(true))
      session.store.readTxn().use { txn ->
        assertEquals(Observability.ImplicitObserved, txn.taskObservability(noopKey))
      }
    }

    // New session is required, as we are changing the input to `callNoopMaybeTaskDef`.
    newSession().use { session ->
      // We call `callNoopMaybeTaskDef` with input `false`, therefore it DOES NOT require `noopTask`, making `noopTask`
      // now unobserved.
      session.requireWithoutObserving(callNoopMaybeDef.createTask(false))
      session.store.readTxn().use { txn ->
        val observability = txn.taskObservability(noopKey)
        assertEquals(Observability.Unobserved, observability)
        assertTrue(observability.isUnobserved)
        assertFalse(observability.isObserved)
      }
    }

    // New session is required, as we are changing the input to `callNoopMaybeTaskDef`.
    newSession().use { session ->
      // We call `callNoopMaybeTaskDef` with input `true` again, making `noopTask` implicitly observed again because
      // (explicitly) observed task `callNoopMaybeTaskDef` requires it, despite requiring without observability.
      session.requireWithoutObserving(callNoopMaybeDef.createTask(true))
      session.store.readTxn().use { txn ->
        assertEquals(Observability.ImplicitObserved, txn.taskObservability(noopKey))
      }
    }
  }


  /*** Explicit unobserve ***/

  @TestFactory
  fun testExplicitUnobserveRoot() = builder.test {
    newSession().use { session ->
      session.require(callNoopTask)
      session.store.readTxn().use { txn ->
        assertEquals(Observability.ExplicitObserved, txn.taskObservability(callNoopKey))
        assertEquals(Observability.ImplicitObserved, txn.taskObservability(noopKey))
      }
      session.unobserve(callNoopTask)
      session.store.readTxn().use { txn ->
        // After explicitly unobserving `callNoop`, the entire spine is unobserved.
        assertEquals(Observability.Unobserved, txn.taskObservability(callNoopKey))
        assertEquals(Observability.Unobserved, txn.taskObservability(noopKey))
      }
    }
  }

  @TestFactory
  fun testExplicitUnobserveLeaf() = builder.test {
    newSession().use { session ->
      session.require(callNoopTask)
      session.store.readTxn().use { txn ->
        assertEquals(Observability.ExplicitObserved, txn.taskObservability(callNoopKey))
        assertEquals(Observability.ImplicitObserved, txn.taskObservability(noopKey))
      }
      session.unobserve(noopTask)
      session.store.readTxn().use { txn ->
        // Explicitly unobserving `noop` does nothing, as it is still observed by `callNoop`.
        assertEquals(Observability.ExplicitObserved, txn.taskObservability(callNoopKey))
        assertEquals(Observability.ImplicitObserved, txn.taskObservability(noopKey))
      }
    }
  }

  @TestFactory
  fun testExplicitUnobserveBothExplicitObservedRoot() = builder.test {
    newSession().use { session ->
      session.require(callNoopTask)
      session.require(noopTask)
      session.store.readTxn().use { txn ->
        assertEquals(Observability.ExplicitObserved, txn.taskObservability(callNoopKey))
        assertEquals(Observability.ExplicitObserved, txn.taskObservability(noopKey))
      }
      session.unobserve(callNoopTask)
      session.store.readTxn().use { txn ->
        // After explicitly unobserving `callNoop`, only `callNoop` is unobserved, as `noop` is still explicitly observed.
        assertEquals(Observability.Unobserved, txn.taskObservability(callNoopKey))
        assertEquals(Observability.ExplicitObserved, txn.taskObservability(noopKey))
      }
    }
  }

  @TestFactory
  fun testExplicitUnobserveBothExplicitObservedLeaf() = builder.test {
    newSession().use { session ->
      session.require(callNoopTask)
      session.require(noopTask)
      session.store.readTxn().use { txn ->
        assertEquals(Observability.ExplicitObserved, txn.taskObservability(callNoopKey))
        assertEquals(Observability.ExplicitObserved, txn.taskObservability(noopKey))
      }
      session.unobserve(noopTask)
      session.store.readTxn().use { txn ->
        // Explicitly unobserving `noop` turns it into an implicitly observed task, as `callNoop` still observes it.
        assertEquals(Observability.ExplicitObserved, txn.taskObservability(callNoopKey))
        assertEquals(Observability.ImplicitObserved, txn.taskObservability(noopKey))
      }
    }
  }


  /*** Observability in bottom-up update ***/

  @TestFactory
  fun testBottomUpExecutesObserved() = builder.test {
    val resource = resource("/file")
    write("Hello, world!", resource)
    val readTask = readDef.createTask(resource)
    val readSTask = readTask.toSerializableTask()
    val readKey = readTask.key()
    val callTask = callDef.createTask(readSTask)
    val callKey = callTask.key()

    newSession().use { session ->
      session.require(callTask)
    }

    // Change the resource and perform a bottom-up build.
    write("Hello, galaxy!", resource)
    newSession().use { session ->
      session.updateAffectedBy(setOf(resource.key))
      // Both tasks are executed because they are observable.
      val bottomUpSession = session.bottomUpSession
      inOrder(bottomUpSession) {
        verify(bottomUpSession).exec(eq(readKey), eq(readTask), anyER(), anyC())
        verify(bottomUpSession).exec(eq(callKey), eq(callTask), anyER(), anyC())
      }
    }
  }

  @TestFactory
  fun testBottomUpSkipsUnobservedRequiree() = builder.test {
    val resource = resource("/file")
    write("Hello, world!", resource)
    val readTask = readDef.createTask(resource)
    val readSTask = readTask.toSerializableTask()
    val readKey = readTask.key()
    val callTask = callDef.createTask(readSTask)
    val callKey = callTask.key()

    newSession().use { session ->
      session.require(callTask)
      // Unobserve `callTask`, making both `callTask` and `readTask` unobservable.
      session.unobserve(callTask)
    }

    // Change the resource and perform a bottom-up build.
    write("Hello, galaxy!", resource)
    newSession().use { session ->
      session.updateAffectedBy(setOf(resource.key))
      // Both tasks are NOT executed because they are unobservable.
      val bottomUpSession = session.bottomUpSession
      verify(bottomUpSession, never()).exec(eq(readKey), eq(readTask), anyER(), anyC())
      verify(bottomUpSession, never()).exec(eq(callKey), eq(callTask), anyER(), anyC())
    }
  }

  @TestFactory
  fun testBottomUpSkipsUnobservedProvider() = builder.test {
    val resource = resource("/file")
    val writeTask = writeDef.createTask(ObservabilityTestCtx.Write(resource, "Hello, world!"))
    val writeSTask = writeTask.toSerializableTask()
    val writeKey = writeTask.key()
    val callTask = callDef.createTask(writeSTask)
    val callKey = callTask.key()

    newSession().use { session ->
      session.require(callTask)
      // Unobserve `callTask`, making both `callTask` and `writeTask` unobservable.
      session.unobserve(callTask)
    }

    // Change the resource and perform a bottom-up build.
    write("Hello, galaxy!", resource)
    newSession().use { session ->
      session.updateAffectedBy(setOf(resource.key))
      // Both tasks are NOT executed because they are unobservable.
      val bottomUpSession = session.bottomUpSession
      verify(bottomUpSession, never()).exec(eq(writeKey), eq(writeTask), anyER(), anyC())
      verify(bottomUpSession, never()).exec(eq(callKey), eq(callTask), anyER(), anyC())
    }
  }

  @TestFactory
  fun testBottomUpRequireUnobserved() = builder.test {
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
      session.require(callMainTask)
      session.require(callRead2Task)
      // Unobserve `callRead2Task`, making it and `read2Task` unobserved.
      session.unobserve(callRead2Task)
      session.store.readTxn().use { txn ->
        assertEquals(Observability.Unobserved, txn.taskObservability(callRead2Key))
        assertEquals(Observability.Unobserved, txn.taskObservability(read2Key))
      }
    }

    // Change resources and perform a bottom-up build.
    write("Hello, galaxy 1!", resource1)
    write("Hello, galaxy 2!", resource2)
    newSession().use { session ->
      session.updateAffectedBy(setOf(resource1.key, resource2.key))
      val bottomUpSession = session.bottomUpSession
      inOrder(bottomUpSession) {
        // `read2Task` is not scheduled nor executed yet, despite its resource being changed, because it is unobserved. Consequently, `callRead2Task` will also not be scheduled yet.
        // `read1Task` gets executed because it is observed and its resource changed.
        verify(bottomUpSession).exec(eq(read1Key), eq(read1Task), anyER(), anyC())
        // This in turn affects `callMainTask`, so it gets executed.
        verify(bottomUpSession).exec(eq(callMainKey), eq(callMainTask), anyER(), anyC())
        // `callMainTask` requires `read1Task`.
        verify(bottomUpSession).require(eq(read1Key), eq(read1Task), any(), anyC())
        // But `read1Task` has already been executed, so it will not be executed again.
        // `callMainTask` now requires `callRead2Task`, because `read1Task` returns a string with 'galaxy' in it.
        verify(bottomUpSession).require(eq(callRead2Key), eq(callRead2Task), any(), anyC())
        // While checking if `callRead2Task` must be executed, it requires unobserved task `read2Task`.
        verify(bottomUpSession).require(eq(read2Key), eq(read2Task), any(), anyC())
        // `read2Task` then gets executed despite being unobserved, because it is required and not consistent yet because `resource2` has changed.
        verify(bottomUpSession).exec(eq(read2Key), eq(read2Task), anyER(), anyC())
        // `callRead2Task` then gets executed despite being unobserved, because the result of required task `read2Task` changed.
        verify(bottomUpSession).exec(eq(callRead2Key), eq(callRead2Task), anyER(), anyC())
      }
      session.store.readTxn().use { txn ->
        // Because `callMainTask` depends on `callRead2Task`, which depends on `read2Task`, they become implicitly observed.
        assertEquals(Observability.ImplicitObserved, txn.taskObservability(callRead2Key))
        assertEquals(Observability.ImplicitObserved, txn.taskObservability(read2Key))
      }
    }

    // Rollback: change resource1 to not have the trigger word 'galaxy' any more.
    write("Hello, world 1!", resource1)
    newSession().use { session ->
      session.updateAffectedBy(setOf(resource1.key))
      session.store.readTxn().use { txn ->
        // Because `callMainTask` does not depend on `callRead2Task` any more, it and `read2Task` become unobserved again.
        assertEquals(Observability.Unobserved, txn.taskObservability(callRead2Key))
        assertEquals(Observability.Unobserved, txn.taskObservability(read2Key))
      }
    }

    // Now we only change resource1.
    write("Hello, galaxy 1!", resource1)
    newSession().use { session ->
      session.updateAffectedBy(setOf(resource1.key, resource2.key))
      val bottomUpSession = session.bottomUpSession
      inOrder(bottomUpSession) {
        // `read2Task` is not scheduled or executed because its resource did not change. Consequently, `callRead2Task` is also not scheduled.
        // `read1Task` gets executed because it is observed and its resource changed.
        verify(bottomUpSession).exec(eq(read1Key), eq(read1Task), anyER(), anyC())
        // This in turn affects `callMainTask`, so it gets executed.
        verify(bottomUpSession).exec(eq(callMainKey), eq(callMainTask), anyER(), anyC())
        // `callMainTask` now requires `callRead2Task`, because `read1Task` returns a string with 'galaxy' in it.
        verify(bottomUpSession).require(eq(callRead2Key), eq(callRead2Task), any(), anyC())
        // While checking if `callRead2Task` must be executed, it requires unobserved task `read2Task`.
        verify(bottomUpSession).require(eq(read2Key), eq(read2Task), any(), anyC())
      }
      // However, `read2Task` does not get executed, because none of its dependencies are inconsistent.
      verify(bottomUpSession, never()).exec(eq(read2Key), eq(read2Task), anyER(), anyC())
      // Consequently, `callRead2Task` also does not get executed.
      verify(bottomUpSession, never()).exec(eq(callRead2Key), eq(callRead2Task), anyER(), anyC())
      session.store.readTxn().use { txn ->
        // Despite not being executed, because `callTask` depends on `callRead2Task`, which depends on `read2Task`, it does become implicitly observed again.
        assertEquals(Observability.ImplicitObserved, txn.taskObservability(callRead2Key))
        assertEquals(Observability.ImplicitObserved, txn.taskObservability(read2Key))
      }
    }
  }


  /*** Garbage collection of unobserved tasks and unobserved provided resources ***/

  @TestFactory
  fun testGCTwiceNoExceptions() = builder.test {
    this.GCSetup().run {
      newSession().use { session ->
        session.deleteUnobservedTasks({ _ -> true }, { _, _ -> true })
        session.deleteUnobservedTasks({ _ -> true }, { _, _ -> true })
      }
    }
  }

  @TestFactory
  fun testGCDeletesCorrect() = builder.test {
    this.GCSetup().run {
      newSession().use { session ->
        session.deleteUnobservedTasks({ _ -> true }, { _, _ -> true })
        session.store.readTxn().use { txn ->
          assertNull(txn.input(aTask.key()))
          assertNull(txn.input(bTask.key()))
          assertNotNull(txn.input(cTask.key()))
          assertNull(txn.input(dTask.key()))
          assertNotNull(txn.input(eTask.key()))
          assertNull(txn.input(fTask.key()))
          assertNotNull(txn.input(gTask.key()))
          assertNotNull(txn.input(hTask.key()))
          assertNull(txn.input(iTask.key()))
          assertNull(txn.input(jTask.key()))
          assertNull(txn.input(kTask.key()))
          assertFalse(file0.exists()) // Deleted provided file
          assertTrue(file1.exists()) // Unobserved required file
          assertTrue(file2.exists()) // Observed provided file
          assertFalse(file3.exists()) // Deleted provided file
          assertTrue(file4.exists()) // Observed required file
          assertTrue(file5.exists()) // Observed provided file
        }
      }
    }
  }

  @TestFactory
  fun testGCDeletesAllDataFromStore() = builder.test {
    this.GCSetup().run {
      newSession().use { session ->
        session.deleteUnobservedTasks({ _ -> true }, { _, _ -> true })
        session.store.readTxn().use { txn ->
          for(key in listOf(aTask.key(), bTask.key(), dTask.key(), fTask.key(), iTask.key(), jTask.key(), kTask.key())) {
            assertNull(txn.input(key))
            assertNull(txn.output(key))
            assertEquals(Observability.Unobserved, txn.taskObservability(key)) // Default value: Observability.Unobserved
            assertTrue(txn.taskRequires(key).isEmpty()) // Default value: empty list
            assertTrue(txn.callersOf(key).isEmpty()) // Default value: empty set
            assertTrue(txn.resourceRequires(key).isEmpty()) // Default value: empty list
            assertTrue(txn.resourceProvides(key).isEmpty()) // Default value: empty list
            assertNull(txn.data(key))
          }
          for(key in listOf(file0.key, file3.key)) {
            assertTrue(txn.requireesOf(key).isEmpty()) // Default value: empty set
            assertNull(txn.providerOf(key))
          }
        }
      }
    }
  }

  @TestFactory
  fun testGCDoesNotInfluenceExecution() = builder.test {
    // First run without garbage collection.
    this.GCSetup().run {
      // Modify all files.
      write("Hello, galaxy 0!", file0)
      write("Hello, galaxy 1!", file1)
      write("Hello, galaxy 2!", file2)
      write("Hello, galaxy 3!", file3)
      write("Hello, galaxy 4!", file4)
      write("Hello, galaxy 5!", file5)
      newSession().use { session ->
        session.updateAffectedBy(listOf(file0, file1, file2, file3, file4, file5).map { it.key }.toSet())
        val bottomUpSession = session.bottomUpSession
        verify(bottomUpSession, never()).exec(eq(aTask.key()), eq(aTask), anyER(), anyC())
        verify(bottomUpSession, never()).exec(eq(bTask.key()), eq(bTask), anyER(), anyC())
        verify(bottomUpSession, times(1)).exec(eq(cTask.key()), eq(cTask), anyER(), anyC())
        verify(bottomUpSession, never()).exec(eq(dTask.key()), eq(dTask), anyER(), anyC())
        verify(bottomUpSession, times(1)).exec(eq(eTask.key()), eq(eTask), anyER(), anyC())
        verify(bottomUpSession, never()).exec(eq(fTask.key()), eq(fTask), anyER(), anyC())
        verify(bottomUpSession, times(1)).exec(eq(gTask.key()), eq(gTask), anyER(), anyC())
        verify(bottomUpSession, times(1)).exec(eq(hTask.key()), eq(hTask), anyER(), anyC())
        verify(bottomUpSession, never()).exec(eq(iTask.key()), eq(iTask), anyER(), anyC())
        verify(bottomUpSession, never()).exec(eq(jTask.key()), eq(jTask), anyER(), anyC())
        verify(bottomUpSession, never()).exec(eq(kTask.key()), eq(kTask), anyER(), anyC())
        verify(bottomUpSession, never()).exec(eq(lTask.key()), eq(lTask), anyER(), anyC())
      }
    }

    // Second run with garbage collection
    pie.dropStore() // Drop store to begin with a clean slate: all tasks will be re-executed.
    this.GCSetup().run {
      // Run setup, which executes tasks and set some tasks to unobserved again.
      // Modify all files.
      write("Hello, galaxy 0!", file0)
      write("Hello, galaxy 1!", file1)
      write("Hello, galaxy 2!", file2)
      write("Hello, galaxy 3!", file3)
      write("Hello, galaxy 4!", file4)
      write("Hello, galaxy 5!", file5)
      newSession().use { session ->
        // First run garbage collection.
        session.deleteUnobservedTasks({ _ -> true }, { _, _ -> true })
        // Then build and confirm that the exact same tasks are executed.
        session.updateAffectedBy(listOf(file0, file1, file2, file3, file4, file5).map { it.key }.toSet())
        val bottomUpSession = session.bottomUpSession
        verify(bottomUpSession, never()).exec(eq(aTask.key()), eq(aTask), anyER(), anyC())
        verify(bottomUpSession, never()).exec(eq(bTask.key()), eq(bTask), anyER(), anyC())
        verify(bottomUpSession, times(1)).exec(eq(cTask.key()), eq(cTask), anyER(), anyC())
        verify(bottomUpSession, never()).exec(eq(dTask.key()), eq(dTask), anyER(), anyC())
        verify(bottomUpSession, times(1)).exec(eq(eTask.key()), eq(eTask), anyER(), anyC())
        verify(bottomUpSession, never()).exec(eq(fTask.key()), eq(fTask), anyER(), anyC())
        verify(bottomUpSession, times(1)).exec(eq(gTask.key()), eq(gTask), anyER(), anyC())
        verify(bottomUpSession, times(1)).exec(eq(hTask.key()), eq(hTask), anyER(), anyC())
        verify(bottomUpSession, never()).exec(eq(iTask.key()), eq(iTask), anyER(), anyC())
        verify(bottomUpSession, never()).exec(eq(jTask.key()), eq(jTask), anyER(), anyC())
        verify(bottomUpSession, never()).exec(eq(kTask.key()), eq(kTask), anyER(), anyC())
        verify(bottomUpSession, never()).exec(eq(lTask.key()), eq(lTask), anyER(), anyC())
      }
    }
  }

  @TestFactory
  fun testGCShouldDeleteFunctionFalse() = builder.test {
    this.GCSetup().run {
      newSession().use { session ->
        session.deleteUnobservedTasks({ _ -> false }, { _, _ -> true })
        session.store.readTxn().use { txn ->
          assertNotNull(txn.input(aTask.key()))
          assertNotNull(txn.input(bTask.key()))
          assertNotNull(txn.input(cTask.key()))
          assertNotNull(txn.input(dTask.key()))
          assertNotNull(txn.input(eTask.key()))
          assertNotNull(txn.input(fTask.key()))
          assertNotNull(txn.input(gTask.key()))
          assertNotNull(txn.input(hTask.key()))
          assertNotNull(txn.input(iTask.key()))
          assertNotNull(txn.input(jTask.key()))
          assertNotNull(txn.input(kTask.key()))
          assertTrue(file0.exists()) // Kept provided file
          assertTrue(file1.exists()) // Unobserved required file
          assertTrue(file2.exists()) // Observed provided file
          assertTrue(file3.exists()) // Kept provided file
          assertTrue(file4.exists()) // Observed required file
          assertTrue(file5.exists()) // Observed provided file
        }
      }
    }
  }

  @TestFactory
  fun testGCShouldDeleteFunctionSpecific() = builder.test {
    this.GCSetup().run {
      newSession().use { session ->
        session.deleteUnobservedTasks({ t -> t == aTask || t == dTask || t == iTask }, { _, _ -> true })
        session.store.readTxn().use { txn ->
          assertNull(txn.input(aTask.key()))
          assertNotNull(txn.input(bTask.key()))
          assertNotNull(txn.input(cTask.key()))
          assertNull(txn.input(dTask.key()))
          assertNotNull(txn.input(eTask.key()))
          assertNotNull(txn.input(fTask.key()))
          assertNotNull(txn.input(gTask.key()))
          assertNotNull(txn.input(hTask.key()))
          assertNull(txn.input(iTask.key()))
          assertNotNull(txn.input(jTask.key()))
          assertNotNull(txn.input(kTask.key()))
          assertFalse(file0.exists()) // Deleted provided file
          assertTrue(file1.exists()) // Unobserved required file
          assertTrue(file2.exists()) // Observed provided file
          assertTrue(file3.exists()) // Deleted provided file
          assertTrue(file4.exists()) // Observed required file
          assertTrue(file5.exists()) // Observed provided file
        }
      }
    }
  }

  @TestFactory
  fun testGCShouldDeleteProvidedResourceFalse() = builder.test {
    this.GCSetup().run {
      newSession().use { session ->
        session.deleteUnobservedTasks({ _ -> true }, { _, _ -> false })
        session.store.readTxn().use { txn ->
          assertNull(txn.input(aTask.key()))
          assertNull(txn.input(bTask.key()))
          assertNotNull(txn.input(cTask.key()))
          assertNull(txn.input(dTask.key()))
          assertNotNull(txn.input(eTask.key()))
          assertNull(txn.input(fTask.key()))
          assertNotNull(txn.input(gTask.key()))
          assertNotNull(txn.input(hTask.key()))
          assertNull(txn.input(iTask.key()))
          assertNull(txn.input(jTask.key()))
          assertNull(txn.input(kTask.key()))
          assertTrue(file0.exists()) // Kept provided file
          assertTrue(file1.exists()) // Unobserved required file
          assertTrue(file2.exists()) // Observed provided file
          assertTrue(file3.exists()) // Kept provided file
          assertTrue(file4.exists()) // Observed required file
          assertTrue(file5.exists()) // Observed provided file
        }
      }
    }
  }

  @TestFactory
  fun testGCShouldDeleteProvidedResourceSpecific() = builder.test {
    this.GCSetup().run {
      newSession().use { session ->
        session.deleteUnobservedTasks({ _ -> true }, { _, r -> r == file3 })
        session.store.readTxn().use { txn ->
          assertNull(txn.input(aTask.key()))
          assertNull(txn.input(bTask.key()))
          assertNotNull(txn.input(cTask.key()))
          assertNull(txn.input(dTask.key()))
          assertNotNull(txn.input(eTask.key()))
          assertNull(txn.input(fTask.key()))
          assertNotNull(txn.input(gTask.key()))
          assertNotNull(txn.input(hTask.key()))
          assertNull(txn.input(iTask.key()))
          assertNull(txn.input(jTask.key()))
          assertNull(txn.input(kTask.key()))
          assertTrue(file0.exists()) // Kept provided file
          assertTrue(file1.exists()) // Unobserved required file
          assertTrue(file2.exists()) // Observed provided file
          assertFalse(file3.exists()) // Deleted provided file
          assertTrue(file4.exists()) // Observed required file
          assertTrue(file5.exists()) // Observed provided file
        }
      }
    }
  }
}


class ObservabilityTestCtx(
  fs: FileSystem,
  taskDefs: MapTaskDefs,
  pieImpl: TestPieImpl
) : RuntimeTestCtx(fs, taskDefs, pieImpl) {
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
    resource.openRead().buffered().use {
      String(it.readBytes())
    }
  }

  data class Write(val resource: FSResource, val text: String) : Serializable

  val writeDef = taskDef<Write, None>("write") { (resource, text) ->
    resource.openWrite().buffered().use {
      it.write(text.toByteArray())
      it.flush()
    }
    provide(resource)
    None.instance
  }

  val callDef = taskDef<STask<*>, Serializable?>("call") {
    require(it)
  }

  data class Call(val task1: STask<*>, val task2: STask<*>) : Serializable

  val call2Def = taskDef<Call, None>("call2") { (task1, task2) ->
    require(task1)
    require(task2)
    None.instance
  }
  val call2IfContainsGalaxyDef = taskDef<Call, None>("call2IfContainsGalaxyDef") { (task1, task2) ->
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
    addTaskDef(call2Def)
    addTaskDef(call2IfContainsGalaxyDef)
  }

  inner class GCSetup {
    val file0 = resource("/file0")
    val file1 = resource("/file1")
    val file2 = resource("/file2")
    val file3 = resource("/file3")
    val file4 = resource("/file4")
    val file5 = resource("/file5")

    val iTask = writeDef.createTask(Write(file0, "Hello, world 0!"))
    val jTask = readDef.createTask(file1)
    val dTask = call2Def.createTask(Call(iTask.toSerializableTask(), jTask.toSerializableTask()))
    val eTask = writeDef.createTask(Write(file2, "Hello, world 2!"))
    val aTask = call2Def.createTask(Call(dTask.toSerializableTask(), eTask.toSerializableTask()))

    val kTask = noopTask
    val lTask = writeDef.createTask(Write(file3, "Hello, world 3!"))
    val fTask = call2Def.createTask(Call(kTask.toSerializableTask(), lTask.toSerializableTask()))
    val bTask = call2Def.createTask(Call(eTask.toSerializableTask(), fTask.toSerializableTask()))

    val gTask = readDef.createTask(file4)
    val hTask = writeDef.createTask(Write(file5, "Hello, world 5!"))
    val cTask = call2Def.createTask(Call(gTask.toSerializableTask(), hTask.toSerializableTask()))

    init {
      write("Hello, world 1!", file1)
      write("Hello, world 4!", file4)
      newSession().use { session ->
        session.require(aTask)
        session.require(bTask)
        session.require(cTask)
        session.require(eTask)

        session.unobserve(aTask)
        session.unobserve(bTask)
      }
    }
  }
}

class ObservabilityTestBuilder : RuntimeTestBuilder<ObservabilityTestCtx>(true, true,
  { fs, taskDefs, pie -> ObservabilityTestCtx(fs, taskDefs, pie as TestPieImpl) }
)
