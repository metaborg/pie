package mb.pie.runtime.core.test

import com.nhaarman.mockito_kotlin.*
import mb.pie.runtime.core.TestGenerator
import mb.pie.runtime.core.impl.store.LMDBBuildStoreFactory
import org.junit.jupiter.api.TestFactory
import java.io.File

internal class LMDBStoreTests {
  @TestFactory
  fun testReuse() = TestGenerator.generate("testReuse") {
    val factory = LMDBBuildStoreFactory(logger)

    registerBuilder(toLowerCase)

    factory.create(File("build/test/lmdbstore")).use {
      it.writeTxn().use { it.drop() }
      val build = b(it, cache, share, layerProvider.get(), reporter)
      build.require(a(toLowerCase, "HELLO WORLD!"))
    }

    // Close and re-open the database
    factory.create(File("build/test/lmdbstore")).use {
      val build = spy(b(it, cache, share, layerProvider.get(), reporter))
      val app = a(toLowerCase, "HELLO WORLD!")
      build.require(app)
      verify(build, never()).rebuild(eq(app), any(), any())
    }
  }
}
