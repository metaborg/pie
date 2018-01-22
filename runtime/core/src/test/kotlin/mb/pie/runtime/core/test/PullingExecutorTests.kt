package mb.pie.runtime.core.test

import com.nhaarman.mockito_kotlin.*
import kotlinx.coroutines.experimental.*
import mb.pie.runtime.core.*
import mb.pie.runtime.core.impl.PullingExecImpl
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

internal class PullingExecutorTests {
  @TestFactory
  fun testExec() = TestGenerator.generate("testExec") {
    val input = "CAPITALIZED"
    val func = spy(toLowerCase)
    registerFunc(func)
    val app = app(func, input)

    val exec = spy(pullingExec())
    val info = exec.require(app)
    assertEquals(NoResultReason(), info.reason)
    val result = info.result
    assertEquals(func.id, result.id)
    assertEquals(input, result.input)
    assertEquals("capitalized", result.output)
    assertEquals(0, result.reqs.size)
    assertEquals(0, result.gens.size)

    inOrder(exec, func) {
      verify(exec, times(1)).require(eq(app), any())
      verify(exec, times(1)).exec(eq(app), eq(NoResultReason()), any(), any())
      verify(func, times(1)).exec(eq(input), anyOrNull())
    }

    verify(func, atLeastOnce()).desc(input)
  }

  @TestFactory
  fun testExecMultiple() = TestGenerator.generate("testExecMultiple") {
    val func = spy(toLowerCase)
    registerFunc(func)

    val input1 = "CAPITALIZED"
    val app1 = app(func, input1)
    val exec1 = spy(pullingExec())
    val info1 = exec1.require(app1)
    assertEquals(NoResultReason(), info1.reason)
    val result1 = info1.result
    assertEquals(func.id, result1.id)
    assertEquals(input1, result1.input)
    assertEquals("capitalized", result1.output)

    val input2 = "CAPITALIZED_EVEN_MORE"
    val app2 = app(func, input2)
    val exec2 = spy(pullingExec())
    val info2 = exec2.require(app2)
    assertEquals(NoResultReason(), info2.reason)
    val result2 = info2.result
    assertEquals(func.id, result2.id)
    assertEquals(input2, result2.input)
    assertEquals("capitalized_even_more", result2.output)

    assertNotEquals(result1, result2)

    inOrder(func, exec1, exec2) {
      verify(exec1, times(1)).require(eq(app1), any())
      verify(exec1, times(1)).exec(eq(app1), eq(NoResultReason()), any(), any())
      verify(func, times(1)).exec(eq(input1), anyOrNull())

      verify(exec2, times(1)).require(eq(app2), any())
      verify(exec2, times(1)).exec(eq(app2), eq(NoResultReason()), any(), any())
      verify(func, times(1)).exec(eq(input2), anyOrNull())
    }
  }

  @TestFactory
  fun testReuse() = TestGenerator.generate("testReuse") {
    val func = spy(toLowerCase)
    registerFunc(func)

    val input = "CAPITALIZED"
    val app = app(func, input)
    val exec1 = pullingExec()
    val info1 = exec1.require(app)
    assertEquals(NoResultReason(), info1.reason)
    val result1 = info1.result

    val exec2 = spy(pullingExec())
    val info2 = exec2.require(app)
    assertNull(info2.reason)
    val result2 = info2.result

    assertEquals(result1, result2)

    // Result is reused if rebuild is never called
    verify(exec2, never()).exec(eq(app), eq(NoResultReason()), any(), any())

    verify(func, atMost(1)).exec(eq(input), anyOrNull())
    verify(func, atLeastOnce()).desc(input)
  }

  @TestFactory
  fun testPathReq() = TestGenerator.generate("testPathReq") {
    val readPath = spy(readPath)
    registerFunc(readPath)

    val filePath = path(fs, "/file")
    write("HELLO WORLD!", filePath)

    // Build 'readPath', observe rebuild
    val exec1 = spy(pullingExec())
    val result1 = exec1.require(app(readPath, filePath)).result
    assertEquals("HELLO WORLD!", result1.output)
    verify(exec1, times(1)).exec(eq(app(readPath, filePath)), eq(NoResultReason()), any(), any())

    // No changes - exec 'readPath', observe no rebuild
    val exec2 = spy(pullingExec())
    val result2 = exec2.require(app(readPath, filePath)).result
    assertEquals("HELLO WORLD!", result2.output)
    verify(exec2, never()).exec(eq(app(readPath, filePath)), any(), any(), any())

    // Change required file in such a way that the output of 'readPath' changes (change file content)
    write("!DLROW OLLEH", filePath)

    // Run again - expect rebuild
    val exec3 = spy(pullingExec())
    val result3 = exec3.require(app(readPath, filePath)).result
    assertEquals("!DLROW OLLEH", result3.output)
    verify(exec3, times(1)).exec(eq(app(readPath, filePath)), check {
      val reason = it as? InconsistentPathReq
      assertNotNull(reason)
      assertEquals(filePath, reason!!.req.path)
    }, any(), any())
  }

