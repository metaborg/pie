package mb.pie.runtime.core.test

import com.nhaarman.mockito_kotlin.*
import mb.pie.runtime.core.impl.store.LMDBBuildStoreFactory
import mb.pie.runtime.core.impl.store.NoopStore
import mb.pie.runtime.core.test.util.TestGenerator
import org.junit.jupiter.api.TestFactory
import java.io.File

internal class LMDBStoreTests {
  @TestFactory
  fun testReuse() = TestGenerator.generate("testReuse", dStoreGens = arrayOf({ NoopStore() })) {
    val factory = LMDBBuildStoreFactory(metaborgLogger)

    registerFunc(toLowerCase)

    factory.create(File("build/test/lmdbstore")).use {
      it.writeTxn().use { it.drop() }
      val exec = pullingExec(it, cache, share, layerProvider.get(), logger)
      exec.require(app(toLowerCase, "HELLO WORLD!"))
    }

    // Close and re-open the database
    factory.create(File("build/test/lmdbstore")).use {
      val exec = spy(pullingExec(it, cache, share, layerProvider.get(), logger))
      val app = app(toLowerCase, "HELLO WORLD!")
      exec.require(app)
      verify(exec, never()).exec(eq(app), any(), any())
    }
  }
}
