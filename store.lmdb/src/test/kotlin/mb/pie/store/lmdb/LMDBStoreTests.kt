package mb.pie.store.lmdb

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import mb.pie.api.test.anyC
import mb.pie.api.test.toLowerCase
import mb.pie.runtime.exec.NoData
import mb.pie.runtime.test.DefaultRuntimeTestBuilder
import org.junit.jupiter.api.TestFactory
import java.io.File

class LMDBStoreTests {
  private val builder = LMDBStoreTestBuilder()


  @TestFactory
  fun testReuse() = builder.build("testReuse") {
    addTaskDef(toLowerCase)
    val task = toLowerCase.createTask("HELLO WORLD!")
    val key = task.key()

    newSession().use { session ->
      session.requireAndObserve(task)
    }

    newSession().use { session ->
      session.requireAndObserve(task)
      verify(session.topDownSession, never()).exec(eq(key), eq(task), eq(NoData()), any(), anyC())
    }
  }
}

class LMDBStoreTestBuilder(shouldSpy: Boolean = true) : DefaultRuntimeTestBuilder(shouldSpy) {
  init {
    storeFactories.clear()
    storeFactories.add { l -> LMDBStore(l, File("build/test/lmdbstore")) }
  }
}
