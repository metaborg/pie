package mb.ceres

import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.verify
import mb.ceres.internal.LMDBBuildStore
import java.io.File

internal class LMDBBuildStoreTests : ParametrizedTestBase() {
  @UseBuildVariability
  fun testReuse() {
    registerBuilder(toLowerCase)

    LMDBBuildStore(File("build/test/lmdbstore")).use {
      it.reset()
      val build = b(it, cache, share)
      build.require(a(toLowerCase, "HELLO WORLD!"))
    }

    // Close and re-open the database
    LMDBBuildStore(File("build/test/lmdbstore")).use {
      val build = spy(b(it, cache, share))
      val app = a(toLowerCase, "HELLO WORLD!")
      build.require(app)
      verify(build, never()).rebuild(app)
    }
  }
}