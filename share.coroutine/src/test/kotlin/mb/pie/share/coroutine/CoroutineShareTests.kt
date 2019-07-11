package mb.pie.share.coroutine

import com.nhaarman.mockitokotlin2.mockingDetails
import com.nhaarman.mockitokotlin2.spy
import kotlinx.coroutines.*
import mb.pie.api.test.toLowerCase
import mb.pie.runtime.test.RuntimeTestGenerator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.TestFactory

internal class CoroutineShareTests {
  @TestFactory
  fun testThreadSafety() = RuntimeTestGenerator.generate("testThreadSafety", shareGens = arrayOf({ _ -> CoroutineShare() })) {
    addTaskDef(toLowerCase)

    runBlocking {
      List(100) { index ->
        launch(coroutineContext + Dispatchers.Default) {
          val session = newSession()
          val task = task(toLowerCase, "HELLO WORLD $index!")
          session.require(task)
        }
      }.forEach { it.join() }
    }
  }

  @Disabled("Coroutine share is not uniquely sharing on every platform; disable for now")
  @TestFactory
  fun testConcurrentReuse() = RuntimeTestGenerator.generate("testConcurrentReuse", shareGens = arrayOf({ _ -> CoroutineShare() })) {
    val taskDef = spy(toLowerCase)
    addTaskDef(taskDef)

    // Run task concurrently 100 times.
    runBlocking {
      List(100) {
        launch(coroutineContext + Dispatchers.Default) {
          val session = newSession()
          val task = task(taskDef, "HELLO WORLD!")
          session.require(task)
        }
      }.forEach { it.join() }
    }

    // Test that function 'exec' has only been called once, even between all threads.
    var invocations = 0
    mockingDetails(taskDef).invocations
      .filter { it.method.name == "exec" }
      .forEach { ++invocations }
    assertEquals(1, invocations)
  }
}
