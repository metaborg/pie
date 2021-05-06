package mb.pie.store.lmdb

import com.nhaarman.mockitokotlin2.*
import mb.pie.api.PieBuilder
import mb.pie.api.test.anyC
import mb.pie.api.test.toLowerCase
import mb.pie.runtime.exec.NoData
import mb.pie.runtime.test.DefaultRuntimeTestBuilder
import org.junit.jupiter.api.TestFactory
import java.io.File

class LMDBStoreTests {
  private val builder = LMDBStoreTestBuilder()


  @TestFactory
  fun testReuse() = builder.test {
    addTaskDef(toLowerCase)
    val task = toLowerCase.createTask("HELLO WORLD!")
    val key = task.key()

    newSession().use { session ->
      session.require(task)
    }

    newSession().use { session ->
      session.require(task)
      verify(session.topDownRunner, never()).exec(eq(key), eq(task), eq(NoData()), any(), any(), anyC())
    }
  }
}

class LMDBStoreTestBuilder(shouldSpy: Boolean = true) : DefaultRuntimeTestBuilder(shouldSpy) {
  init {
    storeFactories.clear()
    storeFactories.add(PieBuilder.StoreFactory { serde, _, loggerFactory -> LMDBStore(serde, File("build/test/lmdbstore"), loggerFactory) })
  }
}
