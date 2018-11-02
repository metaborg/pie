package mb.pie.runtime.test

import com.nhaarman.mockito_kotlin.*
import mb.fs.java.JavaFSNode
import mb.pie.api.fs.toResourceKey
import mb.pie.api.test.*
import mb.pie.runtime.exec.NoData
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestFactory

internal class BottomUpTests {
  @TestFactory
  fun testTopDownExec() = RuntimeTestGenerator.generate("testTopDownExec") {
    val taskDef = spy(toLowerCase)
    addTaskDef(taskDef)

    val input = "CAPITALIZED"
    val task = task(taskDef, input)
    val key = task.key()

    val session = spy(bottomUpSession())
    val output = session.requireTopDownInitial(task)
    Assertions.assertEquals("capitalized", output)

    inOrder(session, taskDef) {
      verify(session, times(1)).requireTopDownInitial(eq(task), anyC())
      verify(session, times(1)).exec(eq(key), eq(task), eq(NoData()), anyC())
    }
  }

  @TestFactory
  fun testTopDownExecMultiple() = RuntimeTestGenerator.generate("testTopDownExecMultiple") {
    val taskDef = toLowerCase
    addTaskDef(taskDef)

    val input1 = "CAPITALIZED"
    val task1 = task(taskDef, input1)
    val key1 = task1.key()
    val session1 = spy(bottomUpSession())
    val output1 = session1.requireTopDownInitial(task1)
    Assertions.assertEquals("capitalized", output1)

    val input2 = "CAPITALIZED_EVEN_MORE"
    val task2 = task(taskDef, input2)
    val key2 = task2.key()
    val session2 = spy(bottomUpSession())
    val output2 = session2.requireTopDownInitial(task2)
    Assertions.assertEquals("capitalized_even_more", output2)

    Assertions.assertNotEquals(output1, output2)

    inOrder(session1, session2) {
      verify(session1, times(1)).requireTopDownInitial(eq(task1), anyC())
      verify(session1, times(1)).exec(eq(key1), eq(task1), eq(NoData()), anyC())

      verify(session2, times(1)).requireTopDownInitial(eq(task2), anyC())
      verify(session2, times(1)).exec(eq(key2), eq(task2), eq(NoData()), anyC())
    }
  }

  @TestFactory
  fun testTopDownReuse() = RuntimeTestGenerator.generate("testTopDownReuse") {
    val taskDef = spy(toLowerCase)
    addTaskDef(taskDef)

    val input = "CAPITALIZED"
    val task = task(taskDef, input)
    val key = task.key()

    val session1 = bottomUpSession()
    val output1 = session1.requireTopDownInitial(task)
    Assertions.assertEquals("capitalized", output1)

    val session2 = spy(bottomUpSession())
    val output2 = session2.requireTopDownInitial(task)
    Assertions.assertEquals("capitalized", output2)

    Assertions.assertEquals(output1, output2)

    // Result is reused if rebuild is never called.
    verify(session2, never()).exec(eq(key), eq(task), eq(NoData()), anyC())
  }

  @TestFactory
  fun testFileChange() = RuntimeTestGenerator.generate("testFileChange") {
    val lowerDef = spy(toLowerCase)
    addTaskDef(lowerDef)
    val readDef = spy(readPath)
    addTaskDef(readDef)
    val combDef = spy(taskDef<JavaFSNode, String>("combine", { input, _ -> "toLowerCase(read($input))" }) {
      val text = require(task(readDef, it))
      require(task(lowerDef, text))
    })
    addTaskDef(combDef)

    val str = "HELLO WORLD!"
    val fileNode = fsNode("/file")
    write(str, fileNode)

    val combTask = task(combDef, fileNode)
    val combKey = combTask.key()
    val readTask = task(readDef, fileNode)
    val readKey = readTask.key()
    val lowerTask = task(lowerDef, str)
    val lowerKey = lowerTask.key()

    // Build 'combine', observe rebuild of all.
    val session1 = spy(bottomUpSession())
    val output1 = session1.requireTopDownInitial(combTask)
    Assertions.assertEquals("hello world!", output1)
    inOrder(session1) {
      verify(session1).exec(eq(combKey), eq(combTask), eq(NoData()), anyC())
      verify(session1).exec(eq(readKey), eq(readTask), eq(NoData()), anyC())
      verify(session1).exec(eq(lowerKey), eq(lowerTask), eq(NoData()), anyC())
    }

    // Change required file in such a way that the output of 'readPath' changes (change file content).
    val newStr = "!DLROW OLLEH"
    write(newStr, fileNode)

    val lowerRevTask = task(lowerDef, newStr)
    val lowerRevKey = lowerRevTask.key()

    // Notify of file change, observe bottom-up execution to [combine], and then top-down execution of [toLowerCase].
    val session2 = spy(bottomUpSession())
    session2.requireBottomUpInitial(setOf(fileNode.toResourceKey()))
    inOrder(session2) {
      verify(session2).exec(eq(readKey), eq(readTask), anyER(), anyC())
      verify(session2).exec(eq(combKey), eq(combTask), anyER(), anyC())
      verify(session2).require(eq(lowerRevKey), eq(lowerRevTask), anyC())
      verify(session2).exec(eq(lowerRevKey), eq(lowerRevTask), anyER(), anyC())
    }

    // Notify of file change, but file hasn't actually changed, observe no execution.
    val session3 = spy(bottomUpSession())
    session3.requireBottomUpInitial(setOf(fileNode.toResourceKey()))
    verify(session3, never()).exec(eq(readKey), eq(readTask), anyER(), anyC())
    verify(session3, never()).exec(eq(combKey), eq(combTask), anyER(), anyC())
    verify(session3, never()).exec(eq(lowerRevKey), eq(lowerRevTask), anyER(), anyC())

    // Change required file in such a way that the file changes, but the output of [readPath] does not.
    write(newStr, fileNode)

    // Notify of file change, observe bottom-up execution of [readPath], but stop there because [combine] is still consistent.
    val exec4 = spy(bottomUpSession())
    exec4.requireBottomUpInitial(setOf(fileNode.toResourceKey()))
    inOrder(exec4) {
      verify(exec4).exec(eq(readKey), eq(readTask), anyER(), anyC())
    }
    verify(exec4, never()).exec(eq(combKey), eq(combTask), anyER(), anyC())
    verify(exec4, never()).require(eq(lowerRevKey), eq(lowerRevTask), anyC())
    verify(exec4, never()).exec(eq(lowerRevKey), eq(lowerRevTask), anyER(), anyC())
  }
}