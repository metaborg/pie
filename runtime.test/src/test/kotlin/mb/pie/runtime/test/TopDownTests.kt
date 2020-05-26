package mb.pie.runtime.test

import com.nhaarman.mockitokotlin2.*
import mb.pie.api.*
import mb.pie.api.test.*
import mb.pie.runtime.exec.NoData
import mb.pie.runtime.layer.ValidationException
import mb.resource.fs.FSResource
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestFactory
import java.io.Serializable

class TopDownTests {
  private val builder = DefaultRuntimeTestBuilder()


  @TestFactory
  fun testExec() = builder.test {
    val lowerDef = spy(toLowerCase)
    addTaskDef(lowerDef)

    newSession().use { session ->
      val task = lowerDef.createTask("CAPITALIZED")
      val key = task.key()
      val output = session.require(task)
      assertEquals("capitalized", output)

      val topDownSession = session.topDownRunner
      inOrder(topDownSession, lowerDef) {
        verify(topDownSession, times(1)).requireInitial(eq(task), any(), anyC())
        verify(topDownSession, times(1)).exec(eq(key), eq(task), eq(NoData()), any(), anyC())
      }
    }
  }

  @TestFactory
  fun testExecMultiple() = builder.test {
    val taskDef = spy(toLowerCase)
    addTaskDef(taskDef)

    val output1 = newSession().use { session ->
      val task = taskDef.createTask("CAPITALIZED")
      val key = task.key()
      val output = session.require(task)
      assertEquals("capitalized", output)
      val topDownSession = session.topDownRunner
      inOrder(topDownSession) {
        verify(topDownSession, times(1)).requireInitial(eq(task), any(), anyC())
        verify(topDownSession, times(1)).exec(eq(key), eq(task), eq(NoData()), any(), anyC())
      }
      output
    }

    val output2 = newSession().use { session ->
      val task = taskDef.createTask("CAPITALIZED_EVEN_MORE")
      val key = task.key()
      val output = session.require(task)
      assertEquals("capitalized_even_more", output)
      val topDownSession = session.topDownRunner
      inOrder(topDownSession) {
        verify(topDownSession, times(1)).requireInitial(eq(task), any(), anyC())
        verify(topDownSession, times(1)).exec(eq(key), eq(task), eq(NoData()), any(), anyC())
      }
      output
    }

    assertNotEquals(output1, output2)
  }

  @TestFactory
  fun testReuse() = builder.test {
    val lowerDef = spy(toLowerCase)
    addTaskDef(lowerDef)

    val task = lowerDef.createTask("CAPITALIZED")
    val key = task.key()

    val output1 = newSession().use { session ->
      val output = session.require(task)
      assertEquals("capitalized", output)
      output
    }

    val output2 = newSession().use { session ->
      val output = session.require(task)
      assertEquals("capitalized", output)
      // Result is reused if exec is never called.
      verify(session.topDownRunner, never()).exec(eq(key), eq(task), eq(NoData()), any(), anyC())
      output
    }

    assertEquals(output1, output2)
  }

  @TestFactory
  fun testResourceRequire() = builder.test {
    val readDef = spy(readResource)
    addTaskDef(readDef)

    val file = resource("/file")
    val task = readDef.createTask(file)
    val key = task.key()
    write("HELLO WORLD!", file)

    // Build 'readPath', observe rebuild.
    newSession().use { session ->
      val output = session.require(task)
      assertEquals("HELLO WORLD!", output)
      verify(session.topDownRunner, times(1)).exec(eq(key), eq(task), eq(NoData()), any(), anyC())
    }

    // No changes - exec 'readPath', observe no rebuild.
    newSession().use { session ->
      val output = session.require(task)
      assertEquals("HELLO WORLD!", output)
      verify(session.topDownRunner, never()).exec(eq(key), eq(task), anyER(), any(), anyC())
    }

    // Change required file in such a way that the output of 'readPath' changes (change file content).
    write("!DLROW OLLEH", file)

    // Run again - expect rebuild.
    newSession().use { session ->
      val output = session.require(task)
      assertEquals("!DLROW OLLEH", output)
      verify(session.topDownRunner, times(1)).exec(eq(key), eq(task), check {
        val reason = it as? InconsistentResourceRequire
        assertNotNull(reason)
        assertEquals(file.key, reason!!.dep.key)
      }, any(), anyC())
    }
  }

