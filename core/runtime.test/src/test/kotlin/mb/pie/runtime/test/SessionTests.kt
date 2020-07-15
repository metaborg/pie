package mb.pie.runtime.test

import mb.pie.api.MapTaskDefs
import mb.pie.api.None
import mb.pie.api.exec.NullCancelableToken
import mb.pie.api.test.toLowerCase
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestFactory
import java.nio.file.FileSystem

class SessionTests {
  private val builder = RuntimeTestBuilder(true, false) { fs, taskDefs, pie ->
    SessionTestCtx(fs, taskDefs, pie as TestPieImpl)
  }


  /// Valid `updateAffectedBy` usage.

  @TestFactory
  fun testUpdateAffectedByOnce() = builder.test {
    newSession().use { session ->
      session.updateAffectedBy(hashSetOf())
    }
  }

  @TestFactory
  fun testUpdateAffectedByDifferentSessions() = builder.test {
    newSession().use { session ->
      session.updateAffectedBy(hashSetOf())
    }
    newSession().use { session ->
      session.updateAffectedBy(hashSetOf())
    }
  }


  /// Valid `require`/`requireWithoutObserving` usage.

  @TestFactory
  fun testRequire1Once() = builder.test {
    newSession().use { session ->
      session.require(toLowerCaseTask)
    }
  }

  @TestFactory
  fun testRequire2Once() = builder.test {
    newSession().use { session ->
      session.require(toLowerCaseTask, NullCancelableToken.instance)
    }
  }

  @TestFactory
  fun testRequireWithoutObserving1Once() = builder.test {
    newSession().use { session ->
      session.requireWithoutObserving(toLowerCaseTask)
    }
  }

  @TestFactory
  fun testRequireWithoutObserving2Once() = builder.test {
    newSession().use { session ->
      session.requireWithoutObserving(toLowerCaseTask, NullCancelableToken.instance)
    }
  }

  @TestFactory
  fun testRequireMix() = builder.test {
    newSession().use { session ->
      session.require(toLowerCaseTask)
      session.require(toLowerCaseTask, NullCancelableToken.instance)
      session.requireWithoutObserving(toLowerCaseTask)
      session.requireWithoutObserving(toLowerCaseTask, NullCancelableToken.instance)
    }
  }

  @TestFactory
  fun testRequire1Sessions() = builder.test {
    newSession().use { session ->
      session.require(toLowerCaseTask)
    }
    newSession().use { session ->
      session.require(toLowerCaseTask)
    }
  }

  @TestFactory
  fun testRequire2Sessions() = builder.test {
    newSession().use { session ->
      session.require(toLowerCaseTask, NullCancelableToken.instance)
    }
    newSession().use { session ->
      session.require(toLowerCaseTask, NullCancelableToken.instance)
    }
  }

  @TestFactory
  fun testRequireWithoutObserving1Sessions() = builder.test {
    newSession().use { session ->
      session.requireWithoutObserving(toLowerCaseTask)
    }
    newSession().use { session ->
      session.requireWithoutObserving(toLowerCaseTask)
    }
  }

  @TestFactory
  fun testRequireWithoutObserving2Sessions() = builder.test {
    newSession().use { session ->
      session.requireWithoutObserving(toLowerCaseTask, NullCancelableToken.instance)
    }
    newSession().use { session ->
      session.requireWithoutObserving(toLowerCaseTask, NullCancelableToken.instance)
    }
  }

  @TestFactory
  fun testRequireMixSessions() = builder.test {
    newSession().use { session ->
      session.require(toLowerCaseTask)
    }
    newSession().use { session ->
      session.require(toLowerCaseTask, NullCancelableToken.instance)
    }
    newSession().use { session ->
      session.requireWithoutObserving(toLowerCaseTask)
    }
    newSession().use { session ->
      session.requireWithoutObserving(toLowerCaseTask, NullCancelableToken.instance)
    }
  }


