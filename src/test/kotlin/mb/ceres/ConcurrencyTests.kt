package mb.ceres

import com.nhaarman.mockito_kotlin.mockingDetails
import com.nhaarman.mockito_kotlin.spy
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import mb.ceres.impl.BuildImpl
import org.junit.jupiter.api.Assertions.assertEquals
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.javaMethod

internal class ConcurrencyTests : ParametrizedTestBase() {
  @UseBuildVariability
  fun testThreadSafety() {
    registerBuilder(toLowerCase)

    runBlocking {
      List(100) { index ->
        launch(context + CommonPool) {
          val build = b()
          val app = a(toLowerCase, "HELLO WORLD $index!")
          build.require(app)
        }
      }.forEach { it.join() }
    }
  }

  @UseBuildVariability
  fun testConcurrentReuse() {
    registerBuilder(toLowerCase)

    val spies = ConcurrentLinkedQueue<BuildImpl>()
    runBlocking {
      List(100) {
        launch(context + CommonPool) {
          val build = spy(b())
          spies.add(build)
          val app = a(toLowerCase, "HELLO WORLD!")
          build.require(app)
        }
      }.forEach { it.join() }
    }

    // Test that function 'rebuildInternal' has only been called once, even between all threads
    var invocations = 0
    val javaFuncName = BuildImpl::class.memberFunctions.first { it.name == "rebuildInternal" }.javaMethod!!.name
    spies.forEach { spy ->
      mockingDetails(spy).invocations
        .filter { it.method.name == javaFuncName }
        .forEach { ++invocations }
    }
    assertEquals(1, invocations)
  }
}