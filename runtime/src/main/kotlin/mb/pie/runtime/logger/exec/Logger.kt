package mb.pie.runtime.logger.exec

import mb.pie.api.*
import mb.pie.api.exec.ExecReason
import java.io.Serializable
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

public class LoggerExecutorLogger : ExecutorLogger {
  private val logger: Logger;
  private val descLimit: Int;
  private var indentation: AtomicInteger = AtomicInteger(0);

  constructor(logger: Logger, descLimit: Int = 200) {
    this.logger = logger;
    this.descLimit = descLimit;
  }


  private fun getIndent(): String {
    return " ".repeat(indentation.get());
  }

  override fun requireTopDownInitialStart(key: TaskKey, task: Task<*, *>) {}
  override fun requireTopDownInitialEnd(key: TaskKey, task: Task<*, *>, output: Out) {}

  override fun requireTopDownStart(key: TaskKey, task: Task<*, *>) {
    logger.trace(getIndent() + "v " + task.desc(descLimit));
    indentation.incrementAndGet();
  }

  override fun requireTopDownEnd(key: TaskKey, task: Task<*, *>, output: Out) {
    indentation.decrementAndGet();
    logger.trace(getIndent() + "✔ " + task.desc(descLimit) + " -> " + StringUtil.toShortString(output.toString(), descLimit));
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
        logger.trace(getIndent() + "␦ " + dep.key + " (inconsistent: " + dep.stamp + " vs " + reason.newStamp + ")");
      } else {
        logger.trace(getIndent() + "␦ " + dep.key + " (inconsistent)");
      }
    } else {
      logger.trace(getIndent() + "␦ " + dep.key + " (consistent: " + dep.stamp + ")");
    }
  }

  override fun checkResourceRequireStart(key: TaskKey, task: Task<*, *>, dep: ResourceRequireDep) {}
  override fun checkResourceRequireEnd(key: TaskKey, task: Task<*, *>, dep: ResourceRequireDep, reason: ExecReason?) {
    if(reason != null) {
      if(reason is InconsistentResourceProvide) {
        logger.trace(getIndent() + "␦ " + dep.key + " (inconsistent: " + dep.stamp + " vs " + reason.newStamp + ")");
      } else {
        logger.trace(getIndent() + "␦ " + dep.key + " (inconsistent)");
      }
    } else {
      logger.trace(getIndent() + "␦ " + dep.key + " (consistent: " + dep.stamp + ")");
    }
  }

  override fun checkTaskRequireStart(key: TaskKey, task: Task<*, *>, dep: TaskRequireDep) {}
  override fun checkTaskRequireEnd(key: TaskKey, task: Task<*, *>, dep: TaskRequireDep, reason: ExecReason?) {
    if(reason is InconsistentTaskReq) {
      logger.trace(getIndent() + "␦ " + dep.callee.toShortString(descLimit) + " (inconsistent: " + dep.stamp + " vs " + reason.newStamp + ")");
    } else if(reason == null) {
      logger.trace(getIndent() + "␦ " + dep.callee.toShortString(descLimit) + " (consistent: " + dep.stamp + ")");
    }
  }


  override fun executeStart(key: TaskKey, task: Task<*, *>, reason: ExecReason) {
    logger.info(getIndent() + "> " + task.desc(descLimit) + " (reason: " + reason + ")");
  }

  override fun executeEnd(key: TaskKey, task: Task<*, *>, reason: ExecReason, data: TaskData<*, *>) {
    logger.info(getIndent() + "< " + StringUtil.toShortString(data.output.toString(), descLimit));
  }


  override fun invokeObserverStart(observer: Consumer<Serializable?>, key: TaskKey, output: Out) {
    logger.trace(getIndent() + "@ " + StringUtil.toShortString(observer.toString(), descLimit) + "(" + StringUtil.toShortString(output.toString(), descLimit) + ")");
  }

  override fun invokeObserverEnd(observer: Consumer<Serializable?>, key: TaskKey, output: Out) {}
}