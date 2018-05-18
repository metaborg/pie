package mb.pie.runtime.logger.exec

import mb.pie.api.*
import mb.pie.api.exec.ExecReason
import mb.pie.vfs.path.PPath
import java.util.concurrent.atomic.AtomicInteger

class LoggerExecutorLogger @JvmOverloads constructor(
  private val logger: Logger,
  private val descLimit: Int = 200
) : ExecutorLogger {
  private var indentation = AtomicInteger(0)
  private val indent get() = " ".repeat(indentation.get())

  override fun requireTopDownInitialStart(task: UTask) {}
  override fun requireTopDownInitialEnd(task: UTask, output: Out) {}

  override fun requireTopDownStart(task: UTask) {
    logger.trace("${indent}v ${task.toShortString(descLimit)}")
    indentation.incrementAndGet()
  }

  override fun requireTopDownEnd(task: UTask, output: Out) {
    indentation.decrementAndGet()
    logger.trace("$indent✔ ${task.toShortString(descLimit)} -> ${output.toString().toShortString(descLimit)}")
  }


  override fun requireBottomUpInitialStart(changedFiles: Set<PPath>) {}
  override fun requireBottomUpInitialEnd() {}

  override fun requireBottomUpStart(task: UTask) {
    logger.trace("$indent^ ${task.toShortString(descLimit)}")
    indentation.incrementAndGet()
  }

  override fun requireBottomUpEnd(task: UTask, output: Out) {
    indentation.decrementAndGet()
    logger.trace("$indent✔ ${task.toShortString(descLimit)} -> ${output.toString().toShortString(descLimit)}")
  }


  override fun checkVisitedStart(task: UTask) {}
  override fun checkVisitedEnd(task: UTask, output: Out) {}


  override fun checkCachedStart(task: UTask) {}
  override fun checkCachedEnd(task: UTask, output: Out) {}


  override fun checkStoredStart(task: UTask) {}
  override fun checkStoredEnd(task: UTask, output: Out) {}


  override fun checkFileGenStart(task: UTask, fileGen: FileGen) {}

  override fun checkFileGenEnd(task: UTask, fileGen: FileGen, reason: ExecReason?) {
    if(reason != null) {
      if(reason is InconsistentFileGen) {
        logger.trace("$indent␦ ${fileGen.file} (inconsistent: ${fileGen.stamp} vs ${reason.newStamp})")
      } else {
        logger.trace("$indent␦ ${fileGen.file} (inconsistent)")
      }
    } else {
      logger.trace("$indent␦ ${fileGen.file} (consistent: ${fileGen.stamp})")
    }
  }


  override fun checkFileReqStart(task: UTask, fileReq: FileReq) {}

  override fun checkFileReqEnd(task: UTask, fileReq: FileReq, reason: ExecReason?) {
    if(reason != null) {
      if(reason is InconsistentFileGen) {
        logger.trace("$indent␦ ${fileReq.file} (inconsistent: ${fileReq.stamp} vs ${reason.newStamp})")
      } else {
        logger.trace("$indent␦ ${fileReq.file} (inconsistent)")
      }
    } else {
      logger.trace("$indent␦ ${fileReq.file} (consistent: ${fileReq.stamp})")
    }
  }


  override fun checkTaskReqStart(task: UTask, taskReq: TaskReq) {}

  override fun checkTaskReqEnd(task: UTask, taskReq: TaskReq, reason: ExecReason?) {
    when(reason) {
      is InconsistentTaskReq ->
        logger.trace("$indent␦ ${taskReq.callee.toShortString(descLimit)} (inconsistent: ${taskReq.stamp} vs ${reason.newStamp})")
      null ->
        logger.trace("$indent␦ ${taskReq.callee.toShortString(descLimit)} (consistent: ${taskReq.stamp})")
    }
  }


  override fun executeStart(task: UTask, reason: ExecReason) {
    logger.info("$indent> ${task.toShortString(descLimit)} (reason: $reason)")
  }

  override fun executeEnd(task: UTask, reason: ExecReason, data: UTaskData) {
    logger.info("$indent< ${data.output.toString().toShortString(descLimit)}")
  }


  override fun invokeObserverStart(observer: Function<Unit>, task: UTask, output: Out) {
    logger.trace("$indent@ ${observer.toString().toShortString(50)}(${output.toString().toShortString(200)})")
  }

  override fun invokeObserverEnd(observer: Function<Unit>, task: UTask, output: Out) {}
}