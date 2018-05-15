package mb.pie.runtime.core.test

import com.nhaarman.mockito_kotlin.*
import mb.pie.runtime.core.test.util.TestGenerator
import mb.pie.runtime.impl.store.LMDBBuildStoreFactory
import mb.pie.runtime.impl.store.NoopStore
import org.junit.jupiter.api.TestFactory
import java.io.File

internal class LMDBStoreTests {
  @TestFactory
  fun testReuse() = TestGenerator.generate("testReuse", dStoreGens = arrayOf({ NoopStore() })) {
    val factory = LMDBBuildStoreFactory(mbLogger)

    registerFunc(toLowerCase)

    factory.create(File("target/test/lmdbstore")).use {
      it.writeTxn().use { it.drop() }
      val exec = pullingExec(it, cache, share, layerProvider.get(), loggerProvider.get())
      exec.requireInitial(app(toLowerCase, "HELLO WORLD!"))
    }

    // Close and re-open the database
    factory.create(File("target/test/lmdbstore")).use {
      val exec = spy(pullingExec(it, cache, share, layerProvider.get(), loggerProvider.get()))
      val app = app(toLowerCase, "HELLO WORLD!")
      exec.requireInitial(app)
      verify(exec, never()).exec(eq(app), any(), any(), any())
    }
  }
}
