package mb.pie.runtime.logger.exec

import mb.pie.api.*
import mb.pie.api.exec.ExecReason
import mb.pie.vfs.path.PPath

class NoopExecutorLogger : ExecutorLogger {
  override fun requireTopDownInitialStart(key: TaskKey, task: Task<*, *>) {}
  override fun requireTopDownInitialEnd(key: TaskKey, task: Task<*, *>, output: Out) {}
  override fun requireTopDownStart(key: TaskKey, task: Task<*, *>) {}
  override fun requireTopDownEnd(key: TaskKey, task: Task<*, *>, output: Out) {}
  override fun requireBottomUpInitialStart(changedFiles: Set<PPath>) {}
  override fun requireBottomUpInitialEnd() {}
  override fun checkVisitedStart(key: TaskKey) {}
  override fun checkVisitedEnd(key: TaskKey, output: Out) {}
  override fun checkStoredStart(key: TaskKey) {}
  override fun checkStoredEnd(key: TaskKey, output: Out) {}
  override fun checkFileGenStart(key: TaskKey, task: Task<*, *>, fileGen: FileGen) {}
  override fun checkFileGenEnd(key: TaskKey, task: Task<*, *>, fileGen: FileGen, reason: ExecReason?) {}
  override fun checkFileReqStart(key: TaskKey, task: Task<*, *>, fileReq: FileReq) {}
  override fun checkFileReqEnd(key: TaskKey, task: Task<*, *>, fileReq: FileReq, reason: ExecReason?) {}
  override fun checkTaskReqStart(key: TaskKey, task: Task<*, *>, taskReq: TaskReq) {}
  override fun checkTaskReqEnd(key: TaskKey, task: Task<*, *>, taskReq: TaskReq, reason: ExecReason?) {}
  override fun executeStart(key: TaskKey, task: Task<*, *>, reason: ExecReason) {}
  override fun executeEnd(key: TaskKey, task: Task<*, *>, reason: ExecReason, data: TaskData<*, *>) {}
  override fun invokeObserverStart(observer: Function<Unit>, key: TaskKey, output: Out) {}
  override fun invokeObserverEnd(observer: Function<Unit>, key: TaskKey, output: Out) {}
}
