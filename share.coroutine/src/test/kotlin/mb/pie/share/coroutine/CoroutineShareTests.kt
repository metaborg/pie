package mb.pie.share.coroutine

import com.nhaarman.mockito_kotlin.mockingDetails
import com.nhaarman.mockito_kotlin.spy
import kotlinx.coroutines.experimental.*
import mb.pie.api.test.toLowerCase
import mb.pie.runtime.exec.TopDownSessionImpl
import mb.pie.runtime.test.RuntimeTestGenerator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestFactory
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.javaMethod

internal class CoroutineShareTests {
  @TestFactory
  fun testThreadSafety() = RuntimeTestGenerator.generate("testThreadSafety", shareGens = arrayOf({ _ -> CoroutineShare() })) {
    addTaskDef(toLowerCase)

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

  // TODO: move to share.coroutine
  @TestFactory
  fun testConcurrentReuse() = RuntimeTestGenerator.generate("testConcurrentReuse", shareGens = arrayOf({ _ -> CoroutineShare() })) {
    addTaskDef(toLowerCase)

    val spies = ConcurrentLinkedQueue<TopDownSessionImpl>()
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
    val javaFuncName = TopDownSessionImpl::class.memberFunctions.first { it.name == "execInternal" }.javaMethod!!.name
    spies.forEach { spy ->
      mockingDetails(spy).invocations
        .filter { it.method.name == javaFuncName }
        .forEach { ++invocations }
    }
    assertEquals(1, invocations)
  }
}
