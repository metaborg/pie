package mb.pie.share.coroutine

import com.nhaarman.mockitokotlin2.mockingDetails
import com.nhaarman.mockitokotlin2.spy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mb.pie.api.test.toLowerCase
import mb.pie.runtime.test.DefaultRuntimeTestBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.TestFactory

class CoroutineShareTests {
  private val builder = CoroutineShareTestBuilder()


  @TestFactory
  fun testThreadSafety() = builder.test {
    addTaskDef(toLowerCase)

    runBlocking {
      List(100) { index ->
        launch(coroutineContext + Dispatchers.Default) {
          newSession().use { session ->
            val task = toLowerCase.createTask("HELLO WORLD $index!")
            session.require(task)
          }
        }
      }.forEach { it.join() }
    }
  }

  @Disabled("Coroutine share is not uniquely sharing on every platform; disable for now")
  @TestFactory
  fun testConcurrentReuse() = builder.test {
    val lowerDef = spy(toLowerCase)
    addTaskDef(lowerDef)

    // Run task concurrently 100 times.
    runBlocking {
      List(100) {
        launch(coroutineContext + Dispatchers.Default) {
          newSession().use { session ->
            val task = lowerDef.createTask("HELLO WORLD!")
            session.require(task)
          }
        }
      }.forEach { it.join() }
    }

    // Test that function 'exec' has only been called once, even between all threads.
    var invocations = 0
    mockingDetails(lowerDef).invocations
      .filter { it.method.name == "exec" }
      .forEach { ++invocations }
    assertEquals(1, invocations)
  }
}

class CoroutineShareTestBuilder(shouldSpy: Boolean = true) : DefaultRuntimeTestBuilder(shouldSpy) {
  init {
    shareFactories.clear()
    shareFactories.add { CoroutineShare() }
  }
}
