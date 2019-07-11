package mb.pie.runtime.test

import com.nhaarman.mockitokotlin2.*
import mb.pie.api.*
import mb.pie.api.exec.NullCancelled
import mb.pie.api.test.*
import mb.pie.runtime.exec.NoData
import mb.pie.runtime.layer.ValidationException
import mb.resource.fs.FSResource
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestFactory

internal class TopDownTests {
  @TestFactory
  fun testExec() = RuntimeTestGenerator.generate("testExec") {
    val taskDef = spy(toLowerCase)
    addTaskDef(taskDef)

    val input = "CAPITALIZED"
    val task = task(taskDef, input)
    val key = task.key()

    val session = spy(newSession().topDownSession)
    val output = session.requireInitial(task, true, NullCancelled())
    assertEquals("capitalized", output)

    inOrder(session, taskDef) {
      verify(session, times(1)).requireInitial(eq(task), eq(true), anyC())
      verify(session, times(1)).exec(eq(key), eq(task), eq(NoData()), eq(true), anyC())
    }
  }

  @TestFactory
  fun testExecMultiple() = RuntimeTestGenerator.generate("testExecMultiple") {
    val taskDef = spy(toLowerCase)
    addTaskDef(taskDef)

    val input1 = "CAPITALIZED"
    val task1 = task(taskDef, input1)
    val key1 = task1.key()
    val session1 = spy(newSession().topDownSession)
    val output1 = session1.requireInitial(task1, true, NullCancelled())
    assertEquals("capitalized", output1)

    val input2 = "CAPITALIZED_EVEN_MORE"
    val task2 = task(taskDef, input2)
    val key2 = task2.key()
    val session2 = spy(newSession().topDownSession)
    val output2 = session2.requireInitial(task2, true, NullCancelled())
    assertEquals("capitalized_even_more", output2)

    assertNotEquals(output1, output2)

    inOrder(taskDef, session1, session2) {
      verify(session1, times(1)).requireInitial(eq(task1), eq(true), anyC())
      verify(session1, times(1)).exec(eq(key1), eq(task1), eq(NoData()), eq(true), anyC())

      verify(session2, times(1)).requireInitial(eq(task2), eq(true), anyC())
      verify(session2, times(1)).exec(eq(key2), eq(task2), eq(NoData()), eq(true), anyC())
    }
  }

  @TestFactory
  fun testReuse() = RuntimeTestGenerator.generate("testReuse") {
    val taskDef = spy(toLowerCase)
    addTaskDef(taskDef)

    val input = "CAPITALIZED"
    val task = task(taskDef, input)
    val key = task.key()

    val session1 = newSession().topDownSession
    val output1 = session1.requireInitial(task, true, NullCancelled())
    assertEquals("capitalized", output1)

    val session2 = spy(newSession().topDownSession)
    val output2 = session2.requireInitial(task, true, NullCancelled())
    assertEquals("capitalized", output2)

    assertEquals(output1, output2)

    // Result is reused if rebuild is never called.
    verify(session2, never()).exec(eq(key), eq(task), eq(NoData()), eq(true), anyC())
  }

  @TestFactory
  fun testFileReq() = RuntimeTestGenerator.generate("testFileReq") {
    val readPath = spy(readResource)
    addTaskDef(readPath)

    val fileNode = resource("/file")
    val task = task(readPath, fileNode)
    val key = task.key()
    write("HELLO WORLD!", fileNode)

    // Build 'readPath', observe rebuild
    val session1 = spy(newSession().topDownSession)
    val output1 = session1.requireInitial(task(readPath, fileNode), true, NullCancelled())
    assertEquals("HELLO WORLD!", output1)
    verify(session1, times(1)).exec(eq(key), eq(task), eq(NoData()), eq(true), anyC())

    // No changes - exec 'readPath', observe no rebuild
    val session2 = spy(newSession().topDownSession)
    val output2 = session2.requireInitial(task(readPath, fileNode), true, NullCancelled())
    assertEquals("HELLO WORLD!", output2)
    verify(session2, never()).exec(eq(key), eq(task), anyER(), eq(true), anyC())

    // Change required file in such a way that the output of 'readPath' changes (change file content)
    write("!DLROW OLLEH", fileNode)

    // Run again - expect rebuild
    val session3 = spy(newSession().topDownSession)
    val output3 = session3.requireInitial(task(readPath, fileNode), true, NullCancelled())
    assertEquals("!DLROW OLLEH", output3)
    verify(session3, times(1)).exec(eq(key), eq(task), check {
      val reason = it as? InconsistentResourceRequire
      assertNotNull(reason)
      assertEquals(fileNode.key, reason!!.dep.key)
    }, eq(true), anyC())
  }

