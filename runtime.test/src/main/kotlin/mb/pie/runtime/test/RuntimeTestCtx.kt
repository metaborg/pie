package mb.pie.runtime.test

import mb.pie.api.*
import mb.pie.api.test.ApiTestCtx
import mb.pie.runtime.PieImpl
import mb.pie.runtime.exec.*
import mb.pie.runtime.taskdefs.MutableMapTaskDefs
import java.nio.file.FileSystem
import java.util.function.Consumer

open class RuntimeTestCtx(
  private val pieImpl: PieImpl,
  private val taskDefs: MutableMapTaskDefs,
  fs: FileSystem
) : ApiTestCtx(pieImpl, fs) {
  override val pie: PieImpl get() = pieImpl
  override val topDownExecutor: TopDownExecutorImpl get() = pie.topDownExecutor as TopDownExecutorImpl
  override val bottomUpExecutor: BottomUpExecutorImpl get() = pie.bottomUpExecutor as BottomUpExecutorImpl

  override fun topDownSession(): TopDownSessionImpl {
    return pieImpl.topDownExecutor.newSession() as TopDownSessionImpl
  }

  fun bottomUpSession(observers: Map<TaskKey, Consumer<Out>> = mapOf()): BottomUpSession {
    for(pair in observers) {
      bottomUpExecutor.setObserver(pair.key, pair.value)
    }
    return bottomUpExecutor.newSession()
  }

  fun addTaskDef(taskDef: TaskDef<*, *>) {
    taskDefs.add(taskDef.id, taskDef)
  }
}