  @TestFactory
  fun testPathGen() = TestGenerator.generate("testPathGen") {
    val writePath = spy(writePath)
    registerFunc(writePath)

    val filePath = path(fs, "/file")
    assertTrue(Files.notExists(filePath.javaPath))

    // Build 'writePath', observe rebuild and existence of file
    val exec1 = spy(pullingExec())
    exec1.require(app(writePath, Pair("HELLO WORLD!", filePath)))
    verify(exec1, times(1)).exec(eq(app(writePath, Pair("HELLO WORLD!", filePath))), eq(NoResultReason()), any(), any())

    assertTrue(Files.exists(filePath.javaPath))
    assertEquals("HELLO WORLD!", read(filePath))

    // No changes - exec 'writePath', observe no rebuild, no change to file
    val exec2 = spy(pullingExec())
    exec2.require(app(writePath, Pair("HELLO WORLD!", filePath)))
    verify(exec2, never()).exec(eq(app(writePath, Pair("HELLO WORLD!", filePath))), any(), any(), any())

    // Change generated file in such a way that 'writePath' is rebuilt (change file content)
    write("!DLROW OLLEH", filePath)

    // Build 'writePath', observe rebuild and change of file
    val exec3 = spy(pullingExec())
    exec3.require(app(writePath, Pair("HELLO WORLD!", filePath)))
    verify(exec3, times(1)).exec(eq(app(writePath, Pair("HELLO WORLD!", filePath))), check {
      val reason = it as? InconsistentGenPath
      assertNotNull(reason)
      assertEquals(filePath, reason!!.gen.path)
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
    val exec1 = spy(pullingExec())
    val result1 = exec1.require(app(combine, filePath)).result
    assertEquals("hello world!", result1.output)
    inOrder(exec1) {
      verify(exec1, times(1)).exec(eq(app(combine, filePath)), eq(NoResultReason()), any(), any())
      verify(exec1, times(1)).exec(eq(app(readPath, filePath)), eq(NoResultReason()), any(), any())
      verify(exec1, times(1)).exec(eq(app(toLowerCase, "HELLO WORLD!")), eq(NoResultReason()), any(), any())
    }

    // No changes - exec 'combine', observe no rebuild
    val exec2 = spy(pullingExec())
    val result2 = exec2.require(app(combine, filePath)).result
    assertEquals("hello world!", result2.output)
    verify(exec2, never()).exec(eq(app(combine, filePath)), any(), any(), any())
    verify(exec2, never()).exec(eq(app(readPath, filePath)), any(), any(), any())
    verify(exec2, never()).exec(eq(app(toLowerCase, "HELLO WORLD!")), any(), any(), any())

    // Change required file in such a way that the output of 'readPath' changes (change file content)
    write("!DLROW OLLEH", filePath)

    // Build 'combine', observe rebuild of all in dependency order
    val exec3 = spy(pullingExec())
    val result3 = exec3.require(app(combine, filePath)).result
    assertEquals("!dlrow olleh", result3.output)
    inOrder(exec3) {
      verify(exec3, times(1)).require(eq(app(combine, filePath)), any())
      verify(exec3, times(1)).exec(eq(app(readPath, filePath)), check {
        val reason = it as? InconsistentPathReq
        assertNotNull(reason)
        assertEquals(filePath, reason!!.req.path)
      }, any(), any())
      verify(exec3, times(1)).exec(eq(app(combine, filePath)), check {
        val reason = it as? InconsistentExecReq
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
    val exec4 = spy(pullingExec())
    val result4 = exec4.require(app(combine, filePath)).result
    assertEquals("!dlrow olleh", result4.output)
    inOrder(exec4) {
      verify(exec4, times(1)).require(eq(app(combine, filePath)), any())
      verify(exec4, times(1)).exec(eq(app(readPath, filePath)), check {
        val reason = it as? InconsistentPathReq
        assertNotNull(reason)
        assertEquals(filePath, reason!!.req.path)
      }, any(), any())
    }
    verify(exec4, never()).exec(eq(app(combine, filePath)), any(), any(), any())
    verify(exec4, never()).exec(eq(app(toLowerCase, "!DLROW OLLEH")), any(), any(), any())
  }

  @TestFactory
  fun testOverlappingGeneratedPath() = TestGenerator.generate("testOverlappingGeneratedPath") {
    registerFunc(writePath)

    val executor = pullingExecutor()

    val filePath = path(fs, "/file")
    assertThrows(ValidationException::class.java) {
      val exec = executor.exec()
      exec.requireOutput(app(writePath, Pair("HELLO WORLD 1!", filePath)))
      exec.requireOutput(app(writePath, Pair("HELLO WORLD 2!", filePath)))
    }

    // Overlapping generated path exception should also trigger between separate execs
    assertThrows(ValidationException::class.java) {
      val exec = executor.exec()
      exec.requireOutput(app(writePath, Pair("HELLO WORLD 3!", filePath)))
    }
  }

  @TestFactory
  fun testGenerateRequiredHiddenDep() = TestGenerator.generate("testGenerateRequiredHiddenDep") {
    registerFunc(readPath)
    registerFunc(writePath)

    val executor = pullingExecutor()

    val filePath = path(fs, "/file")
    write("HELLO WORLD!", filePath)

    assertThrows(ValidationException::class.java) {
      val exec = executor.exec()
      exec.requireOutput(app(readPath, filePath))
      exec.requireOutput(app(writePath, Pair("HELLO WORLD!", filePath)))
    }

    // Hidden dependency exception should also trigger between separate execs
    assertThrows(ValidationException::class.java) {
      val exec = executor.exec()
      exec.requireOutput(app(writePath, Pair("HELLO WORLD!", filePath)))
    }
  }

  @TestFactory
  fun testRequireGeneratedHiddenDep() = TestGenerator.generate("testRequireGeneratedHiddenDep") {
    registerFunc(writePath)
    registerFunc(readPath)
    val indirection = requireOutputFunc<Pair<String, PPath>, None>()
    registerFunc(indirection)

    val executor = pullingExecutor()

    val combineIncorrect = spy(func<Pair<String, PPath>, String>("combineIncorrect", { "combine$it" }) { (text, path) ->
      requireExec(app(indirection, app(writePath, Pair(text, path))))
      requireOutput(app(readPath, path))
    })
    registerFunc(combineIncorrect)

    run {
      val filePath1 = path(fs, "/file1")
      // CHANGED: this dependency is now inferred automatically, so no exception is thrown
      // assertThrows(HiddenDependencyException::class.java) {
      val exec = executor.exec()
      exec.requireOutput(app(combineIncorrect, Pair("HELLO WORLD!", filePath1)))
      // }
    }

    val combineStillIncorrect = spy(func<Pair<String, PPath>, String>("combineStillIncorrect", { "combine$it" }) { (text, path) ->
      requireExec(app(indirection, app(writePath, Pair(text, path))))
      requireExec(app(writePath, Pair(text, path)))
      requireOutput(app(readPath, path))
    })
    registerFunc(combineStillIncorrect)

    run {
      val filePath2 = path(fs, "/file2")
      // CHANGED: this dependency is now inferred automatically, so no exception is thrown
      // assertThrows(HiddenDependencyException::class.java) {
      val exec = executor.exec()
      exec.requireOutput(app(combineStillIncorrect, Pair("HELLO WORLD!", filePath2)))
      // }
    }
  }

  @TestFactory
  fun testCyclicDependency() = TestGenerator.generate("testCyclicDependency") {
    val b1 = func<None, None>("b1", { "b1" }) { requireOutput(app("b1", None.instance)) }
    registerFunc(b1)

    val bm = pullingExecutor()

    assertThrows(ValidationException::class.java) {
      val exec = bm.exec()
      exec.requireOutput(app(b1, None.instance))
    }
  }

  @TestFactory
  fun testThreadSafety() = TestGenerator.generate("testThreadSafety") {
    registerFunc(toLowerCase)

    runBlocking {
      List(100) { index ->
        launch(coroutineContext + CommonPool) {
          val exec = pullingExec()
          val app = app(toLowerCase, "HELLO WORLD $index!")
          exec.require(app)
        }
      }.forEach { it.join() }
    }
  }

  @TestFactory
  fun testConcurrentReuse() = TestGenerator.generate("testConcurrentReuse",
    dShareGens = arrayOf({ CoroutineShare() }) /* Testing sharing, so only use shares that correctly share */) {
    registerFunc(toLowerCase)

    val spies = ConcurrentLinkedQueue<PullingExecImpl>()
    runBlocking {
      List(100) {
        launch(coroutineContext + CommonPool) {
          val exec = spy(pullingExec())
          spies.add(exec)
          val app = app(toLowerCase, "HELLO WORLD!")
          exec.require(app)
        }
      }.forEach { it.join() }
    }

    // Test that function 'execInternal' has only been called once, even between all threads
    var invocations = 0
    val javaFuncName = PullingExecImpl::class.memberFunctions.first { it.name == "execInternal" }.javaMethod!!.name
    spies.forEach { spy ->
      mockingDetails(spy).invocations
        .filter { it.method.name == javaFuncName }
        .forEach { ++invocations }
    }
    assertEquals(1, invocations)
  }
}