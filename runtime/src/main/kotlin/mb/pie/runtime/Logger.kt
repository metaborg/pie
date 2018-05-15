package mb.pie.runtime

import mb.pie.runtime.impl.*
import mb.pie.runtime.stamp.FileStamp
import mb.pie.runtime.stamp.OutputStamp


/**
 * Internal logger.
 */
interface Logger {
  fun requireTopDownInitialStart(task: UTask)
  fun requireTopDownInitialEnd(task: UTask, output: Out)

  fun requireTopDownStart(task: UTask)
  fun requireTopDownEnd(task: UTask, output: Out)

  fun requireBottomUpInitialStart(task: UTask)
  fun requireBottomUpInitialEnd(task: UTask, output: Out)

  fun requireBottomUpStart(task: UTask)
  fun requireBottomUpEnd(task: UTask, output: Out)

  fun checkVisitedStart(task: UTask)
  fun checkVisitedEnd(task: UTask, output: Out)

  fun checkCachedStart(task: UTask)
  fun checkCachedEnd(task: UTask, output: Out)

  fun checkStoredStart(task: UTask)
  fun checkStoredEnd(task: UTask, output: Out)

  fun checkFileGenStart(task: UTask, fileGen: FileGen)
  fun checkFileGenEnd(task: UTask, fileGen: FileGen, reason: InconsistentFileGen?)

  fun checkFileReqStart(task: UTask, fileReq: FileReq)
  fun checkFileReqEnd(task: UTask, fileReq: FileReq, reason: InconsistentFileReq?)

  fun checkTaskReqStart(task: UTask, taskReq: TaskReq)
  fun checkTaskReqEnd(task: UTask, taskReq: TaskReq, reason: InconsistentTaskReq?)

  fun executeStart(task: UTask, reason: ExecReason)
  fun executeEnd(task: UTask, reason: ExecReason, data: UTaskData)

  fun invokeObserverStart(observer: Function<Unit>, task: UTask, output: Out)
  fun invokeObserverEnd(observer: Function<Unit>, task: UTask, output: Out)

  fun error(message: String)
  fun warn(message: String)
  fun info(message: String)
  fun trace(message: String)
}


interface ExecReason {
  override fun toString(): String
}

class UnknownExecReason : ExecReason {
  override fun toString() = "unknown/any"


  override fun equals(other: Any?): Boolean {
    if(this === other) return true
    if(other?.javaClass != javaClass) return false
    return true
  }

  override fun hashCode(): Int {
    return 0
  }
}

class NoResultReason : ExecReason {
  override fun toString() = "no stored or cached output"


  override fun equals(other: Any?): Boolean {
    if(this === other) return true
    if(other?.javaClass != javaClass) return false
    return true
  }

  override fun hashCode(): Int {
    return 0
  }
}

data class InconsistentTransientOutput(val inconsistentOutput: OutTransient<*>) : ExecReason {
  override fun toString() = "transient output is inconsistent"
}

data class InconsistentFileGen(val fileGen: FileGen, val newStamp: FileStamp) : ExecReason {
  override fun toString() = "generated file ${fileGen.file} is inconsistent"
}

data class InconsistentFileReq(val req: FileReq, val newStamp: FileStamp) : ExecReason {
  override fun toString() = "required file ${req.file} is inconsistent"
}

data class InconsistentTaskReq(val req: TaskReq, val newStamp: OutputStamp) : ExecReason {
  override fun toString() = "required build ${req.callee.toShortString(100)} is inconsistent"
}
