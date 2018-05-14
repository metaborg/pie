package mb.pie.runtime.core.test

import com.nhaarman.mockito_kotlin.*
import kotlinx.coroutines.experimental.*
import mb.pie.runtime.core.*
import mb.pie.runtime.core.impl.exec.TopDownExecImpl
import mb.pie.runtime.core.impl.layer.ValidationException
import mb.pie.runtime.core.impl.share.CoroutineShare
import mb.pie.runtime.core.test.util.TestGenerator
import mb.vfs.path.PPath
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestFactory
import java.nio.file.Files
import java.nio.file.attribute.FileTime
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.javaMethod

internal class TopDownTests {
  @TestFactory
  fun testExec() = TestGenerator.generate("testExec") {
    val input = "CAPITALIZED"
    val func = spy(toLowerCase)
    registerFunc(func)
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
  fun testExecMultiple() = TestGenerator.generate("testExecMultiple") {
    val func = spy(toLowerCase)
    registerFunc(func)

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
  fun testReuse() = TestGenerator.generate("testReuse") {
    val func = spy(toLowerCase)
    registerFunc(func)

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
  fun testPathReq() = TestGenerator.generate("testPathReq") {
    val readPath = spy(readPath)
    registerFunc(readPath)

    val filePath = path(fs, "/file")
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
  fun testPathGen() = TestGenerator.generate("testPathGen") {
    val writePath = spy(writePath)
    registerFunc(writePath)

    val filePath = path(fs, "/file")
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
  fun testCallReq() = TestGenerator.generate("testCallReq") {
    val toLowerCase = spy(toLowerCase)
    registerFunc(toLowerCase)
    val readPath = spy(readPath)
    registerFunc(readPath)
    val combine = spy(func<PPath, String>("combine", { "toLowerCase(read($it))" }) {
      val text = requireOutput(app(readPath, it))
      requireOutput(app(toLowerCase, text))
    })
    registerFunc(combine)

    val filePath = path(fs, "/file")
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
  fun testOverlappingGeneratedPath() = TestGenerator.generate("testOverlappingGeneratedPath") {
    registerFunc(writePath)

    val executor = topDownExecutor()

    val filePath = path(fs, "/file")
    assertThrows(ValidationException::class.java) {
      val exec = executor.exec()
      exec.requireInitial(app(writePath, Pair("HELLO WORLD 1!", filePath)))
      exec.requireInitial(app(writePath, Pair("HELLO WORLD 2!", filePath)))
    }

    // Overlapping generated file exception should also trigger between separate execs
    assertThrows(ValidationException::class.java) {
      val exec = executor.exec()
      exec.requireInitial(app(writePath, Pair("HELLO WORLD 3!", filePath)))
    }
  }

  @TestFactory
  fun testGenerateRequiredHiddenDep() = TestGenerator.generate("testGenerateRequiredHiddenDep") {
    registerFunc(readPath)
    registerFunc(writePath)

    val executor = topDownExecutor()

    val filePath = path(fs, "/file")
    write("HELLO WORLD!", filePath)

    assertThrows(ValidationException::class.java) {
      val exec = executor.exec()
      exec.requireInitial(app(readPath, filePath))
      exec.requireInitial(app(writePath, Pair("HELLO WORLD!", filePath)))
    }

    // Hidden dependency exception should also trigger between separate execs
    assertThrows(ValidationException::class.java) {
      val exec = executor.exec()
      exec.requireInitial(app(writePath, Pair("HELLO WORLD!", filePath)))
    }
  }

  @TestFactory
  fun testRequireGeneratedHiddenDep() = TestGenerator.generate("testRequireGeneratedHiddenDep") {
    registerFunc(writePath)
    registerFunc(readPath)
    val indirection = requireOutputFunc<Pair<String, PPath>, None>()
    registerFunc(indirection)

    val executor = topDownExecutor()

    val combineIncorrect = spy(func<Pair<String, PPath>, String>("combineIncorrect", { "combine$it" }) { (text, path) ->
      requireExec(app(indirection, app(writePath, Pair(text, path))))
      requireOutput(app(readPath, path))
    })
    registerFunc(combineIncorrect)

    run {
      val filePath1 = path(fs, "/file1")
      assertThrows(ValidationException::class.java) {
        val exec = executor.exec()
        exec.requireInitial(app(combineIncorrect, Pair("HELLO WORLD!", filePath1)))
      }
    }

    val combineStillIncorrect = spy(func<Pair<String, PPath>, String>("combineStillIncorrect", { "combine$it" }) { (text, path) ->
      requireExec(app(indirection, app(writePath, Pair(text, path))))
      requireExec(app(writePath, Pair(text, path)))
      requireOutput(app(readPath, path))
    })
    registerFunc(combineStillIncorrect)

    run {
      val filePath2 = path(fs, "/file2")
      assertThrows(ValidationException::class.java) {
        val exec = executor.exec()
        exec.requireInitial(app(combineStillIncorrect, Pair("HELLO WORLD!", filePath2)))
      }
    }
  }

  @TestFactory
  fun testCyclicDependency() = TestGenerator.generate("testCyclicDependency") {
    val b1 = func<None, None>("b1", { "b1" }) { requireOutput(app("b1", None.instance)) }
    registerFunc(b1)

    val bm = topDownExecutor()

    assertThrows(ValidationException::class.java) {
      val exec = bm.exec()
      exec.requireInitial(app(b1, None.instance))
    }
  }

  @TestFactory
  fun testThreadSafety() = TestGenerator.generate("testThreadSafety") {
    registerFunc(toLowerCase)

    runBlocking {
      List(100) { index ->
        launch(coroutineContext + CommonPool) {
          val exec = topDownExec()
          val app = app(toLowerCase, "HELLO WORLD $index!")
          exec.requireInitial(app)
        }
      }.forEach { it.join() }
    }
  }

  @TestFactory
  fun testConcurrentReuse() = TestGenerator.generate("testConcurrentReuse",
    dShareGens = arrayOf({ CoroutineShare() }) /* Testing sharing, so only use shares that correctly share */) {
    registerFunc(toLowerCase)

    val spies = ConcurrentLinkedQueue<TopDownExecImpl>()
    runBlocking {
      List(100) {
        launch(coroutineContext + CommonPool) {
          val exec = spy(topDownExec())
          spies.add(exec)
          val app = app(toLowerCase, "HELLO WORLD!")
          exec.requireInitial(app)
        }
      }.forEach { it.join() }
    }

    // Test that function 'execInternal' has only been called once, even between all threads
    var invocations = 0
    val javaFuncName = TopDownExecImpl::class.memberFunctions.first { it.name == "execInternal" }.javaMethod!!.name
    spies.forEach { spy ->
      mockingDetails(spy).invocations
        .filter { it.method.name == javaFuncName }
        .forEach { ++invocations }
    }
    assertEquals(1, invocations)
  }
}