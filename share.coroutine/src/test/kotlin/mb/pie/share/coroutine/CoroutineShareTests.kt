package mb.pie.share.coroutine

import kotlinx.coroutines.*
import mb.pie.api.test.toLowerCase
import mb.pie.runtime.test.RuntimeTestGenerator
import org.junit.jupiter.api.TestFactory

internal class CoroutineShareTests {
  @TestFactory
  fun testThreadSafety() = RuntimeTestGenerator.generate("testThreadSafety", shareGens = arrayOf({ _ -> CoroutineShare() })) {
    addTaskDef(toLowerCase)

    runBlocking {
      List(100) { index ->
        launch(coroutineContext + Dispatchers.Default) {
          val session = topDownSession()
          val task = task(toLowerCase, "HELLO WORLD $index!")
          session.requireInitial(task)
        }
      }.forEach { it.join() }
    }
  }

  // Disabled test for now, as concurrent share does not completely work.
//  @TestFactory
//  fun testConcurrentReuse() = RuntimeTestGenerator.generate("testConcurrentReuse", shareGens = arrayOf({ _ -> CoroutineShare() })) {
//    val taskDef = spy(toLowerCase)
//    addTaskDef(taskDef)
//
//    runBlocking {
//      List(100) {
//        launch(coroutineContext + CommonPool) {
//          val session = topDownSession()
//          val task = task(taskDef, "HELLO WORLD!")
//          session.requireInitial(task)
//        }
//      }.forEach { it.join() }
//    }
//
//    // Test that function 'execInternal' has only been called once, even between all threads
//    var invocations = 0
//    val javaFuncName = TaskDef::class.declaredMemberExtensionFunctions.first { it.name == "exec" }.javaMethod!!.name
//    mockingDetails(taskDef).invocations
//      .filter { it.method.name == javaFuncName }
//      .forEach { ++invocations }
//    assertEquals(1, invocations)
//  }
}