  /// Valid `updateAffectedBy` + `require`/`requireWithoutObserving` usage.

  @TestFactory
  fun testRequire1AfterUpdateAffectedBySessions() = builder.test {
    newSession().use { session ->
      session.updateAffectedBy(hashSetOf())
    }
    newSession().use { session ->
      session.require(toLowerCaseTask)
    }
  }

  @TestFactory
  fun testRequire2AfterUpdateAffectedBySessions() = builder.test {
    newSession().use { session ->
      session.updateAffectedBy(hashSetOf())
    }
    newSession().use { session ->
      session.require(toLowerCaseTask, NullCancelableToken.instance)
    }
  }

  @TestFactory
  fun testRequireWithoutObserving1AfterUpdateAffectedBySessions() = builder.test {
    newSession().use { session ->
      session.updateAffectedBy(hashSetOf())
    }
    newSession().use { session ->
      session.requireWithoutObserving(toLowerCaseTask)
    }
  }

  @TestFactory
  fun testRequireWithoutObserving2AfterUpdateAffectedBySessions() = builder.test {
    newSession().use { session ->
      session.updateAffectedBy(hashSetOf())
    }
    newSession().use { session ->
      session.requireWithoutObserving(toLowerCaseTask, NullCancelableToken.instance)
    }
  }

  @TestFactory
  fun testRequire1AfterUpdateAffectedBy() = builder.test {
    newSession().use { session ->
      val afterSession = session.updateAffectedBy(hashSetOf())
      afterSession.require(toLowerCaseTask)
    }
  }

  @TestFactory
  fun testRequire2AfterUpdateAffectedBy() = builder.test {
    newSession().use { session ->
      val afterSession = session.updateAffectedBy(hashSetOf())
      afterSession.require(toLowerCaseTask, NullCancelableToken.instance)
    }
  }

  @TestFactory
  fun testRequireWithoutObserving1AfterUpdateAffectedBy() = builder.test {
    newSession().use { session ->
      val afterSession = session.updateAffectedBy(hashSetOf())
      afterSession.requireWithoutObserving(toLowerCaseTask)
    }
  }

  @TestFactory
  fun testRequireWithoutObserving2AfterUpdateAffectedBy() = builder.test {
    newSession().use { session ->
      val afterSession = session.updateAffectedBy(hashSetOf())
      afterSession.requireWithoutObserving(toLowerCaseTask, NullCancelableToken.instance)
    }
  }

  @TestFactory
  fun testGetOutput() = builder.test {
    newSession().use { it.require(toLowerCaseTask) } // Require once to create the task.
    newSession().use { session ->
      val afterSession = session.updateAffectedBy(hashSetOf())
      Assertions.assertEquals("abc", afterSession.getOutput(toLowerCaseTask))
    }
  }


  /// Invalid `updateAffectedBy` usage.

  @TestFactory
  fun testUpdateAffectedByMultipleFails() = builder.test {
    newSession().use { session ->
      session.updateAffectedBy(hashSetOf())
      Assertions.assertThrows(IllegalStateException::class.java) {
        session.updateAffectedBy(hashSetOf())
      }
      Assertions.assertThrows(IllegalStateException::class.java) {
        session.updateAffectedBy(hashSetOf())
      }
    }
  }

  @TestFactory
  fun testUpdateAffectedByAfterRequire1Fails() = builder.test {
    newSession().use { session ->
      session.require(toLowerCaseTask)
      Assertions.assertThrows(IllegalStateException::class.java) {
        session.updateAffectedBy(hashSetOf())
      }
    }
  }

  @TestFactory
  fun testUpdateAffectedByAfterRequire2Fails() = builder.test {
    newSession().use { session ->
      session.require(toLowerCaseTask, NullCancelableToken.instance)
      Assertions.assertThrows(IllegalStateException::class.java) {
        session.updateAffectedBy(hashSetOf())
      }
    }
  }

