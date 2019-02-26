package mb.pie.runtime.logger.exec

import mb.pie.api.*
import mb.pie.api.exec.ExecReason
import java.io.Serializable
import java.util.function.Consumer

class NoopExecutorLogger : ExecutorLogger {
  override fun requireTopDownInitialStart(key: TaskKey, task: Task<*, *>) {}
  override fun requireTopDownInitialEnd(key: TaskKey, task: Task<*, *>, output: Out) {}
  override fun requireTopDownStart(key: TaskKey, task: Task<*, *>) {}
  override fun requireTopDownEnd(key: TaskKey, task: Task<*, *>, output: Out) {}
  override fun requireBottomUpInitialStart(changedResources: Set<ResourceKey>) {}
  override fun requireBottomUpInitialEnd() {}
  override fun checkVisitedStart(key: TaskKey) {}
  override fun checkVisitedEnd(key: TaskKey, output: Out) {}
  override fun checkStoredStart(key: TaskKey) {}
  override fun checkStoredEnd(key: TaskKey, output: Out) {}
  override fun checkResourceProvideStart(key: TaskKey, task: Task<*, *>, dep: ResourceProvideDep) {}
  override fun checkResourceProvideEnd(key: TaskKey, task: Task<*, *>, dep: ResourceProvideDep, reason: ExecReason?) {}
  override fun checkResourceRequireStart(key: TaskKey, task: Task<*, *>, dep: ResourceRequireDep) {}
  override fun checkResourceRequireEnd(key: TaskKey, task: Task<*, *>, dep: ResourceRequireDep, reason: ExecReason?) {}
  override fun checkTaskRequireStart(key: TaskKey, task: Task<*, *>, dep: TaskRequireDep) {}
  override fun checkTaskRequireEnd(key: TaskKey, task: Task<*, *>, dep: TaskRequireDep, reason: ExecReason?) {}
  override fun executeStart(key: TaskKey, task: Task<*, *>, reason: ExecReason) {}
  override fun executeEnd(key: TaskKey, task: Task<*, *>, reason: ExecReason, data: TaskData<*, *>) {}
  override fun invokeObserverStart(observer: Consumer<Serializable?>, key: TaskKey, output: Out) {}
  override fun invokeObserverEnd(observer: Consumer<Serializable?>, key: TaskKey, output: Out) {}
}
