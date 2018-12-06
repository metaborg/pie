package mb.pie.runtime.logger.exec

import mb.pie.api.*
import mb.pie.api.exec.ExecReason
import java.util.concurrent.atomic.AtomicInteger

class LoggerExecutorLogger @JvmOverloads constructor(
  private val logger: Logger,
  private val descLimit: Int = 200
) : ExecutorLogger {
  private var indentation = AtomicInteger(0)
  private val indent get() = " ".repeat(indentation.get())

  override fun requireTopDownInitialStart(key: TaskKey, task: Task<*, *>) {}
  override fun requireTopDownInitialEnd(key: TaskKey, task: Task<*, *>, output: Out) {}

  override fun requireTopDownStart(key: TaskKey, task: Task<*, *>) {
    logger.trace("${indent}v ${task.desc(descLimit)}")
    indentation.incrementAndGet()
  }

  override fun requireTopDownEnd(key: TaskKey, task: Task<*, *>, output: Out) {
    indentation.decrementAndGet()
    logger.trace("$indent✔ ${task.desc(descLimit)} -> ${output.toString().toShortString(descLimit)}")
  }


  override fun requireBottomUpInitialStart(changedResources: Set<ResourceKey>) {}
  override fun requireBottomUpInitialEnd() {}


  override fun checkVisitedStart(key: TaskKey) {}
  override fun checkVisitedEnd(key: TaskKey, output: Out) {}

  override fun checkStoredStart(key: TaskKey) {}
  override fun checkStoredEnd(key: TaskKey, output: Out) {}

  override fun checkResourceProvideStart(key: TaskKey, task: Task<*, *>, dep: ResourceProvideDep) {}
  override fun checkResourceProvideEnd(key: TaskKey, task: Task<*, *>, dep: ResourceProvideDep, reason: ExecReason?) {
    if(reason != null) {
      if(reason is InconsistentResourceProvide) {
        logger.trace("$indent␦ ${dep.key} (inconsistent: ${dep.stamp} vs ${reason.newStamp})")
      } else {
        logger.trace("$indent␦ ${dep.key} (inconsistent)")
      }
    } else {
      logger.trace("$indent␦ ${dep.key} (consistent: ${dep.stamp})")
    }
  }

  override fun checkResourceRequireStart(key: TaskKey, task: Task<*, *>, dep: ResourceRequireDep) {}
  override fun checkResourceRequireEnd(key: TaskKey, task: Task<*, *>, dep: ResourceRequireDep, reason: ExecReason?) {
    if(reason != null) {
      if(reason is InconsistentResourceProvide) {
        logger.trace("$indent␦ ${dep.key} (inconsistent: ${dep.stamp} vs ${reason.newStamp})")
      } else {
        logger.trace("$indent␦ ${dep.key} (inconsistent)")
      }
    } else {
      logger.trace("$indent␦ ${dep.key} (consistent: ${dep.stamp})")
    }
  }

  override fun checkTaskRequireStart(key: TaskKey, task: Task<*, *>, dep: TaskRequireDep) {}
  override fun checkTaskRequireEnd(key: TaskKey, task: Task<*, *>, dep: TaskRequireDep, reason: ExecReason?) {
    when(reason) {
      is InconsistentTaskReq ->
        logger.trace("$indent␦ ${dep.callee.toShortString(descLimit)} (inconsistent: ${dep.stamp} vs ${reason.newStamp})")
      null ->
        logger.trace("$indent␦ ${dep.callee.toShortString(descLimit)} (consistent: ${dep.stamp})")
    }
  }


  override fun executeStart(key: TaskKey, task: Task<*, *>, reason: ExecReason) {
    logger.info("$indent> ${task.desc(descLimit)} (reason: $reason)")
  }

  override fun executeEnd(key: TaskKey, task: Task<*, *>, reason: ExecReason, data: TaskData<*, *>) {
    logger.info("$indent< ${data.output.toString().toShortString(descLimit)}")
  }


  override fun invokeObserverStart(observer: Function<Unit>, key: TaskKey, output: Out) {
    logger.trace("$indent@ ${observer.toString().toShortString(descLimit)}(${output.toString().toShortString(descLimit)})")
  }

  override fun invokeObserverEnd(observer: Function<Unit>, key: TaskKey, output: Out) {}
}