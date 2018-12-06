package mb.pie.api

import mb.pie.api.exec.ExecReason

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
  fun requireBottomUpInitialStart(changedResources: Set<ResourceKey>)
  fun requireBottomUpInitialEnd()
  fun checkVisitedStart(key: TaskKey)
  fun checkVisitedEnd(key: TaskKey, output: Out)
  fun checkStoredStart(key: TaskKey)
  fun checkStoredEnd(key: TaskKey, output: Out)
  fun checkResourceProvideStart(key: TaskKey, task: Task<*, *>, dep: ResourceProvideDep)
  fun checkResourceProvideEnd(key: TaskKey, task: Task<*, *>, dep: ResourceProvideDep, reason: ExecReason?)
  fun checkResourceRequireStart(key: TaskKey, task: Task<*, *>, dep: ResourceRequireDep)
  fun checkResourceRequireEnd(key: TaskKey, task: Task<*, *>, dep: ResourceRequireDep, reason: ExecReason?)
  fun checkTaskRequireStart(key: TaskKey, task: Task<*, *>, dep: TaskRequireDep)
  fun checkTaskRequireEnd(key: TaskKey, task: Task<*, *>, dep: TaskRequireDep, reason: ExecReason?)
  fun executeStart(key: TaskKey, task: Task<*, *>, reason: ExecReason)
  fun executeEnd(key: TaskKey, task: Task<*, *>, reason: ExecReason, data: TaskData<*, *>)
  fun invokeObserverStart(observer: Function<Unit>, key: TaskKey, output: Out)
  fun invokeObserverEnd(observer: Function<Unit>, key: TaskKey, output: Out)
}
