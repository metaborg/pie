package mb.pie.runtime.test

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import com.nhaarman.mockitokotlin2.*
import mb.pie.api.InconsistentResourceRequire
import mb.pie.api.MapTaskDefs
import mb.pie.api.test.anyC
import mb.pie.api.test.anyER
import mb.pie.api.test.readResource
import mb.pie.runtime.exec.NoData
import mb.pie.runtime.store.InMemoryStore
import mb.pie.runtime.store.SerializingStore
import mb.resource.fs.FSResource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.nio.file.FileSystem
import java.util.function.Supplier

class SerializingStoreTests {
  @Test
  fun testSerializeDezerialize() {
    // Manually create PIE instance, as we need to control the serialization/deserialization of the InMemoryStore.
    val fileSystem = Jimfs.newFileSystem(Configuration.unix())
    val pieBuilder = TestPieBuilderImpl(true)
    pieBuilder.withStoreFactory { _, _ -> SerializingStore(FSResource(fileSystem.getPath("store")), Supplier { InMemoryStore() }) }
    val taskDefs = MapTaskDefs()
    pieBuilder.withTaskDefs(taskDefs)

    pieBuilder.build().use { pie ->
      InMemoryStoreTestCtx(fileSystem, taskDefs, pie).run {
        write("HELLO WORLD!", file)
        val task = readDef.createTask(file)
        val key = task.key()

        // Build 'readPath', observe rebuild.
        newSession().use { session ->
          val output = session.require(task)
          Assertions.assertEquals("HELLO WORLD!", output)
          verify(session.topDownRunner, times(1)).exec(eq(key), eq(task), eq(NoData()), any(), anyC())
        }
      }
    }

    // Serialize-deserialize roundtrip due to pie instance being closed.
    pieBuilder.build().use { pie ->
      InMemoryStoreTestCtx(fileSystem, taskDefs, pie).run {
        val task = readDef.createTask(file)
        val key = task.key()

        // No changes - exec 'readPath', observe no rebuild.
        newSession().use { session ->
          val output = session.require(task)
          Assertions.assertEquals("HELLO WORLD!", output)
          verify(session.topDownRunner, never()).exec(eq(key), eq(task), anyER(), any(), anyC())
        }
      }
    }

    // Serialize-deserialize roundtrip due to pie instance being closed.
    pieBuilder.build().use { pie ->
      InMemoryStoreTestCtx(fileSystem, taskDefs, pie).run {
        val task = readDef.createTask(file)
        val key = task.key()

        // Change required file in such a way that the output of 'readPath' changes (change file content).
        write("!DLROW OLLEH", file)

        // Run again - expect rebuild.
        newSession().use { session ->
          val output = session.require(task)
          Assertions.assertEquals("!DLROW OLLEH", output)
          verify(session.topDownRunner, times(1)).exec(eq(key), eq(task), check {
            val reason = it as? InconsistentResourceRequire
            Assertions.assertNotNull(reason)
            Assertions.assertEquals(file.key, reason!!.dep.key)
          }, any(), anyC())
        }
      }
    }
  }
}

class InMemoryStoreTestCtx(
  fileSystem: FileSystem,
  taskDefs: MapTaskDefs,
  testPieImpl: TestPieImpl
) : RuntimeTestCtx(fileSystem, taskDefs, testPieImpl) {
  val readDef = spy(readResource)
  val file = resource("/file")

  init {
    addTaskDef(readDef)
  }
}
