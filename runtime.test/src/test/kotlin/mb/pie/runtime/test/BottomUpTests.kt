package mb.pie.runtime.test

import com.nhaarman.mockitokotlin2.*
import mb.pie.api.exec.NullCancelled
import mb.pie.api.test.anyC
import mb.pie.api.test.anyER
import mb.pie.api.test.readResource
import mb.pie.api.test.toLowerCase
import mb.pie.runtime.exec.NoData
import mb.resource.fs.FSResource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestFactory

class BottomUpTests {
  private val builder = DefaultRuntimeTestBuilder()


  @TestFactory
  fun testUpdateAffectedBy() = builder.build("testUpdateAffectedBy") {
    val lowerDef = spy(toLowerCase)
    addTaskDef(lowerDef)
    val readDef = spy(readResource)
    addTaskDef(readDef)
    val combDef = spy(taskDef<FSResource, String>("combine", { input, _ -> "toLowerCase(read($input))" }) {
      val text = require(readDef.createTask(it))
      require(lowerDef.createTask(text))
    })
    addTaskDef(combDef)

    val str = "HELLO WORLD!"
    val file = resource("/file")
    write(str, file)

    val combTask = combDef.createTask(file)
    val combKey = combTask.key()
    var combOutput: String? = null
    var combObserved = 0
    pie.setCallback(combTask) { s -> combOutput = s; ++combObserved }

    val readTask = readDef.createTask(file)
    val readKey = readTask.key()
    var readOutput: String? = null
    var readObserved = 0
    pie.setCallback(readTask) { s -> readOutput = s; ++readObserved }

    val lowerTask = lowerDef.createTask(str)
    val lowerKey = lowerTask.key()
    var lowerOutput: String? = null
    var lowerObserved = 0
    pie.setCallback(lowerTask) { s -> lowerOutput = s; ++lowerObserved }

    // Build [combineTask] in top-down fashion, observe rebuild of all.
    newSession().use { session ->
      val output = session.requireAndObserve(combTask)
      Assertions.assertEquals("hello world!", output)
      Assertions.assertEquals("hello world!", combOutput)
      Assertions.assertEquals(1, combObserved)
      Assertions.assertEquals("HELLO WORLD!", readOutput)
      Assertions.assertEquals(1, readObserved)
      Assertions.assertEquals("hello world!", lowerOutput)
      Assertions.assertEquals(1, lowerObserved)
      val topDownSession = session.topDownSession
      inOrder(topDownSession) {
        verify(topDownSession).exec(eq(combKey), eq(combTask), eq(NoData()), any(), anyC())
        verify(topDownSession).exec(eq(readKey), eq(readTask), eq(NoData()), any(), anyC())
        verify(topDownSession).exec(eq(lowerKey), eq(lowerTask), eq(NoData()), any(), anyC())
      }
    }

    // Change required file in such a way that the output of [readTask] changes (change file content).
    val newStr = "!DLROW OLLEH"
    write(newStr, file)

    val lowerRevTask = lowerDef.createTask(newStr)
    val lowerRevKey = lowerRevTask.key()
    var lowerRevOutput: String? = null
    var lowerRevObserved = 0
    pie.setCallback(lowerRevTask) { s -> lowerRevOutput = s; ++lowerRevObserved }

    // Notify of file change, observe bottom-up execution of directly affected [readTask], which then affects
    // [combTask], which in turn requires [lowerRevTask].
    newSession().use { session ->
      session.updateAffectedBy(setOf(file.key))
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
      val bottomUpSession = session.bottomUpSession
      inOrder(bottomUpSession) {
        verify(bottomUpSession).exec(eq(readKey), eq(readTask), anyER(), anyC())
        verify(bottomUpSession).exec(eq(combKey), eq(combTask), anyER(), anyC())
        verify(bottomUpSession).require(eq(lowerRevKey), eq(lowerRevTask), any(), anyC())
        verify(bottomUpSession).exec(eq(lowerRevKey), eq(lowerRevTask), anyER(), anyC())
      }
    }

    // Notify of file change, but file hasn't actually changed, observe no execution.
    newSession().use { session ->
      session.updateAffectedBy(setOf(file.key), NullCancelled())
      // Since no task has been affected by the file change, no observers are observed (all asserts are same as last session).
      Assertions.assertEquals("!dlrow olleh", combOutput)
      Assertions.assertEquals(2, combObserved)
      Assertions.assertEquals("!DLROW OLLEH", readOutput)
      Assertions.assertEquals(2, readObserved)
      Assertions.assertEquals("hello world!", lowerOutput)
      Assertions.assertEquals(1, lowerObserved)
      Assertions.assertEquals("!dlrow olleh", lowerRevOutput)
      Assertions.assertEquals(1, lowerRevObserved)
      val bottomUpSession = session.bottomUpSession
      verify(bottomUpSession, never()).exec(eq(readKey), eq(readTask), anyER(), anyC())
      verify(bottomUpSession, never()).exec(eq(combKey), eq(combTask), anyER(), anyC())
      verify(bottomUpSession, never()).exec(eq(lowerRevKey), eq(lowerRevTask), anyER(), anyC())
    }

    // Change required file in such a way that the file changes (modified date), but the output of [readTask] does not.
    write(newStr, file)

    // Notify of file change, observe bottom-up execution of [readTask], but stop there because [combineTask] is still consistent.
    newSession().use { session ->
      session.updateAffectedBy(setOf(file.key), NullCancelled())
      Assertions.assertEquals("!dlrow olleh", combOutput)
      Assertions.assertEquals(2, combObserved)
      Assertions.assertEquals("!DLROW OLLEH", readOutput)
      Assertions.assertEquals(3, readObserved)
      Assertions.assertEquals("hello world!", lowerOutput)
      Assertions.assertEquals(1, lowerObserved)
      Assertions.assertEquals("!dlrow olleh", lowerRevOutput)
      Assertions.assertEquals(1, lowerRevObserved)
      val bottomUpSession = session.bottomUpSession
      inOrder(bottomUpSession) {
        verify(bottomUpSession).exec(eq(readKey), eq(readTask), anyER(), anyC())
      }
      verify(bottomUpSession, never()).exec(eq(combKey), eq(combTask), anyER(), anyC())
      verify(bottomUpSession, never()).require(eq(lowerRevKey), eq(lowerRevTask), any(), anyC())
      verify(bottomUpSession, never()).exec(eq(lowerRevKey), eq(lowerRevTask), anyER(), anyC())
    }
  }
}