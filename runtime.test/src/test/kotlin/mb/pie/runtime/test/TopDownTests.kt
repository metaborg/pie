package mb.pie.runtime.test

import com.nhaarman.mockito_kotlin.*
import mb.pie.api.*
import mb.pie.api.test.*
import mb.pie.runtime.exec.NoResultReason
import mb.pie.runtime.layer.ValidationException
import mb.pie.vfs.path.PPath
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestFactory
import java.nio.file.Files
import java.nio.file.attribute.FileTime

internal class TopDownTests {
  @TestFactory
  fun testExec() = RuntimeTestGenerator.generate("testExec") {
    val input = "CAPITALIZED"
    val func = spy(toLowerCase)
    addTaskDef(func)
    val app = app(func, input)

    val exec = spy(topDownExec())
    val output = exec.requireInitial(app)
    assertEquals("capitalized", output)

    inOrder(exec, func) {
      verify(exec, times(1)).requireInitial(eq(app), any())
      verify(exec, times(1)).exec(eq(app), eq(NoResultReason()), any(), any())
    }
  }

  @TestFactory
  fun testExecMultiple() = RuntimeTestGenerator.generate("testExecMultiple") {
    val func = spy(toLowerCase)
    addTaskDef(func)

    val input1 = "CAPITALIZED"
    val app1 = app(func, input1)
    val exec1 = spy(topDownExec())
    val output1 = exec1.requireInitial(app1)
    assertEquals("capitalized", output1)

    val input2 = "CAPITALIZED_EVEN_MORE"
    val app2 = app(func, input2)
    val exec2 = spy(topDownExec())
    val output2 = exec2.requireInitial(app2)
    assertEquals("capitalized_even_more", output2)

    assertNotEquals(output1, output2)

    inOrder(func, exec1, exec2) {
      verify(exec1, times(1)).requireInitial(eq(app1), any())
      verify(exec1, times(1)).exec(eq(app1), eq(NoResultReason()), any(), any())

      verify(exec2, times(1)).requireInitial(eq(app2), any())
      verify(exec2, times(1)).exec(eq(app2), eq(NoResultReason()), any(), any())
    }
  }

  @TestFactory
  fun testReuse() = RuntimeTestGenerator.generate("testReuse") {
    val func = spy(toLowerCase)
    addTaskDef(func)

    val input = "CAPITALIZED"
    val app = app(func, input)
    val exec1 = topDownExec()
    val output1 = exec1.requireInitial(app)
    assertEquals("capitalized", output1)

    val exec2 = spy(topDownExec())
    val output2 = exec2.requireInitial(app)
    assertEquals("capitalized", output2)

    assertEquals(output1, output2)

    // Result is reused if rebuild is never called.
    verify(exec2, never()).exec(eq(app), eq(NoResultReason()), any(), any())
  }

  @TestFactory
  fun testPathReq() = RuntimeTestGenerator.generate("testPathReq") {
    val readPath = spy(readPath)
    addTaskDef(readPath)

    val filePath = path("/file")
    write("HELLO WORLD!", filePath)

    // Build 'readPath', observe rebuild
    val exec1 = spy(topDownExec())
    val output1 = exec1.requireInitial(app(readPath, filePath))
    assertEquals("HELLO WORLD!", output1)
    verify(exec1, times(1)).exec(eq(app(readPath, filePath)), eq(NoResultReason()), any(), any())

    // No changes - exec 'readPath', observe no rebuild
    val exec2 = spy(topDownExec())
    val output2 = exec2.requireInitial(app(readPath, filePath))
    assertEquals("HELLO WORLD!", output2)
    verify(exec2, never()).exec(eq(app(readPath, filePath)), any(), any(), any())

    // Change required file in such a way that the output of 'readPath' changes (change file content)
    write("!DLROW OLLEH", filePath)

    // Run again - expect rebuild
    val exec3 = spy(topDownExec())
    val output3 = exec3.requireInitial(app(readPath, filePath))
    assertEquals("!DLROW OLLEH", output3)
    verify(exec3, times(1)).exec(eq(app(readPath, filePath)), check {
      val reason = it as? InconsistentFileReq
      assertNotNull(reason)
      assertEquals(filePath, reason!!.req.file)
    }, any(), any())
  }

