package mb.pie.runtime.test

import com.nhaarman.mockitokotlin2.*
import mb.pie.api.exec.NullCancelled
import mb.pie.api.test.*
import mb.pie.runtime.exec.NoData
import mb.resource.fs.FSResource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestFactory

internal class BottomUpTests {
  @TestFactory
  fun testFileChange() = RuntimeTestGenerator.generate("testFileChange") {
    val lowerDef = spy(toLowerCase)
    addTaskDef(lowerDef)
    val readDef = spy(readResource)
    addTaskDef(readDef)
    val combDef = spy(taskDef<FSResource, String>("combine", { input, _ -> "toLowerCase(read($input))" }) {
      val text = require(task(readDef, it))
      require(task(lowerDef, text))
    })
    addTaskDef(combDef)

    val str = "HELLO WORLD!"
    val file = resource("/file")
    write(str, file)

    val combTask = task(combDef, file)
    val combKey = combTask.key()
    var combOutput: String? = null
    var combObserved = 0
    pie.setCallback(combTask) { s -> combOutput = s; ++combObserved }

    val readTask = task(readDef, file)
    val readKey = readTask.key()
    var readOutput: String? = null
    var readObserved = 0
    pie.setCallback(readTask) { s -> readOutput = s; ++readObserved }

    val lowerTask = task(lowerDef, str)
    val lowerKey = lowerTask.key()
    var lowerOutput: String? = null
    var lowerObserved = 0
    pie.setCallback(lowerTask) { s -> lowerOutput = s; ++lowerObserved }

    // Build [combineTask] in top-down fashion, observe rebuild of all.
    val session1 = spy(newSession().topDownSession)
    val output1 = session1.requireInitial(combTask, true, NullCancelled())
    Assertions.assertEquals("hello world!", output1)
    Assertions.assertEquals("hello world!", combOutput)
    Assertions.assertEquals(1, combObserved)
    Assertions.assertEquals("HELLO WORLD!", readOutput)
    Assertions.assertEquals(1, readObserved)
    Assertions.assertEquals("hello world!", lowerOutput)
    Assertions.assertEquals(1, lowerObserved)
    inOrder(session1) {
      verify(session1).exec(eq(combKey), eq(combTask), eq(NoData()), eq(true), anyC())
      verify(session1).exec(eq(readKey), eq(readTask), eq(NoData()), eq(true), anyC())
      verify(session1).exec(eq(lowerKey), eq(lowerTask), eq(NoData()), eq(true), anyC())
    }

    // Change required file in such a way that the output of [readTask] changes (change file content).
    val newStr = "!DLROW OLLEH"
    write(newStr, file)

    val lowerRevTask = task(lowerDef, newStr)
    val lowerRevKey = lowerRevTask.key()
    var lowerRevOutput: String? = null
    var lowerRevObserved = 0
    pie.setCallback(lowerRevTask) { s -> lowerRevOutput = s; ++lowerRevObserved }

    // Notify of file change, observe bottom-up execution of directly affected [readTask], which then affects
    // [combTask], which in turn requires [lowerRevTask].
    val session2 = spy(newSession().bottomUpSession)
    session2.requireInitial(setOf(file.key), NullCancelled())
    // [combTask]'s key has not changed, since it is based on a file name that did not change.
    Assertions.assertEquals("!dlrow olleh", combOutput)
    Assertions.assertEquals(2, combObserved)
    // [readTask]'s key has not changed, since it is based on a file name that did not change.
    Assertions.assertEquals("!DLROW OLLEH", readOutput)
    Assertions.assertEquals(2, readObserved)
    // [lowerTask]'s key changed, so the previous task was not required, and thus not observed (asserts are same as last session).
    Assertions.assertEquals("hello world!", lowerOutput)
    Assertions.assertEquals(1, lowerObserved)
    // [lowerRevTask] has been required, and has thus been observed once.
    Assertions.assertEquals("!dlrow olleh", lowerRevOutput)
    Assertions.assertEquals(1, lowerRevObserved)
    inOrder(session2) {
      verify(session2).exec(eq(readKey), eq(readTask), anyER(), anyC())
      verify(session2).exec(eq(combKey), eq(combTask), anyER(), anyC())
      verify(session2).require(eq(lowerRevKey), eq(lowerRevTask), eq(true), anyC())
      verify(session2).exec(eq(lowerRevKey), eq(lowerRevTask), anyER(), anyC())
    }

    // Notify of file change, but file hasn't actually changed, observe no execution.
    val session3 = spy(newSession().bottomUpSession)
    session3.requireInitial(setOf(file.key), NullCancelled())
    // Since no task has been affected by the file change, no observers are observed (all asserts are same as last session).
    Assertions.assertEquals("!dlrow olleh", combOutput)
    Assertions.assertEquals(2, combObserved)
    Assertions.assertEquals("!DLROW OLLEH", readOutput)
    Assertions.assertEquals(2, readObserved)
    Assertions.assertEquals("hello world!", lowerOutput)
    Assertions.assertEquals(1, lowerObserved)
    Assertions.assertEquals("!dlrow olleh", lowerRevOutput)
    Assertions.assertEquals(1, lowerRevObserved)
    verify(session3, never()).exec(eq(readKey), eq(readTask), anyER(), anyC())
    verify(session3, never()).exec(eq(combKey), eq(combTask), anyER(), anyC())
    verify(session3, never()).exec(eq(lowerRevKey), eq(lowerRevTask), anyER(), anyC())

    // Change required file in such a way that the file changes (modified date), but the output of [readTask] does not.
    write(newStr, file)

    // Notify of file change, observe bottom-up execution of [readTask], but stop there because [combineTask] is still consistent.
    val exec4 = spy(newSession().bottomUpSession)
    exec4.requireInitial(setOf(file.key), NullCancelled())
    Assertions.assertEquals("!dlrow olleh", combOutput)
    Assertions.assertEquals(2, combObserved)
    Assertions.assertEquals("!DLROW OLLEH", readOutput)
    Assertions.assertEquals(3, readObserved)
    Assertions.assertEquals("hello world!", lowerOutput)
    Assertions.assertEquals(1, lowerObserved)
    Assertions.assertEquals("!dlrow olleh", lowerRevOutput)
    Assertions.assertEquals(1, lowerRevObserved)
    inOrder(exec4) {
      verify(exec4).exec(eq(readKey), eq(readTask), anyER(), anyC())
    }
    verify(exec4, never()).exec(eq(combKey), eq(combTask), anyER(), anyC())
    verify(exec4, never()).require(eq(lowerRevKey), eq(lowerRevTask), eq(true), anyC())
    verify(exec4, never()).exec(eq(lowerRevKey), eq(lowerRevTask), anyER(), anyC())
  }
}