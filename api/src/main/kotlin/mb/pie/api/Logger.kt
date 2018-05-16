package mb.pie.api

import mb.pie.api.exec.ExecReason
import mb.vfs.path.PPath

/**
 * Logger interface.
 */
interface Logger {
  fun error(message: String, throwable: Throwable?)
  fun warn(message: String, throwable: Throwable?)
  fun info(message: String)
  fun debug(message: String)
  fun trace(message: String)
}

/**
 * Logger interface for task executors.
 */
interface ExecutorLogger {
  fun requireTopDownInitialStart(task: UTask)
  fun requireTopDownInitialEnd(task: UTask, output: Out)
  fun requireTopDownStart(task: UTask)
  fun requireTopDownEnd(task: UTask, output: Out)
  fun requireBottomUpInitialStart(changedFiles: Set<PPath>)
  fun requireBottomUpInitialEnd()
  fun requireBottomUpStart(task: UTask)
  fun requireBottomUpEnd(task: UTask, output: Out)
  fun checkVisitedStart(task: UTask)
  fun checkVisitedEnd(task: UTask, output: Out)
  fun checkCachedStart(task: UTask)
  fun checkCachedEnd(task: UTask, output: Out)
  fun checkStoredStart(task: UTask)
  fun checkStoredEnd(task: UTask, output: Out)
  fun checkFileGenStart(task: UTask, fileGen: FileGen)
  fun checkFileGenEnd(task: UTask, fileGen: FileGen, reason: ExecReason?)
  fun checkFileReqStart(task: UTask, fileReq: FileReq)
  fun checkFileReqEnd(task: UTask, fileReq: FileReq, reason: ExecReason?)
  fun checkTaskReqStart(task: UTask, taskReq: TaskReq)
  fun checkTaskReqEnd(task: UTask, taskReq: TaskReq, reason: ExecReason?)
  fun executeStart(task: UTask, reason: ExecReason)
  fun executeEnd(task: UTask, reason: ExecReason, data: UTaskData)
  fun invokeObserverStart(observer: Function<Unit>, task: UTask, output: Out)
  fun invokeObserverEnd(observer: Function<Unit>, task: UTask, output: Out)
}