  @TestFactory
  fun testFileGen() = RuntimeTestGenerator.generate("testFileGen") {
    val writePath = spy(writeResource)
    addTaskDef(writePath)

    val fileNode = resource("/file")
    assertTrue(!fileNode.exists())

    val task = task(writePath, Pair("HELLO WORLD!", fileNode))
    val key = task.key()

    // Build 'writePath', observe rebuild and existence of file
    val session1 = spy(newSession().topDownSession)
    session1.requireInitial(task, true, NullCancelled())
    verify(session1, times(1)).exec(eq(key), eq(task), eq(NoData()), eq(true), anyC())

    assertTrue(fileNode.exists())
    assertEquals("HELLO WORLD!", read(fileNode))

    // No changes - exec 'writePath', observe no rebuild, no change to file
    val session2 = spy(newSession().topDownSession)
    session2.requireInitial(task, true, NullCancelled())
    verify(session2, never()).exec(eq(key), eq(task), anyER(), eq(true), anyC())

    // Change generated file in such a way that 'writePath' is rebuilt (change file content)
    write("!DLROW OLLEH", fileNode)

    // Build 'writePath', observe rebuild and change of file
    val session3 = spy(newSession().topDownSession)
    session3.requireInitial(task, true, NullCancelled())
    verify(session3, times(1)).exec(eq(key), eq(task), check {
      val reason = it as? InconsistentResourceProvide
      assertNotNull(reason)
      assertEquals(fileNode.key, reason!!.dep.key)
    }, eq(true), anyC())

    assertEquals("HELLO WORLD!", read(fileNode))
  }

  @TestFactory
  fun testTaskReq() = RuntimeTestGenerator.generate("testTaskReq") {
    val lowerDef = spy(toLowerCase)
    addTaskDef(lowerDef)
    val readDef = spy(readResource)
    addTaskDef(readDef)

    val fileNode = resource("/file")

    val readTask = task(readDef, fileNode)
    val readKey = readTask.key()

    val combDef = spy(taskDef<FSResource, String>("combine", { it, _ -> "combine($it)" }) {
      val text = require(readTask)
      require(lowerDef, text)
    })
    addTaskDef(combDef)

    val str = "HELLO WORLD!"
    write(str, fileNode)

    val lowerTask = task(lowerDef, str)
    val lowerKey = lowerTask.key()
    val combTask = task(combDef, fileNode)
    val combKey = combTask.key()

    // Build 'combine', observe rebuild of all.
    val session1 = spy(newSession().topDownSession)
    val output1 = session1.requireInitial(combTask, true, NullCancelled())
    assertEquals("hello world!", output1)
    inOrder(session1) {
      verify(session1, times(1)).exec(eq(combKey), eq(combTask), eq(NoData()), eq(true), anyC())
      verify(session1, times(1)).exec(eq(readKey), eq(readTask), eq(NoData()), eq(true), anyC())
      verify(session1, times(1)).exec(eq(lowerKey), eq(lowerTask), eq(NoData()), eq(true), anyC())
    }

    // No changes - exec 'combine', observe no rebuild.
    val session2 = spy(newSession().topDownSession)
    val output2 = session2.requireInitial(combTask, true, NullCancelled())
    assertEquals("hello world!", output2)
    verify(session2, never()).exec(eq(combKey), eq(combTask), anyER(), eq(true), anyC())
    verify(session2, never()).exec(eq(readKey), eq(readTask), anyER(), eq(true), anyC())
    verify(session2, never()).exec(eq(lowerKey), eq(lowerTask), anyER(), eq(true), anyC())

    // Change required file in such a way that the output of 'readPath' changes (change file content).
    val newStr = "!DLROW OLLEH"
    write(newStr, fileNode)

    val lowerRevTask = task(lowerDef, newStr)
    val lowerRevKey = lowerRevTask.key()

    // Build 'combine', observe rebuild of all in dependency order
    val session3 = spy(newSession().topDownSession)
    val output3 = session3.requireInitial(combTask, true, NullCancelled())
    assertEquals("!dlrow olleh", output3)
    inOrder(session3) {
      verify(session3, times(1)).requireInitial(eq(combTask), eq(true), anyC())
      verify(session3, times(1)).exec(eq(readKey), eq(readTask), check {
        val reason = it as? InconsistentResourceRequire
        assertNotNull(reason)
        assertEquals(fileNode.key, reason!!.dep.key)
      }, eq(true), anyC())
      verify(session3, times(1)).exec(eq(combKey), eq(combTask), check {
        val reason = it as? InconsistentTaskReq
        assertNotNull(reason)
        assertEquals(readKey, reason!!.dep.callee)
      }, eq(true), anyC())
      verify(session3, times(1)).exec(eq(lowerRevKey), eq(lowerRevTask), eq(NoData()), eq(true), anyC())
    }

    // Change required file in such a way that the output of 'readPath' does not change (change modification date).
    // NOTE: must add at least one millisecond, as filesystems may ignore nanosecond changes.
    fileNode.lastModifiedTime = fileNode.lastModifiedTime.plusMillis(1)

    // Build 'combine', observe rebuild of 'readPath' only
    val session4 = spy(newSession().topDownSession)
    val output4 = session4.requireInitial(combTask, true, NullCancelled())
    assertEquals("!dlrow olleh", output4)
    inOrder(session4) {
      verify(session4, times(1)).requireInitial(eq(combTask), eq(true), anyC())
      verify(session4, times(1)).exec(eq(readKey), eq(readTask), check {
        val reason = it as? InconsistentResourceRequire
        assertNotNull(reason)
        assertEquals(fileNode.key, reason!!.dep.key)
      }, eq(true), anyC())
    }
    verify(session4, never()).exec(eq(combKey), eq(combTask), anyER(), eq(true), anyC())
    verify(session4, never()).exec(eq(lowerRevKey), eq(lowerRevTask), anyER(), eq(true), anyC())
  }

