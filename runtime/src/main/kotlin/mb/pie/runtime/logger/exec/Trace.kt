package mb.pie.runtime.logger.exec

import mb.pie.api.*
import mb.pie.api.exec.ExecReason
import mb.vfs.path.PPath
import java.lang.management.ManagementFactory

class TraceExecutorLogger : ExecutorLogger {
  private val mxBean = ManagementFactory.getThreadMXBean()
  private inline val currentTime get() = mxBean.currentThreadCpuTime

  val traces = mutableListOf<Trace>()


  override fun requireTopDownInitialStart(task: UTask) {
    traces.add(RequireTopDownInitialStart(task, currentTime))
  }

  override fun requireTopDownInitialEnd(task: UTask, output: Out) {
    traces.add(RequireTopDownInitialEnd(task, output, currentTime))
  }


  override fun requireTopDownStart(task: UTask) {
    traces.add(RequireTopDownStart(task, currentTime))
  }

  override fun requireTopDownEnd(task: UTask, output: Out) {
    traces.add(RequireTopDownEnd(task, output, currentTime))
  }


  override fun requireBottomUpInitialStart(changedFiles: Set<PPath>) {
    traces.add(RequireBottomUpInitialStart(changedFiles, currentTime))
  }

  override fun requireBottomUpInitialEnd() {
    traces.add(RequireBottomUpInitialEnd(currentTime))
  }

  override fun requireBottomUpStart(task: UTask) {
    traces.add(RequireBottomUpStart(task, currentTime))
  }

  override fun requireBottomUpEnd(task: UTask, output: Out) {
    traces.add(RequireBottomUpEnd(task, output, currentTime))
  }


  override fun checkVisitedStart(task: UTask) {
    traces.add(CheckVisitedStart(task, currentTime))
  }

  override fun checkVisitedEnd(task: UTask, output: Out) {
    traces.add(CheckVisitedEnd(task, output, currentTime))
  }


  override fun checkCachedStart(task: UTask) {
    traces.add(CheckCachedStart(task, currentTime))

  }

  override fun checkCachedEnd(task: UTask, output: Out) {
    traces.add(CheckCachedEnd(task, output, currentTime))
  }


  override fun checkStoredStart(task: UTask) {
    traces.add(CheckStoredStart(task, currentTime))

  }

  override fun checkStoredEnd(task: UTask, output: Out) {
    traces.add(CheckStoredEnd(task, output, currentTime))
  }


  override fun checkFileGenStart(task: UTask, fileGen: FileGen) {
    traces.add(CheckGenStart(task, fileGen, currentTime))
  }

  override fun checkFileGenEnd(task: UTask, fileGen: FileGen, reason: ExecReason?) {
    traces.add(CheckGenEnd(task, fileGen, reason, currentTime))
  }


  override fun checkFileReqStart(task: UTask, fileReq: FileReq) {
    traces.add(CheckPathReqStart(task, fileReq, currentTime))
  }

  override fun checkFileReqEnd(task: UTask, fileReq: FileReq, reason: ExecReason?) {
    traces.add(CheckPathReqEnd(task, fileReq, reason, currentTime))
  }


  override fun checkTaskReqStart(task: UTask, taskReq: TaskReq) {
    traces.add(CheckBuildReqStart(task, taskReq, currentTime))
  }

  override fun checkTaskReqEnd(task: UTask, taskReq: TaskReq, reason: ExecReason?) {
    traces.add(CheckBuildReqEnd(task, taskReq, reason, currentTime))
  }


  override fun executeStart(task: UTask, reason: ExecReason) {
    traces.add(RebuildStart(task, reason, currentTime))
  }

  override fun executeEnd(task: UTask, reason: ExecReason, data: UTaskData) {
    traces.add(RebuildEnd(task, reason, data, currentTime))
  }


  override fun invokeObserverStart(observer: Function<Unit>, task: UTask, output: Out) {
    traces.add(InvokeObserverStart(observer, task, output, currentTime))
  }

  override fun invokeObserverEnd(observer: Function<Unit>, task: UTask, output: Out) {
    traces.add(InvokeObserverEnd(observer, task, output, currentTime))
  }
}

interface Trace {
  val time: Long
}

data class RequireTopDownInitialStart(val app: UTask, override val time: Long) : Trace
data class RequireTopDownInitialEnd(val app: UTask, val output: Out, override val time: Long) : Trace
data class RequireTopDownStart(val app: UTask, override val time: Long) : Trace
data class RequireTopDownEnd(val app: UTask, val output: Out, override val time: Long) : Trace
data class RequireBottomUpInitialStart(val changedFiles: Set<PPath>, override val time: Long) : Trace
data class RequireBottomUpInitialEnd(override val time: Long) : Trace
data class RequireBottomUpStart(val app: UTask, override val time: Long) : Trace
data class RequireBottomUpEnd(val app: UTask, val output: Out, override val time: Long) : Trace
data class CheckVisitedStart(val app: UTask, override val time: Long) : Trace
data class CheckVisitedEnd(val app: UTask, val output: Out, override val time: Long) : Trace
data class CheckCachedStart(val app: UTask, override val time: Long) : Trace
data class CheckCachedEnd(val app: UTask, val output: Out, override val time: Long) : Trace
data class CheckStoredStart(val app: UTask, override val time: Long) : Trace
data class CheckStoredEnd(val app: UTask, val output: Out, override val time: Long) : Trace
data class CheckGenStart(val app: UTask, val fileGen: FileGen, override val time: Long) : Trace
data class CheckGenEnd(val app: UTask, val fileGen: FileGen, val reason: ExecReason?, override val time: Long) : Trace
data class CheckPathReqStart(val app: UTask, val eq: FileReq, override val time: Long) : Trace
data class CheckPathReqEnd(val app: UTask, val eq: FileReq, val reason: ExecReason?, override val time: Long) : Trace
data class CheckBuildReqStart(val app: UTask, val req: TaskReq, override val time: Long) : Trace
data class CheckBuildReqEnd(val app: UTask, val req: TaskReq, val reason: ExecReason?, override val time: Long) : Trace
data class RebuildStart(val app: UTask, val reason: ExecReason, override val time: Long) : Trace
data class RebuildEnd(val app: UTask, val reason: ExecReason, val data: UTaskData, override val time: Long) : Trace
data class InvokeObserverStart(val observer: Function<Unit>, val app: UTask, val output: Out, override val time: Long) : Trace
data class InvokeObserverEnd(val observer: Function<Unit>, val app: UTask, val output: Out, override val time: Long) : Trace
