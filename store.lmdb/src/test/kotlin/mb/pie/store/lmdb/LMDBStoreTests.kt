package mb.pie.store.lmdb

import com.nhaarman.mockito_kotlin.*
import mb.pie.api.test.toLowerCase
import mb.pie.runtime.exec.NoOutputReason
import mb.pie.runtime.test.RuntimeTestGenerator
import org.junit.jupiter.api.TestFactory
import java.io.File

internal class LMDBStoreTests {
  @TestFactory
  fun testReuse() = RuntimeTestGenerator.generate("testReuse", storeGens = arrayOf({ logger -> LMDBStore(logger, File("target/test/lmdbstore")) })) {
    addTaskDef(toLowerCase)
    val app = app(toLowerCase, "HELLO WORLD!")

    val exec1 = topDownExec()
    exec1.requireInitial(app)

    val exec2 = spy(topDownExec())
    exec2.requireInitial(app)
    verify(exec2, never()).exec(eq(app), eq(NoOutputReason()), any(), any())
  }
}