  @TestFactory
  fun testOverlappingGeneratedPath() = RuntimeTestGenerator.generate("testOverlappingGeneratedPath") {
    addTaskDef(writeResource)

    val filePath = resource("/file")
    assertThrows(ValidationException::class.java) {
      val session = newSession().topDownSession
      session.requireInitial(task(writeResource, Pair("HELLO WORLD 1!", filePath)), true, NullCancelled())
      session.requireInitial(task(writeResource, Pair("HELLO WORLD 2!", filePath)), true, NullCancelled())
    }

    // Overlapping generated file exception should also trigger between separate execs
    assertThrows(ValidationException::class.java) {
      val session = newSession().topDownSession
      session.requireInitial(task(writeResource, Pair("HELLO WORLD 3!", filePath)), true, NullCancelled())
    }
  }

  @TestFactory
  fun testGenerateRequiredHiddenDep() = RuntimeTestGenerator.generate("testGenerateRequiredHiddenDep") {
    addTaskDef(readResource)
    addTaskDef(writeResource)

    val filePath = resource("/file")
    write("HELLO WORLD!", filePath)

    assertThrows(ValidationException::class.java) {
      val session = newSession().topDownSession
      session.requireInitial(task(readResource, filePath), true, NullCancelled())
      session.requireInitial(task(writeResource, Pair("HELLO WORLD!", filePath)), true, NullCancelled())
    }

    // Hidden dependency exception should also trigger between separate execs
    assertThrows(ValidationException::class.java) {
      val session = newSession().topDownSession
      session.requireInitial(task(writeResource, Pair("HELLO WORLD!", filePath)), true, NullCancelled())
    }
  }

  @TestFactory
  fun testRequireGeneratedHiddenDep() = RuntimeTestGenerator.generate("testRequireGeneratedHiddenDep") {
    addTaskDef(writeResource)
    addTaskDef(readResource)
    val indirection = requireOutputFunc<None>()
    addTaskDef(indirection)

    val combineIncorrect = spy(taskDef<Pair<String, FSResource>, String>("combineIncorrect", { input, _ -> "combine($input)" }) { (text, path) ->
      require(task(indirection, stask(writeResource, Pair(text, path))))
      require(task(readResource, path))
    })
    addTaskDef(combineIncorrect)

    run {
      val fileNode = resource("/file1")
      assertThrows(ValidationException::class.java) {
        val session = newSession().topDownSession
        session.requireInitial(task(combineIncorrect, Pair("HELLO WORLD!", fileNode)), true, NullCancelled())
      }
    }

    val combineStillIncorrect = spy(taskDef<Pair<String, FSResource>, String>("combineStillIncorrect", { input, _ -> "combine($input)" }) { (text, path) ->
      require(task(indirection, stask(writeResource, Pair(text, path))))
      require(task(writeResource, Pair(text, path)))
      require(task(readResource, path))
    })
    addTaskDef(combineStillIncorrect)

    run {
      val filePath2 = resource("/file2")
      assertThrows(ValidationException::class.java) {
        val exec = newSession().topDownSession
        exec.requireInitial(task(combineStillIncorrect, Pair("HELLO WORLD!", filePath2)), true, NullCancelled())
      }
    }
  }

  @TestFactory
  fun testCyclicDependency() = RuntimeTestGenerator.generate("testCyclicDependency") {
    val cyclicTaskDef = taskDef<None, None>("b1", { _, _ -> "b1" }) { require(stask("b1", None.instance)) as None }
    addTaskDef(cyclicTaskDef)

    assertThrows(ValidationException::class.java) {
      val session = newSession().topDownSession
      session.requireInitial(task(cyclicTaskDef, None.instance), true, NullCancelled())
    }
  }
}