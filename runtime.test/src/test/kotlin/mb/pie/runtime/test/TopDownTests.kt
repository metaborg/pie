package mb.pie.runtime.test

import com.nhaarman.mockito_kotlin.*
import mb.fs.java.JavaFSNode
import mb.pie.api.*
import mb.pie.api.fs.ResourceUtils
import mb.pie.api.test.*
import mb.pie.runtime.exec.NoData
import mb.pie.runtime.layer.ValidationException
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

    val session = spy(topDownSession())
    val output = session.requireInitial(task)
    assertEquals("capitalized", output)

    inOrder(session, taskDef) {
      verify(session, times(1)).requireInitial(eq(task), anyC())
      verify(session, times(1)).exec(eq(key), eq(task), eq(NoData()), anyC())
    }
  }

  @TestFactory
  fun testExecMultiple() = RuntimeTestGenerator.generate("testExecMultiple") {
    val taskDef = spy(toLowerCase)
    addTaskDef(taskDef)

    val input1 = "CAPITALIZED"
    val task1 = task(taskDef, input1)
    val key1 = task1.key()
    val session1 = spy(topDownSession())
    val output1 = session1.requireInitial(task1)
    assertEquals("capitalized", output1)

    val input2 = "CAPITALIZED_EVEN_MORE"
    val task2 = task(taskDef, input2)
    val key2 = task2.key()
    val session2 = spy(topDownSession())
    val output2 = session2.requireInitial(task2)
    assertEquals("capitalized_even_more", output2)

    assertNotEquals(output1, output2)

    inOrder(taskDef, session1, session2) {
      verify(session1, times(1)).requireInitial(eq(task1), anyC())
      verify(session1, times(1)).exec(eq(key1), eq(task1), eq(NoData()), anyC())

      verify(session2, times(1)).requireInitial(eq(task2), anyC())
      verify(session2, times(1)).exec(eq(key2), eq(task2), eq(NoData()), anyC())
    }
  }

  @TestFactory
  fun testReuse() = RuntimeTestGenerator.generate("testReuse") {
    val taskDef = spy(toLowerCase)
    addTaskDef(taskDef)

    val input = "CAPITALIZED"
    val task = task(taskDef, input)
    val key = task.key()

    val session1 = topDownSession()
    val output1 = session1.requireInitial(task)
    assertEquals("capitalized", output1)

    val session2 = spy(topDownSession())
    val output2 = session2.requireInitial(task)
    assertEquals("capitalized", output2)

    assertEquals(output1, output2)

    // Result is reused if rebuild is never called.
    verify(session2, never()).exec(eq(key), eq(task), eq(NoData()), anyC())
  }

  @TestFactory
  fun testFileReq() = RuntimeTestGenerator.generate("testFileReq") {
    val readPath = spy(readPath)
    addTaskDef(readPath)

    val fileNode = fsNode("/file")
    val task = task(readPath, fileNode)
    val key = task.key()
    write("HELLO WORLD!", fileNode)

    // Build 'readPath', observe rebuild
    val session1 = spy(topDownSession())
    val output1 = session1.requireInitial(task(readPath, fileNode))
    assertEquals("HELLO WORLD!", output1)
    verify(session1, times(1)).exec(eq(key), eq(task), eq(NoData()), anyC())

    // No changes - exec 'readPath', observe no rebuild
    val session2 = spy(topDownSession())
    val output2 = session2.requireInitial(task(readPath, fileNode))
    assertEquals("HELLO WORLD!", output2)
    verify(session2, never()).exec(eq(key), eq(task), anyER(), anyC())

    // Change required file in such a way that the output of 'readPath' changes (change file content)
    write("!DLROW OLLEH", fileNode)

    // Run again - expect rebuild
    val session3 = spy(topDownSession())
    val output3 = session3.requireInitial(task(readPath, fileNode))
    assertEquals("!DLROW OLLEH", output3)
    verify(session3, times(1)).exec(eq(key), eq(task), check {
      val reason = it as? InconsistentResourceRequire
      assertNotNull(reason)
      assertEquals(ResourceUtils.toResourceKey(fileNode), reason!!.dep.key)
    }, anyC())
  }

  @TestFactory
  fun testFileGen() = RuntimeTestGenerator.generate("testFileGen") {
    val writePath = spy(writePath)
    addTaskDef(writePath)

    val fileNode = fsNode("/file")
    assertTrue(!fileNode.exists())

    val task = task(writePath, Pair("HELLO WORLD!", fileNode))
    val key = task.key()

    // Build 'writePath', observe rebuild and existence of file
    val session1 = spy(topDownSession())
    session1.requireInitial(task)
    verify(session1, times(1)).exec(eq(key), eq(task), eq(NoData()), anyC())

    assertTrue(fileNode.exists())
    assertEquals("HELLO WORLD!", read(fileNode))

    // No changes - exec 'writePath', observe no rebuild, no change to file
    val session2 = spy(topDownSession())
    session2.requireInitial(task)
    verify(session2, never()).exec(eq(key), eq(task), anyER(), anyC())

    // Change generated file in such a way that 'writePath' is rebuilt (change file content)
    write("!DLROW OLLEH", fileNode)

    // Build 'writePath', observe rebuild and change of file
    val session3 = spy(topDownSession())
    session3.requireInitial(task)
    verify(session3, times(1)).exec(eq(key), eq(task), check {
      val reason = it as? InconsistentResourceProvide
      assertNotNull(reason)
      assertEquals(ResourceUtils.toResourceKey(fileNode), reason!!.dep.key)
    }, anyC())

    assertEquals("HELLO WORLD!", read(fileNode))
  }

  @TestFactory
  fun testTaskReq() = RuntimeTestGenerator.generate("testTaskReq") {
    val lowerDef = spy(toLowerCase)
    addTaskDef(lowerDef)
    val readDef = spy(readPath)
    addTaskDef(readDef)

    val fileNode = fsNode("/file")

    val readTask = task(readDef, fileNode)
    val readKey = readTask.key()

    val combDef = spy(taskDef<JavaFSNode, String>("combine", { it, _ -> "combine($it)" }) {
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
    val session1 = spy(topDownSession())
    val output1 = session1.requireInitial(combTask)
    assertEquals("hello world!", output1)
    inOrder(session1) {
      verify(session1, times(1)).exec(eq(combKey), eq(combTask), eq(NoData()), anyC())
      verify(session1, times(1)).exec(eq(readKey), eq(readTask), eq(NoData()), anyC())
      verify(session1, times(1)).exec(eq(lowerKey), eq(lowerTask), eq(NoData()), anyC())
    }

    // No changes - exec 'combine', observe no rebuild.
    val session2 = spy(topDownSession())
    val output2 = session2.requireInitial(combTask)
    assertEquals("hello world!", output2)
    verify(session2, never()).exec(eq(combKey), eq(combTask), anyER(), anyC())
    verify(session2, never()).exec(eq(readKey), eq(readTask), anyER(), anyC())
    verify(session2, never()).exec(eq(lowerKey), eq(lowerTask), anyER(), anyC())

    // Change required file in such a way that the output of 'readPath' changes (change file content).
    val newStr = "!DLROW OLLEH"
    write(newStr, fileNode)

    val lowerRevTask = task(lowerDef, newStr)
    val lowerRevKey = lowerRevTask.key()

    // Build 'combine', observe rebuild of all in dependency order
    val session3 = spy(topDownSession())
    val output3 = session3.requireInitial(combTask)
    assertEquals("!dlrow olleh", output3)
    inOrder(session3) {
      verify(session3, times(1)).requireInitial(eq(combTask), anyC())
      verify(session3, times(1)).exec(eq(readKey), eq(readTask), check {
        val reason = it as? InconsistentResourceRequire
        assertNotNull(reason)
        assertEquals(ResourceUtils.toResourceKey(fileNode), reason!!.dep.key)
      }, anyC())
      verify(session3, times(1)).exec(eq(combKey), eq(combTask), check {
        val reason = it as? InconsistentTaskReq
        assertNotNull(reason)
        assertEquals(readKey, reason!!.dep.callee)
      }, anyC())
      verify(session3, times(1)).exec(eq(lowerRevKey), eq(lowerRevTask), eq(NoData()), anyC())
    }

    // Change required file in such a way that the output of 'readPath' does not change (change modification date).
    // NOTE: must add at least one millisecond, as filesystems may ignore nanosecond changes.
    fileNode.lastModifiedTime = fileNode.lastModifiedTime.plusMillis(1)

    // Build 'combine', observe rebuild of 'readPath' only
    val session4 = spy(topDownSession())
    val output4 = session4.requireInitial(combTask)
    assertEquals("!dlrow olleh", output4)
    inOrder(session4) {
      verify(session4, times(1)).requireInitial(eq(combTask), anyC())
      verify(session4, times(1)).exec(eq(readKey), eq(readTask), check {
        val reason = it as? InconsistentResourceRequire
        assertNotNull(reason)
        assertEquals(ResourceUtils.toResourceKey(fileNode), reason!!.dep.key)
      }, anyC())
    }
    verify(session4, never()).exec(eq(combKey), eq(combTask), anyER(), anyC())
    verify(session4, never()).exec(eq(lowerRevKey), eq(lowerRevTask), anyER(), anyC())
  }

  @TestFactory
  fun testOverlappingGeneratedPath() = RuntimeTestGenerator.generate("testOverlappingGeneratedPath") {
    addTaskDef(writePath)

    val executor = topDownExecutor

    val filePath = fsNode("/file")
    assertThrows(ValidationException::class.java) {
      val session = executor.newSession()
      session.requireInitial(task(writePath, Pair("HELLO WORLD 1!", filePath)))
      session.requireInitial(task(writePath, Pair("HELLO WORLD 2!", filePath)))
    }

    // Overlapping generated file exception should also trigger between separate execs
    assertThrows(ValidationException::class.java) {
      val session = executor.newSession()
      session.requireInitial(task(writePath, Pair("HELLO WORLD 3!", filePath)))
    }
  }

  @TestFactory
  fun testGenerateRequiredHiddenDep() = RuntimeTestGenerator.generate("testGenerateRequiredHiddenDep") {
    addTaskDef(readPath)
    addTaskDef(writePath)

    val executor = topDownExecutor

    val filePath = fsNode("/file")
    write("HELLO WORLD!", filePath)

    assertThrows(ValidationException::class.java) {
      val session = executor.newSession()
      session.requireInitial(task(readPath, filePath))
      session.requireInitial(task(writePath, Pair("HELLO WORLD!", filePath)))
    }

    // Hidden dependency exception should also trigger between separate execs
    assertThrows(ValidationException::class.java) {
      val session = executor.newSession()
      session.requireInitial(task(writePath, Pair("HELLO WORLD!", filePath)))
    }
  }

  @TestFactory
  fun testRequireGeneratedHiddenDep() = RuntimeTestGenerator.generate("testRequireGeneratedHiddenDep") {
    addTaskDef(writePath)
    addTaskDef(readPath)
    val indirection = requireOutputFunc<Pair<String, JavaFSNode>, None>()
    addTaskDef(indirection)

    val executor = topDownExecutor

    val combineIncorrect = spy(taskDef<Pair<String, JavaFSNode>, String>("combineIncorrect", { input, _ -> "combine($input)" }) { (text, path) ->
      require(task(indirection, stask(writePath, Pair(text, path))))
      require(task(readPath, path))
    })
    addTaskDef(combineIncorrect)

    run {
      val fileNode = fsNode("/file1")
      assertThrows(ValidationException::class.java) {
        val session = executor.newSession()
        session.requireInitial(task(combineIncorrect, Pair("HELLO WORLD!", fileNode)))
      }
    }

    val combineStillIncorrect = spy(taskDef<Pair<String, JavaFSNode>, String>("combineStillIncorrect", { input, _ -> "combine($input)" }) { (text, path) ->
      require(task(indirection, stask(writePath, Pair(text, path))))
      require(task(writePath, Pair(text, path)))
      require(task(readPath, path))
    })
    addTaskDef(combineStillIncorrect)

    run {
      val filePath2 = fsNode("/file2")
      assertThrows(ValidationException::class.java) {
        val exec = executor.newSession()
        exec.requireInitial(task(combineStillIncorrect, Pair("HELLO WORLD!", filePath2)))
      }
    }
  }

  @TestFactory
  fun testCyclicDependency() = RuntimeTestGenerator.generate("testCyclicDependency") {
    val cyclicTaskDef = taskDef<None, None>("b1", { _, _ -> "b1" }) { require(stask("b1", None.instance)) as None }
    addTaskDef(cyclicTaskDef)

    val executor = topDownExecutor
    assertThrows(ValidationException::class.java) {
      val session = executor.newSession()
      session.requireInitial(task(cyclicTaskDef, None.instance))
    }
  }
}