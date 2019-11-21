package mb.pie.runtime.test

import mb.pie.api.MapTaskDefs
import mb.pie.api.TaskDef
import mb.pie.api.test.ApiTestCtx
import java.nio.file.FileSystem

open class RuntimeTestCtx(
  fileSystem: FileSystem,
  private val taskDefs: MapTaskDefs,
  private val testPieImpl: TestPieImpl
) : ApiTestCtx(fileSystem, testPieImpl) {
  override val pie: TestPieImpl get() = testPieImpl

  override fun newSession(): TestPieSessionImpl = pie.newSession() as TestPieSessionImpl

  fun addTaskDef(taskDef: TaskDef<*, *>) {
    taskDefs.add(taskDef)
  }
}
