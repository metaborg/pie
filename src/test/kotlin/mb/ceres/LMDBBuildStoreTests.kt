package mb.ceres

import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.verify
import mb.ceres.internal.LMDBBuildStore
import mb.ceres.internal.LMDBBuildStoreFactory
import java.io.File

internal class LMDBBuildStoreTests : ParametrizedTestBase() {
  val factory = LMDBBuildStoreFactory()
  
  @UseBuildVariability
  fun testReuse() {
    registerBuilder(toLowerCase)

    factory.create(File("build/test/lmdbstore")).use {
      it.drop()
      val build = b(it, cache, share)
      build.require(a(toLowerCase, "HELLO WORLD!"))
    }

    // Close and re-open the database
    factory.create(File("build/test/lmdbstore")).use {
      val build = spy(b(it, cache, share))
      val app = a(toLowerCase, "HELLO WORLD!")
      build.require(app)
      verify(build, never()).rebuild(app)
    }
  }
}