package mb.pie.store.lmdb

import com.nhaarman.mockitokotlin2.*
import mb.pie.api.exec.NullCancelled
import mb.pie.api.test.anyC
import mb.pie.api.test.toLowerCase
import mb.pie.runtime.exec.NoData
import mb.pie.runtime.test.RuntimeTestGenerator
import org.junit.jupiter.api.TestFactory
import java.io.File

internal class LMDBStoreTests {
  @TestFactory
  fun testReuse() = RuntimeTestGenerator.generate("testReuse", storeGens = arrayOf({ logger -> LMDBStore(logger, File("build/test/lmdbstore")) })) {
    addTaskDef(toLowerCase)
    val task = task(toLowerCase, "HELLO WORLD!")
    val key = task.key()

    val session1 = newSession().topDownSession
    session1.requireInitial(task, true, NullCancelled())

    val session2 = spy(newSession().topDownSession)
    session2.requireInitial(task, true, NullCancelled())
    verify(session2, never()).exec(eq(key), eq(task), eq(NoData()), eq(true), anyC())
  }
}
