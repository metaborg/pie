package mb.pie.runtime.test

import mb.pie.api.*
import mb.pie.api.test.ApiTestCtx
import mb.pie.runtime.PieImpl
import mb.pie.runtime.exec.*
import mb.pie.runtime.taskdefs.MutableMapTaskDefs
import java.nio.file.FileSystem

open class RuntimeTestCtx(
  private val pieImpl: PieImpl,
  private val taskDefs: MutableMapTaskDefs,
  fs: FileSystem
) : ApiTestCtx(pieImpl, fs) {
  override val pie: PieImpl get() = pieImpl
  override val topDownExecutor: TopDownExecutorImpl get() = pie.topDownExecutor as TopDownExecutorImpl
  override val bottomUpExecutor: BottomUpExecutorImpl get() = pie.bottomUpExecutor as BottomUpExecutorImpl

  override fun topDownExec(): TopDownSessionImpl {
    return pieImpl.topDownExecutor.newSession() as TopDownSessionImpl
  }

  fun bottomUpExec(observers: Map<UTask, TaskObserver> = mapOf()): BottomUpSession {
    for(pair in observers) {
      bottomUpExecutor.setObserver(pair.key, pair.value)
    }
    return bottomUpExecutor.newSession()
  }

  fun addTaskDef(taskDef: UTaskDef) {
    taskDefs.addTaskDef(taskDef.id, taskDef)
  }
}