  @TestFactory
  fun testPathGen() = RuntimeTestGenerator.generate("testPathGen") {
    val writePath = spy(writePath)
    addTaskDef(writePath)

    val filePath = path("/file")
    assertTrue(Files.notExists(filePath.javaPath))

    // Build 'writePath', observe rebuild and existence of file
    val exec1 = spy(topDownExec())
    exec1.requireInitial(app(writePath, Pair("HELLO WORLD!", filePath)))
    verify(exec1, times(1)).exec(eq(app(writePath, Pair("HELLO WORLD!", filePath))), eq(NoResultReason()), any(), any())

    assertTrue(Files.exists(filePath.javaPath))
    assertEquals("HELLO WORLD!", read(filePath))

    // No changes - exec 'writePath', observe no rebuild, no change to file
    val exec2 = spy(topDownExec())
    exec2.requireInitial(app(writePath, Pair("HELLO WORLD!", filePath)))
    verify(exec2, never()).exec(eq(app(writePath, Pair("HELLO WORLD!", filePath))), any(), any(), any())

    // Change generated file in such a way that 'writePath' is rebuilt (change file content)
    write("!DLROW OLLEH", filePath)

    // Build 'writePath', observe rebuild and change of file
    val exec3 = spy(topDownExec())
    exec3.requireInitial(app(writePath, Pair("HELLO WORLD!", filePath)))
    verify(exec3, times(1)).exec(eq(app(writePath, Pair("HELLO WORLD!", filePath))), check {
      val reason = it as? InconsistentFileGen
      assertNotNull(reason)
      assertEquals(filePath, reason!!.fileGen.file)
    }, any(), any())

    assertEquals("HELLO WORLD!", read(filePath))
  }

  @TestFactory
  fun testCallReq() = RuntimeTestGenerator.generate("testCallReq") {
    val toLowerCase = spy(toLowerCase)
    addTaskDef(toLowerCase)
    val readPath = spy(readPath)
    addTaskDef(readPath)
    val combine = spy(func<PPath, String>("combine", { "toLowerCase(read($it))" }) {
      val text = requireOutput(app(readPath, it))
      requireOutput(app(toLowerCase, text))
    })
    addTaskDef(combine)

    val filePath = path("/file")
    write("HELLO WORLD!", filePath)

    // Build 'combine', observe rebuild of all
    val exec1 = spy(topDownExec())
    val output1 = exec1.requireInitial(app(combine, filePath))
    assertEquals("hello world!", output1)
    inOrder(exec1) {
      verify(exec1, times(1)).exec(eq(app(combine, filePath)), eq(NoResultReason()), any(), any())
      verify(exec1, times(1)).exec(eq(app(readPath, filePath)), eq(NoResultReason()), any(), any())
      verify(exec1, times(1)).exec(eq(app(toLowerCase, "HELLO WORLD!")), eq(NoResultReason()), any(), any())
    }

    // No changes - exec 'combine', observe no rebuild
    val exec2 = spy(topDownExec())
    val output2 = exec2.requireInitial(app(combine, filePath))
    assertEquals("hello world!", output2)
    verify(exec2, never()).exec(eq(app(combine, filePath)), any(), any(), any())
    verify(exec2, never()).exec(eq(app(readPath, filePath)), any(), any(), any())
    verify(exec2, never()).exec(eq(app(toLowerCase, "HELLO WORLD!")), any(), any(), any())

    // Change required file in such a way that the output of 'readPath' changes (change file content)
    write("!DLROW OLLEH", filePath)

    // Build 'combine', observe rebuild of all in dependency order
    val exec3 = spy(topDownExec())
    val output3 = exec3.requireInitial(app(combine, filePath))
    assertEquals("!dlrow olleh", output3)
    inOrder(exec3) {
      verify(exec3, times(1)).requireInitial(eq(app(combine, filePath)), any())
      verify(exec3, times(1)).exec(eq(app(readPath, filePath)), check {
        val reason = it as? InconsistentFileReq
        assertNotNull(reason)
        assertEquals(filePath, reason!!.req.file)
      }, any(), any())
      verify(exec3, times(1)).exec(eq(app(combine, filePath)), check {
        val reason = it as? InconsistentTaskReq
        assertNotNull(reason)
        assertEquals(app(readPath, filePath), reason!!.req.callee)
      }, any(), any())
      verify(exec3, times(1)).exec(eq(app(toLowerCase, "!DLROW OLLEH")), eq(NoResultReason()), any(), any())
    }

    // Change required file in such a way that the output of 'readPath' does not change (change modification date)
    val lastModified = Files.getLastModifiedTime(filePath.javaPath)
    val newLastModified = FileTime.fromMillis(lastModified.toMillis() + 1)
    Files.setLastModifiedTime(filePath.javaPath, newLastModified)

    // Build 'combine', observe rebuild of 'readPath' only
    val exec4 = spy(topDownExec())
    val output4 = exec4.requireInitial(app(combine, filePath))
    assertEquals("!dlrow olleh", output4)
    inOrder(exec4) {
      verify(exec4, times(1)).requireInitial(eq(app(combine, filePath)), any())
      verify(exec4, times(1)).exec(eq(app(readPath, filePath)), check {
        val reason = it as? InconsistentFileReq
        assertNotNull(reason)
        assertEquals(filePath, reason!!.req.file)
      }, any(), any())
    }
    verify(exec4, never()).exec(eq(app(combine, filePath)), any(), any(), any())
    verify(exec4, never()).exec(eq(app(toLowerCase, "!DLROW OLLEH")), any(), any(), any())
  }

