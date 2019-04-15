package mb.pie.runtime.test

import mb.pie.api.TaskDef
import mb.pie.api.test.ApiTestCtx
import mb.pie.runtime.PieImpl
import mb.pie.runtime.PieSessionImpl
import mb.pie.runtime.taskdefs.MapTaskDefs
import java.nio.file.FileSystem

open class RuntimeTestCtx(
  private val pieImpl: PieImpl,
  private val taskDefs: MapTaskDefs,
  fs: FileSystem
) : ApiTestCtx(pieImpl, fs) {
  override val pie: PieImpl get() = pieImpl

  override fun newSession(): PieSessionImpl = pie.newSession() as PieSessionImpl

  fun addTaskDef(taskDef: TaskDef<*, *>) {
    taskDefs.add(taskDef)
  }
}