  @TestFactory
  fun testResourceProvide() = builder.test {
    val writeDef = spy(writeResource)
    addTaskDef(writeDef)

    val file = resource("/file")
    assertTrue(!file.exists())

    val task = writeDef.createTask(Pair("HELLO WORLD!", file))
    val key = task.key()

    // Build 'writePath', observe rebuild and existence of file
    newSession().use { session ->
      session.require(task)
      verify(session.topDownRunner, times(1)).exec(eq(key), eq(task), eq(NoData()), any(), anyC())
    }

    assertTrue(file.exists())
    assertEquals("HELLO WORLD!", read(file))

    // No changes - exec 'writePath', observe no rebuild, no change to file
    newSession().use { session ->
      session.require(task)
      verify(session.topDownRunner, never()).exec(eq(key), eq(task), anyER(), any(), anyC())
    }

    // Change generated file in such a way that 'writePath' is rebuilt (change file content)
    write("!DLROW OLLEH", file)

    // Build 'writePath', observe rebuild and change of file
    newSession().use { session ->
      session.require(task)
      verify(session.topDownRunner, times(1)).exec(eq(key), eq(task), check {
        val reason = it as? InconsistentResourceProvide
        assertNotNull(reason)
        assertEquals(file.key, reason!!.dep.key)
      }, any(), anyC())
    }

    assertEquals("HELLO WORLD!", read(file))
  }

  @TestFactory
  fun testTaskRequire() = builder.test {
    val lowerDef = spy(toLowerCase)
    addTaskDef(lowerDef)
    val readDef = spy(readResource)
    addTaskDef(readDef)

    val file = resource("/file")

    val readTask = readDef.createTask(file)
    val readKey = readTask.key()

    val combDef = spy(taskDef<FSResource, String>("combine", { it, _ -> "combine($it)" }) {
      val text = require(readTask)
      require(lowerDef, text)
    })
    addTaskDef(combDef)

    val str = "HELLO WORLD!"
    write(str, file)

    val lowerTask = lowerDef.createTask(str)
    val lowerKey = lowerTask.key()
    val combTask = combDef.createTask(file)
    val combKey = combTask.key()

    // Build 'combine', observe rebuild of all.
    newSession().use { session ->
      val output = session.require(combTask)
      assertEquals("hello world!", output)
      val topDownSession = session.topDownRunner
      inOrder(topDownSession) {
        verify(topDownSession, times(1)).exec(eq(combKey), eq(combTask), eq(NoData()), any(), anyC())
        verify(topDownSession, times(1)).exec(eq(readKey), eq(readTask), eq(NoData()), any(), anyC())
        verify(topDownSession, times(1)).exec(eq(lowerKey), eq(lowerTask), eq(NoData()), any(), anyC())
      }
    }

    // No changes - exec 'combine', observe no rebuild.
    newSession().use { session ->
      val output = session.require(combTask)
      assertEquals("hello world!", output)
      val topDownSession = session.topDownRunner
      verify(topDownSession, never()).exec(eq(combKey), eq(combTask), anyER(), any(), anyC())
      verify(topDownSession, never()).exec(eq(readKey), eq(readTask), anyER(), any(), anyC())
      verify(topDownSession, never()).exec(eq(lowerKey), eq(lowerTask), anyER(), any(), anyC())
    }

    // Change required file in such a way that the output of 'readPath' changes (change file content).
    val newStr = "!DLROW OLLEH"
    write(newStr, file)

    val lowerRevTask = lowerDef.createTask(newStr)
    val lowerRevKey = lowerRevTask.key()

    // Build 'combine', observe rebuild of all in dependency order
    newSession().use { session ->
      val output = session.require(combTask)
      assertEquals("!dlrow olleh", output)
      val topDownSession = session.topDownRunner
      inOrder(topDownSession) {
        verify(topDownSession, times(1)).requireInitial(eq(combTask), any(), anyC())
        verify(topDownSession, times(1)).exec(eq(readKey), eq(readTask), check {
          val reason = it as? InconsistentResourceRequire
          assertNotNull(reason)
          assertEquals(file.key, reason!!.dep.key)
        }, any(), anyC())
        verify(topDownSession, times(1)).exec(eq(combKey), eq(combTask), check {
          val reason = it as? InconsistentTaskReq
          assertNotNull(reason)
          assertEquals(readKey, reason!!.dep.callee)
        }, any(), anyC())
        verify(topDownSession, times(1)).exec(eq(lowerRevKey), eq(lowerRevTask), eq(NoData()), any(), anyC())
      }
    }

    // Change required file in such a way that the output of 'readPath' does not change (change modification date).
    // NOTE: must add at least one millisecond, as filesystems may ignore nanosecond changes.
    file.lastModifiedTime = file.lastModifiedTime.plusMillis(1)

    // Build 'combine', observe rebuild of 'readPath' only
    newSession().use { session ->
      val output = session.require(combTask)
      assertEquals("!dlrow olleh", output)
      val topDownSession = session.topDownRunner
      inOrder(topDownSession) {
        verify(topDownSession, times(1)).requireInitial(eq(combTask), any(), anyC())
        verify(topDownSession, times(1)).exec(eq(readKey), eq(readTask), check {
          val reason = it as? InconsistentResourceRequire
          assertNotNull(reason)
          assertEquals(file.key, reason!!.dep.key)
        }, any(), anyC())
      }
      verify(topDownSession, never()).exec(eq(combKey), eq(combTask), anyER(), any(), anyC())
      verify(topDownSession, never()).exec(eq(lowerRevKey), eq(lowerRevTask), anyER(), any(), anyC())
    }
  }

