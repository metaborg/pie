package mb.ceres

import com.nhaarman.mockito_kotlin.*
import mb.ceres.impl.LMDBBuildStoreFactory
import java.io.File

internal class LMDBBuildStoreTests : ParametrizedTestBase() {
  val factory = LMDBBuildStoreFactory()

  @UseBuildVariability
  fun testReuse() {
    registerBuilder(toLowerCase)

    factory.create(File("build/test/lmdbstore")).use {
      it.drop()
      val build = b(it, cache, share, reporter)
      build.require(a(toLowerCase, "HELLO WORLD!"))
    }

    // Close and re-open the database
    factory.create(File("build/test/lmdbstore")).use {
      val build = spy(b(it, cache, share, reporter))
      val app = a(toLowerCase, "HELLO WORLD!")
      build.require(app)
      verify(build, never()).rebuild(eq(app), any(), any())
    }
  }
}