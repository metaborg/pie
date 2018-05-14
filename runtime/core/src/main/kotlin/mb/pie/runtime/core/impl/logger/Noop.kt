package mb.pie.runtime.core.impl.logger

import mb.pie.runtime.core.*
import mb.pie.runtime.core.impl.*


class NoopLogger : Logger {
  override fun requireTopDownInitialStart(task: UTask) {}
  override fun requireTopDownInitialEnd(task: UTask, output: Out) {}
  override fun requireTopDownStart(task: UTask) {}
  override fun requireTopDownEnd(task: UTask, output: Out) {}
  override fun requireBottomUpInitialStart(task: UTask) {}
  override fun requireBottomUpInitialEnd(task: UTask, output: Out) {}
  override fun requireBottomUpStart(task: UTask) {}
  override fun requireBottomUpEnd(task: UTask, output: Out) {}
  override fun checkVisitedStart(task: UTask) {}
  override fun checkVisitedEnd(task: UTask, output: Out) {}
  override fun checkCachedStart(task: UTask) {}
  override fun checkCachedEnd(task: UTask, output: Out) {}
  override fun checkStoredStart(task: UTask) {}
  override fun checkStoredEnd(task: UTask, output: Out) {}
  override fun checkFileGenStart(task: UTask, fileGen: FileGen) {}
  override fun checkFileGenEnd(task: UTask, fileGen: FileGen, reason: InconsistentFileGen?) {}
  override fun checkFileReqStart(task: UTask, fileReq: FileReq) {}
  override fun checkFileReqEnd(task: UTask, fileReq: FileReq, reason: InconsistentFileReq?) {}
  override fun checkTaskReqStart(task: UTask, taskReq: TaskReq) {}
  override fun checkTaskReqEnd(task: UTask, taskReq: TaskReq, reason: InconsistentTaskReq?) {}
  override fun executeStart(task: UTask, reason: ExecReason) {}
  override fun executeEnd(task: UTask, reason: ExecReason, data: UTaskData) {}
  override fun invokeObserverStart(observer: Function<Unit>, task: UTask, output: Out) {}
  override fun invokeObserverEnd(observer: Function<Unit>, task: UTask, output: Out) {}
  override fun error(message: String) {}
  override fun warn(message: String) {}
  override fun info(message: String) {}
  override fun trace(message: String) {}
}