  @TestFactory
  fun testOverlappingProvidedResourceTriggersValidationError() = builder.test {
    addTaskDef(writeResource)

    val file = resource("/file")
    assertThrows(ValidationException::class.java) {
      newSession().use { session ->
        session.require(writeResource.createTask(Pair("HELLO WORLD 1!", file)))
        session.require(writeResource.createTask(Pair("HELLO WORLD 2!", file)))
      }
    }

    // Overlapping generated file exception should also trigger between separate execs
    assertThrows(ValidationException::class.java) {
      newSession().use { session ->
        session.require(writeResource.createTask(Pair("HELLO WORLD 3!", file)))
      }
    }
  }

  @TestFactory
  fun testProvideRequiredResourceHiddenDepTriggersValidationError() = builder.test {
    addTaskDef(readResource)
    addTaskDef(writeResource)

    val file = resource("/file")
    write("HELLO WORLD!", file)

    newSession().use { session ->
      session.require(readResource.createTask(file))
      assertThrows(ValidationException::class.java) {
        session.require(writeResource.createTask(Pair("HELLO WORLD!", file)))
      }
    }

    newSession().use { session ->
      assertThrows(ValidationException::class.java) {
        // Hidden dependency exception should also trigger between separate sessions.
        session.require(writeResource.createTask(Pair("HELLO WORLD!", file)))
      }
    }
  }

  @TestFactory
  fun testRequireProvidedResourceHiddenDepTriggersValidationError() = builder.test {
    addTaskDef(writeResource)
    addTaskDef(readResource)
    val indirectionDef = requireOutputFunc<None>()
    addTaskDef(indirectionDef)

    val combineIncorrect = taskDef<Pair<String, FSResource>, String>("combineIncorrect", { input, _ -> "combine($input)" }) { (text, path) ->
      require(indirectionDef.createTask(writeResource.createSupplier(Pair(text, path))))
      require(readResource.createTask(path))
    }
    addTaskDef(combineIncorrect)

    newSession().use { session ->
      val file = resource("/file1")
      assertThrows(ValidationException::class.java) {
        session.require(combineIncorrect.createTask(Pair("HELLO WORLD!", file)))
      }
    }

    val combineStillIncorrect = taskDef<Pair<String, FSResource>, String>("combineStillIncorrect", { input, _ -> "combine($input)" }) { (text, path) ->
      require(indirectionDef.createTask(writeResource.createSupplier(Pair(text, path))))
      require(writeResource.createTask(Pair(text, path)))
      require(readResource.createTask(path))
    }
    addTaskDef(combineStillIncorrect)

    newSession().use { session ->
      val file = resource("/file2")
      assertThrows(ValidationException::class.java) {
        session.require(combineStillIncorrect.createTask(Pair("HELLO WORLD!", file)))
      }
    }
  }

  @TestFactory
  fun testCyclicDependencyTriggersValidationError() = builder.test {
    val cyclicDef = taskDef<None, None>("b1", { _, _ -> "b1" }) { require(STask<Serializable?>("b1", None.instance)) as None }
    addTaskDef(cyclicDef)

    newSession().use { session ->
      assertThrows(ValidationException::class.java) {
        session.require(cyclicDef.createTask(None.instance))
      }
    }
  }

  @TestFactory
  fun testDifferentInputsTriggersValidationError() = builder.test {
    val singletonTaskDef = taskDef<Int, None>("singleton", { _ -> None.instance }) {
      println(it)
      None.instance
    }
    addTaskDef(singletonTaskDef)

    newSession().use { session ->
      session.require(singletonTaskDef.createTask(1))
      assertThrows(ValidationException::class.java) {
        session.require(singletonTaskDef.createTask(2))
      }
    }

    val requireSingletonTaskDef = taskDef<Int, None>("requireSingletonTaskDef") {
      require(singletonTaskDef.createTask(it))
      require(singletonTaskDef.createTask(it + 1))
      None.instance
    }
    addTaskDef(requireSingletonTaskDef)

    newSession().use { session ->
      assertThrows(ValidationException::class.java) {
        session.require(requireSingletonTaskDef.createTask(1))
      }
    }
  }
}
