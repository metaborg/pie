package mb.ceres

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import mb.ceres.internal.BuildImpl
import mb.ceres.internal.BuildShare
import mb.ceres.internal.Store
import name.falgout.jeffrey.testing.junit5.GuiceExtension
import name.falgout.jeffrey.testing.junit5.IncludeModule
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(GuiceExtension::class)
@IncludeModule(TestModule::class)
internal class ConcurrencyTests : TestBase() {
  @UseBuildStores
  fun testThreadSafety(store: Store, bStore: BuilderStore, share: BuildShare) {
    bStore.registerBuilder(toLowerCase)

    runBlocking {
      List(100) { index ->
        launch(context + CommonPool) {
          val build = BuildImpl(store, bStore, share)
          val app = a(toLowerCase, "HELLO WORLD $index!")
          build.require(app)
        }
      }.forEach { it.join() }
    }
  }
}