  @TestFactory
  fun testOverlappingGeneratedPath() = RuntimeTestGenerator.generate("testOverlappingGeneratedPath") {
    addTaskDef(writePath)

    val executor = topDownExecutor

    val filePath = path("/file")
    assertThrows(ValidationException::class.java) {
      val exec = executor.newSession()
      exec.requireInitial(app(writePath, Pair("HELLO WORLD 1!", filePath)))
      exec.requireInitial(app(writePath, Pair("HELLO WORLD 2!", filePath)))
    }

    // Overlapping generated file exception should also trigger between separate execs
    assertThrows(ValidationException::class.java) {
      val exec = executor.newSession()
      exec.requireInitial(app(writePath, Pair("HELLO WORLD 3!", filePath)))
    }
  }

  @TestFactory
  fun testGenerateRequiredHiddenDep() = RuntimeTestGenerator.generate("testGenerateRequiredHiddenDep") {
    addTaskDef(readPath)
    addTaskDef(writePath)

    val executor = topDownExecutor

    val filePath = path("/file")
    write("HELLO WORLD!", filePath)

    assertThrows(ValidationException::class.java) {
      val exec = executor.newSession()
      exec.requireInitial(app(readPath, filePath))
      exec.requireInitial(app(writePath, Pair("HELLO WORLD!", filePath)))
    }

    // Hidden dependency exception should also trigger between separate execs
    assertThrows(ValidationException::class.java) {
      val exec = executor.newSession()
      exec.requireInitial(app(writePath, Pair("HELLO WORLD!", filePath)))
    }
  }

  @TestFactory
  fun testRequireGeneratedHiddenDep() = RuntimeTestGenerator.generate("testRequireGeneratedHiddenDep") {
    addTaskDef(writePath)
    addTaskDef(readPath)
    val indirection = requireOutputFunc<Pair<String, PPath>, None>()
    addTaskDef(indirection)

    val executor = topDownExecutor

    val combineIncorrect = spy(func<Pair<String, PPath>, String>("combineIncorrect", { "combine$it" }) { (text, path) ->
      requireExec(app(indirection, app(writePath, Pair(text, path))))
      requireOutput(app(readPath, path))
    })
    addTaskDef(combineIncorrect)

    run {
      val filePath1 = path("/file1")
      assertThrows(ValidationException::class.java) {
        val exec = executor.newSession()
        exec.requireInitial(app(combineIncorrect, Pair("HELLO WORLD!", filePath1)))
      }
    }

    val combineStillIncorrect = spy(func<Pair<String, PPath>, String>("combineStillIncorrect", { "combine$it" }) { (text, path) ->
      requireExec(app(indirection, app(writePath, Pair(text, path))))
      requireExec(app(writePath, Pair(text, path)))
      requireOutput(app(readPath, path))
    })
    addTaskDef(combineStillIncorrect)

    run {
      val filePath2 = path("/file2")
      assertThrows(ValidationException::class.java) {
        val exec = executor.newSession()
        exec.requireInitial(app(combineStillIncorrect, Pair("HELLO WORLD!", filePath2)))
      }
    }
  }

  @TestFactory
  fun testCyclicDependency() = RuntimeTestGenerator.generate("testCyclicDependency") {
    val b1 = func<None, None>("b1", { "b1" }) { requireOutput(app("b1", None.instance)) }
    addTaskDef(b1)

    val bm = topDownExecutor

    assertThrows(ValidationException::class.java) {
      val exec = bm.newSession()
      exec.requireInitial(app(b1, None.instance))
    }
  }
}