  @TestFactory
  fun testUpdateAffectedByAfterRequireWithoutObserving1Fails() = builder.test {
    newSession().use { session ->
      session.requireWithoutObserving(toLowerCaseTask)
      Assertions.assertThrows(IllegalStateException::class.java) {
        session.updateAffectedBy(hashSetOf())
      }
    }
  }

  @TestFactory
  fun testUpdateAffectedByAfterRequireWithoutObserving2Fails() = builder.test {
    newSession().use { session ->
      session.requireWithoutObserving(toLowerCaseTask, NullCancelableToken.instance)
      Assertions.assertThrows(IllegalStateException::class.java) {
        session.updateAffectedBy(hashSetOf())
      }
    }
  }


  /// Invalid `require`/`requireWithoutObserving` usage.

  @TestFactory
  fun testRequire1AfterUpdateAffectedByFails() = builder.test {
    newSession().use { session ->
      session.updateAffectedBy(hashSetOf())
      Assertions.assertThrows(IllegalStateException::class.java) {
        session.require(toLowerCaseTask)
      }
      Assertions.assertThrows(IllegalStateException::class.java) {
        session.require(toLowerCaseTask)
      }
    }
  }

  @TestFactory
  fun testRequire2AfterUpdateAffectedByFails() = builder.test {
    newSession().use { session ->
      session.updateAffectedBy(hashSetOf())
      Assertions.assertThrows(IllegalStateException::class.java) {
        session.require(toLowerCaseTask, NullCancelableToken.instance)
      }
      Assertions.assertThrows(IllegalStateException::class.java) {
        session.require(toLowerCaseTask, NullCancelableToken.instance)
      }
    }
  }

  @TestFactory
  fun testRequireWithoutObserving1AfterUpdateAffectedByFails() = builder.test {
    newSession().use { session ->
      session.updateAffectedBy(hashSetOf())
      Assertions.assertThrows(IllegalStateException::class.java) {
        session.requireWithoutObserving(toLowerCaseTask)
      }
      Assertions.assertThrows(IllegalStateException::class.java) {
        session.requireWithoutObserving(toLowerCaseTask)
      }
    }
  }

  @TestFactory
  fun testRequireWithoutObserving2AfterUpdateAffectedByFails() = builder.test {
    newSession().use { session ->
      session.updateAffectedBy(hashSetOf())
      Assertions.assertThrows(IllegalStateException::class.java) {
        session.requireWithoutObserving(toLowerCaseTask, NullCancelableToken.instance)
      }
      Assertions.assertThrows(IllegalStateException::class.java) {
        session.requireWithoutObserving(toLowerCaseTask, NullCancelableToken.instance)
      }
    }
  }


  /// Invalid `getOutput` usage.

  @TestFactory
  fun testGetOutputFailsDoesNotExist() = builder.test {
    newSession().use { session ->
      val afterSession = session.updateAffectedBy(hashSetOf())
      Assertions.assertThrows(IllegalStateException::class.java) {
        afterSession.getOutput(toLowerCaseTask) // Try to get output of non-existent task.
      }
    }
  }

  @TestFactory
  fun testGetOutputFailsDifferentInput() = builder.test {
    newSession().use { it.require(singletonDef.createTask(true)) } // Create task with input `true`.
    newSession().use { session ->
      val afterSession = session.updateAffectedBy(hashSetOf())
      Assertions.assertThrows(IllegalStateException::class.java) {
        afterSession.getOutput(singletonDef.createTask(false)) // Try to get output with input `false`.
      }
    }
  }
}


class SessionTestCtx(fs: FileSystem, taskDefs: MapTaskDefs, pieImpl: TestPieImpl) : RuntimeTestCtx(fs, taskDefs, pieImpl) {
  val toLowerCaseTask = toLowerCase.createTask("ABC")

  val singletonDef = taskDef<Boolean, None>("singleton", { _ -> None.instance }) { None.instance }

  init {
    addTaskDef(toLowerCase)
    addTaskDef(singletonDef)
  }
}
