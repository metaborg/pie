package mb.pie.runtime.test

import mb.pie.api.MapTaskDefs
import mb.pie.api.None
import mb.pie.api.StatelessSerializableFunction
import mb.resource.fs.FSResource
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestFactory
import java.nio.file.FileSystem

data class Value(val str: String, val int: Int) : java.io.Serializable

class StrMapper : StatelessSerializableFunction<Value, String>() {
  override fun apply(t: Value): String {
    return t.str
  }
}

class IntMapper : StatelessSerializableFunction<Value, Int>() {
  override fun apply(t: Value): Int {
    return t.int
  }
}

class MultiDependencyTestCtx(fs: FileSystem, taskDefs: MapTaskDefs, pieImpl: TestPieImpl) : RuntimeTestCtx(fs, taskDefs, pieImpl) {
  val returnsValue = taskDef<None, Value>("returnsValue") {
    Value("string", 42)
  }

  val dependsOnReturnValueTwice = taskDef<FSResource, None>("dependsOnReturnValueTwice") { file ->
    require(file)
    val text = file.readString()
    if(text.contains("str")) {
      requireMapping(returnsValue, None.instance, StrMapper())
    }
    if(text.contains("int")) {
      requireMapping(returnsValue, None.instance, IntMapper())
    }
    None.instance
  }

  init {
    addTaskDef(returnsValue)
    addTaskDef(dependsOnReturnValueTwice)
  }
}

class MultiDependencyTests {
  private val builder = RuntimeTestBuilder { fs, taskDefs, pie ->
    MultiDependencyTestCtx(fs, taskDefs, pie as TestPieImpl)
  }

  @TestFactory
  fun testMultipleDependenciesUnobserveAndDelete() = builder.test {
    val file = resource("/input.txt")
    val task = dependsOnReturnValueTwice.createTask(file)

    write("str int", file)
    newSession().use { session ->
      session.require(task)
    }

    newSession().use { session ->
      session.unobserve(task)
      session.deleteUnobservedTasks({ true }, { _, _ -> true })
    }
  }

  @TestFactory
  fun testMultipleDependenciesCorrectDependenciesInStore() = builder.test {
    val file = resource("/input.txt")
    val dependeeTask = returnsValue.createTask(None.instance)
    val dependeeKey = dependeeTask.key()
    val dependerTask = dependsOnReturnValueTwice.createTask(file)
    val dependerKey = dependerTask.key()

    write("str int", file)
    newSession().use { session ->
      session.require(dependerTask)
      session.store.readTxn().use { txn ->
        assertTrue(txn.getRequiredTasks(dependerKey).contains(dependeeKey))
        assertTrue(txn.getCallersOf(dependeeKey).contains(dependerKey))
      }
      session.deleteUnobservedTasks({ true }, { _, _ -> true })
      session.store.readTxn().use { txn ->
        assertTrue(txn.getRequiredTasks(dependerKey).contains(dependeeKey))
        assertTrue(txn.getCallersOf(dependeeKey).contains(dependerKey))
      }
    }

    write("str", file)
    newSession().use { session ->
      session.require(dependerTask)
      session.store.readTxn().use { txn ->
        assertTrue(txn.getRequiredTasks(dependerKey).contains(dependeeKey))
        assertTrue(txn.getCallersOf(dependeeKey).contains(dependerKey))
      }
      session.deleteUnobservedTasks({ true }, { _, _ -> true })
      session.store.readTxn().use { txn ->
        assertTrue(txn.getRequiredTasks(dependerKey).contains(dependeeKey))
        assertTrue(txn.getCallersOf(dependeeKey).contains(dependerKey))
      }
    }

    write(" ", file)
    newSession().use { session ->
      session.require(dependerTask)
      session.store.readTxn().use { txn ->
        assertFalse(txn.getRequiredTasks(dependerKey).contains(dependeeKey))
        assertFalse(txn.getCallersOf(dependeeKey).contains(dependerKey))
      }
      session.deleteUnobservedTasks({ true }, { _, _ -> true })
      session.store.readTxn().use { txn ->
        assertFalse(txn.getRequiredTasks(dependerKey).contains(dependeeKey))
        assertFalse(txn.getCallersOf(dependeeKey).contains(dependerKey))
      }
    }
  }
}

