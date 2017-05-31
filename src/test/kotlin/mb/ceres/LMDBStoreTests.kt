package mb.ceres

import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.verify
import mb.ceres.internal.BuildShare
import mb.ceres.internal.LMDBStore
import name.falgout.jeffrey.testing.junit5.GuiceExtension
import name.falgout.jeffrey.testing.junit5.IncludeModule
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File

@ExtendWith(GuiceExtension::class)
@IncludeModule(TestModule::class)
internal class LMDBStoreTests : TestBase() {
  @Test
  fun testReuse(bStore: BuilderStore, share: BuildShare) {
    bStore.registerBuilder(toLowerCase)

    LMDBStore(File("build/test/lmdbstore")).use {
      it.reset()
      val build = b(it, bStore, share)
      build.require(a(toLowerCase, "HELLO WORLD!"))
    }

    // Close and re-open the database
    LMDBStore(File("build/test/lmdbstore")).use {
      val build = spy(b(it, bStore, share))
      val app = a(toLowerCase, "HELLO WORLD!")
      build.require(app)
      verify(build, never()).rebuild(app)
    }
  }
}