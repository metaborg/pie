package mb.pie.runtime

import com.nhaarman.mockito_kotlin.*
import mb.pie.api.*
import mb.pie.api.exec.ExecReason
import mb.pie.api.stamp.FileStamper
import mb.pie.api.stamp.OutputStamper
import mb.pie.runtime.exec.NoResultReason
import mb.pie.runtime.util.TestGenerator
import mb.util.async.Cancelled
import mb.util.async.NullCancelled
import mb.vfs.path.PPath
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestFactory
import org.mockito.Mockito

inline fun <reified T : Any> safeAny(default: T) = Mockito.any(T::class.java) ?: default


class NoExecReason : ExecReason {
  override fun toString() = ""
}

fun anyER() = safeAny<ExecReason>(NoExecReason())


class NoExecContext : ExecContext {
  override fun <I : In, O : Out> requireOutput(task: Task<I, O>, stamper: OutputStamper?): O {
    @Suppress("UNCHECKED_CAST")
    return null as O
  }

  override fun requireExec(task: UTask) {}
  override fun require(file: PPath, stamper: FileStamper?) {}
  override fun generate(file: PPath, stamper: FileStamper?) {}
  override val logger: Logger = null!!
}

fun anyEC() = safeAny<ExecContext>(NoExecContext())


fun anyC() = safeAny<Cancelled>(NullCancelled())


internal class BottomUpTests {
  @TestFactory
  fun testTopDownExec() = TestGenerator.generate("testTopDownExec") {
    val input = "CAPITALIZED"
    val func = spy(toLowerCase)
    addTaskDef(func)
    val app = app(func, input)

    val exec = spy(bottomUpExec())
    val output = exec.require(app)
    Assertions.assertEquals("capitalized", output)

    inOrder(exec, func) {
      verify(exec, times(1)).require(eq(app), any())
      verify(exec, times(1)).exec(eq(app), eq(NoResultReason()), any(), any())
    }
  }

  @TestFactory
  fun testTopDownExecMultiple() = TestGenerator.generate("testTopDownExecMultiple") {
    val func = spy(toLowerCase)
    addTaskDef(func)

    val input1 = "CAPITALIZED"
    val app1 = app(func, input1)
    val exec1 = spy(bottomUpExec())
    val output1 = exec1.require(app1)
    Assertions.assertEquals("capitalized", output1)

    val input2 = "CAPITALIZED_EVEN_MORE"
    val app2 = app(func, input2)
    val exec2 = spy(bottomUpExec())
    val output2 = exec2.require(app2)
    Assertions.assertEquals("capitalized_even_more", output2)

    Assertions.assertNotEquals(output1, output2)

    inOrder(func, exec1, exec2) {
      verify(exec1, times(1)).require(eq(app1), any())
      verify(exec1, times(1)).exec(eq(app1), eq(NoResultReason()), any(), any())

      verify(exec2, times(1)).require(eq(app2), any())
      verify(exec2, times(1)).exec(eq(app2), eq(NoResultReason()), any(), any())
    }
  }

  @TestFactory
  fun testTopDownReuse() = TestGenerator.generate("testTopDownReuse") {
    val func = spy(toLowerCase)
    addTaskDef(func)

    val input = "CAPITALIZED"
    val app = app(func, input)
    val exec1 = bottomUpExec()
    val output1 = exec1.require(app)
    Assertions.assertEquals("capitalized", output1)

    val exec2 = spy(bottomUpExec())
    val output2 = exec2.require(app)
    Assertions.assertEquals("capitalized", output2)

    Assertions.assertEquals(output1, output2)

    // Result is reused if rebuild is never called.
    verify(exec2, never()).exec(eq(app), eq(NoResultReason()), any(), any())
  }

  @TestFactory
  fun testPathChange() = TestGenerator.generate("testPathChange") {
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
    val exec1 = spy(bottomUpExec())
    val output1 = exec1.require(app(combine, filePath))
    Assertions.assertEquals("hello world!", output1)
    inOrder(exec1) {
      verify(exec1).exec(eq(app(combine, filePath)), eq(NoResultReason()), anyC(), any())
      verify(exec1).exec(eq(app(readPath, filePath)), eq(NoResultReason()), anyC(), any())
      verify(exec1).exec(eq(app(toLowerCase, "HELLO WORLD!")), eq(NoResultReason()), anyC(), any())
    }

    // Change required file in such a way that the output of 'readPath' changes (change file content)
    val newStr = "!DLROW OLLEH"
    write(newStr, filePath)

    // Notify of file change, observe bottom-up execution to [combine], and then top-down execution of [toLowerCase].
    val exec2 = spy(bottomUpExec())
    exec2.scheduleAffectedByFiles(setOf(filePath))
    exec2.execScheduled(NullCancelled())
    inOrder(exec2) {
      verify(exec2).exec(eq(app(readPath, filePath)), anyER(), anyC(), any())
      verify(exec2).exec(eq(app(combine, filePath)), anyER(), anyC(), any())
      verify(exec2).require(eq(app(toLowerCase, newStr)), anyC())
      verify(exec2).exec(eq(app(toLowerCase, newStr)), anyER(), anyC(), any())
    }

    // Notify of file change, but file hasn't actually changed, observe no execution.
    val exec3 = spy(bottomUpExec())
    exec3.scheduleAffectedByFiles(setOf(filePath))
    exec3.execScheduled(NullCancelled())
    verify(exec3, never()).exec(eq(app(readPath, filePath)), anyER(), anyC(), any())
    verify(exec3, never()).exec(eq(app(combine, filePath)), anyER(), anyC(), any())
    verify(exec3, never()).exec(eq(app(toLowerCase, newStr)), anyER(), anyC(), any())

    // Change required file in such a way that the file changes, but the output of [readPath] does not.
    write(newStr, filePath)

    // Notify of file change, observe bottom-up execution of [readPath], but stop there because [combine] is still consistent
    val exec4 = spy(bottomUpExec())
    exec4.scheduleAffectedByFiles(setOf(filePath))
    exec4.execScheduled(NullCancelled())
    inOrder(exec4) {
      verify(exec4).exec(eq(app(readPath, filePath)), anyER(), anyC(), any())
    }
    verify(exec4, never()).exec(eq(app(combine, filePath)), anyER(), anyC(), any())
    verify(exec4, never()).require(eq(app(toLowerCase, newStr)), anyC())
    verify(exec4, never()).exec(eq(app(toLowerCase, newStr)), anyER(), anyC(), any())
  }
}