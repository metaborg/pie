package mb.ceres

import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.verify
import mb.ceres.internal.BuildManagerImpl
import mb.ceres.internal.LMDBBuildStore
import name.falgout.jeffrey.testing.junit5.GuiceExtension
import name.falgout.jeffrey.testing.junit5.IncludeModule
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File

@ExtendWith(GuiceExtension::class)
@IncludeModule(TestModule::class)
internal class LMDBBuildStoreTests : TestBase() {
  @Test
  fun testReuse() {
    LMDBBuildStore(File("build/test/lmdbstore")).use {
      it.reset()
      val bm = BuildManagerImpl(it)
      bm.registerBuilder(toLowerCase)
      bm.build(BuildApp(toLowerCase, "HELLO WORLD!"))
    }

    // Close and re-open the database and build manager
    LMDBBuildStore(File("build/test/lmdbstore")).use {
      val bm = spy(BuildManagerImpl(it))
      bm.registerBuilder(toLowerCase)
      val app = BuildApp(toLowerCase, "HELLO WORLD!")
      bm.build(app)
      verify(bm, never()).rebuild(app)
    }
  }
}