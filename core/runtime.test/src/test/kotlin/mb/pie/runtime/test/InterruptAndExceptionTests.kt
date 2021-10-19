package mb.pie.runtime.test

import com.nhaarman.mockitokotlin2.*
import mb.pie.api.InconsistentResourceRequire
import mb.pie.api.MapTaskDefs
import mb.pie.api.exec.InterruptCancelableToken
import mb.pie.api.test.anyC
import mb.pie.runtime.exec.NoData
import mb.resource.fs.FSResource
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestFactory
import java.nio.file.FileSystem

class InterruptAndExceptionTestCtx(fs: FileSystem, taskDefs: MapTaskDefs, pieImpl: TestPieImpl) : RuntimeTestCtx(fs, taskDefs, pieImpl) {
  val sometimesThrows = taskDef<FSResource, String>("sometimesThrows") { file ->
    require(file)
    val text = file.readString()
    if(text.contains("throw")) throw Exception("Exception")
    text
  }

  val sometimesInterrupts = taskDef<FSResource, String>("sometimesInterrupts") { file ->
    require(file)
    val text = file.readString()
    if(text.contains("interrupt")) Thread.currentThread().interrupt()
    text
  }
}

class InterruptAndExceptionTests {
  private val builder = RuntimeTestBuilder { fs, taskDefs, pie ->
    InterruptAndExceptionTestCtx(fs, taskDefs, pie as TestPieImpl)
  }

  @TestFactory
  fun testExceptionalFailureRestoreData() = builder.test {
    val sometimesThrows = spy(sometimesThrows)
    addTaskDef(sometimesThrows)

    val file = resource("/input1.str")
    write("test", file)
    newSession().use { session ->
      val task = sometimesThrows.createTask(file)
      val result1 = session.require(task)
      assertEquals("test", result1)
    }

    write("throw", file)
    newSession().use { session ->
      val task = sometimesThrows.createTask(file)
      assertThrows(Exception::class.java) {
        session.require(task)
      }
    }

    newSession().use { session ->
      val task = sometimesThrows.createTask(file)
      val key = task.key()
      assertThrows(Exception::class.java) {
        session.require(task)
      }
      // Verify that task is re-executed due to InconsistentResourceRequire, even though nothing changed, because the
      // task failed exceptionally and its previous state was restored.
      val topDownSession = session.topDownRunner
      inOrder(topDownSession, sometimesThrows) {
        verify(topDownSession, times(1)).exec(eq(key), eq(task), argThat {
          if(this is InconsistentResourceRequire) {
            this.dep.key == file.key
          } else {
            false
          }
        }, any(), any(), anyC())
      }
    }
  }

  @TestFactory
  fun testExceptionalFailureResetData() = builder.test {
    val sometimesThrows = spy(sometimesThrows)
    addTaskDef(sometimesThrows)

    val file = resource("/input1.str")
    write("throw", file)
    newSession().use { session ->
      val task = sometimesThrows.createTask(file)
      assertThrows(Exception::class.java) {
        session.require(task)
      }
    }

    newSession().use { session ->
      val task = sometimesThrows.createTask(file)
      val key = task.key()
      assertThrows(Exception::class.java) {
        session.require(task)
      }
      // Verify that task is re-executed due to NoData, even though nothing changed, because the task failed
      // exceptionally and its state was reset.
      val topDownSession = session.topDownRunner
      inOrder(topDownSession, sometimesThrows) {
        verify(topDownSession, times(1)).exec(eq(key), eq(task), eq(NoData()), any(), any(), anyC())
      }
    }
  }

  @TestFactory
  fun testInterruptRestoreData() = builder.test {
    val sometimesInterrupts = spy(sometimesInterrupts)
    addTaskDef(sometimesInterrupts)

    val file = resource("/input1.str")
    write("test", file)

    Thread.interrupted() // Clear interrupted status.
    newSession().use { session ->
      val task = sometimesInterrupts.createTask(file)
      val result1 = session.require(task, InterruptCancelableToken())
      assertEquals("test", result1)
    }

    Thread.interrupted() // Clear interrupted status.
    write("interrupt", file)
    newSession().use { session ->
      val task = sometimesInterrupts.createTask(file)
      assertThrows(InterruptedException::class.java) {
        session.require(task, InterruptCancelableToken())
      }
    }

    Thread.interrupted() // Clear interrupted status.
    newSession().use { session ->
      val task = sometimesInterrupts.createTask(file)
      val key = task.key()
      assertThrows(InterruptedException::class.java) {
        session.require(task, InterruptCancelableToken())
      }
      // Verify that task is re-executed due to InconsistentResourceRequire, even though nothing changed, because the
      // task failed exceptionally and its previous state was restored.
      val topDownSession = session.topDownRunner
      inOrder(topDownSession, sometimesInterrupts) {
        verify(topDownSession, times(1)).exec(eq(key), eq(task), argThat {
          if(this is InconsistentResourceRequire) {
            this.dep.key == file.key
          } else {
            false
          }
        }, any(), any(), anyC())
      }
    }
  }

  @TestFactory
  fun testInterruptResetData() = builder.test {
    val sometimesInterrupts = spy(sometimesInterrupts)
    addTaskDef(sometimesInterrupts)

    val file = resource("/input1.str")
    write("interrupt", file)

    Thread.interrupted() // Clear interrupted status.
    newSession().use { session ->
      val task = sometimesInterrupts.createTask(file)
      assertThrows(InterruptedException::class.java) {
        session.require(task, InterruptCancelableToken())
      }
    }

    Thread.interrupted() // Clear interrupted status.
    newSession().use { session ->
      val task = sometimesInterrupts.createTask(file)
      val key = task.key()
      assertThrows(InterruptedException::class.java) {
        session.require(task, InterruptCancelableToken())
      }
      // Verify that task is re-executed due to NoData, even though nothing changed, because the task failed
      // exceptionally and its state was reset.
      val topDownSession = session.topDownRunner
      inOrder(topDownSession, sometimesInterrupts) {
        verify(topDownSession, times(1)).exec(eq(key), eq(task), eq(NoData()), any(), any(), anyC())
      }
    }
  }
}

