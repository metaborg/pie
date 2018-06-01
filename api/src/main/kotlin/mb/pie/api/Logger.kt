package mb.pie.api

import mb.pie.api.exec.ExecReason
import mb.pie.vfs.path.PPath

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
  fun requireTopDownInitialStart(key: TaskKey, task: Task<*, *>)
  fun requireTopDownInitialEnd(key: TaskKey, task: Task<*, *>, output: Out)
  fun requireTopDownStart(key: TaskKey, task: Task<*, *>)
  fun requireTopDownEnd(key: TaskKey, task: Task<*, *>, output: Out)
  fun requireBottomUpInitialStart(changedFiles: Set<PPath>)
  fun requireBottomUpInitialEnd()
//  fun requireBottomUpStart(key: TaskKey)
//  fun requireBottomUpEnd(key: TaskKey, output: Out)
  fun checkVisitedStart(key: TaskKey)
  fun checkVisitedEnd(key: TaskKey, output: Out)
//  fun checkCachedStart(key: TaskKey)
//  fun checkCachedEnd(key: TaskKey, output: Out)
  fun checkStoredStart(key: TaskKey)
  fun checkStoredEnd(key: TaskKey, output: Out)
  fun checkFileGenStart(key: TaskKey, task: Task<*, *>, fileGen: FileGen)
  fun checkFileGenEnd(key: TaskKey, task: Task<*, *>, fileGen: FileGen, reason: ExecReason?)
  fun checkFileReqStart(key: TaskKey, task: Task<*, *>, fileReq: FileReq)
  fun checkFileReqEnd(key: TaskKey, task: Task<*, *>, fileReq: FileReq, reason: ExecReason?)
  fun checkTaskReqStart(key: TaskKey, task: Task<*, *>, taskReq: TaskReq)
  fun checkTaskReqEnd(key: TaskKey, task: Task<*, *>, taskReq: TaskReq, reason: ExecReason?)
  fun executeStart(key: TaskKey, task: Task<*, *>, reason: ExecReason)
  fun executeEnd(key: TaskKey, task: Task<*, *>, reason: ExecReason, data: TaskData<*, *>)
  fun invokeObserverStart(observer: Function<Unit>, key: TaskKey, output: Out)
  fun invokeObserverEnd(observer: Function<Unit>, key: TaskKey, output: Out)
}
