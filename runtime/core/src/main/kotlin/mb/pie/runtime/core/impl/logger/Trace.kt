package mb.pie.runtime.core.impl.logger

import mb.pie.runtime.core.*
import mb.pie.runtime.core.impl.*
import java.lang.management.ManagementFactory

class TraceLogger : Logger {
  private val mxBean = ManagementFactory.getThreadMXBean()
  private inline val currentTime get() = mxBean.currentThreadCpuTime

  val traces = mutableListOf<Trace>()


  override fun requireTopDownInitialStart(app: UFuncApp) {
    traces.add(RequireTopDownInitialStart(app, currentTime))
  }

  override fun requireTopDownInitialEnd(app: UFuncApp, result: UExecRes) {
    traces.add(RequireTopDownInitialEnd(app, result, currentTime))
  }


  override fun requireTopDownStart(app: UFuncApp) {
    traces.add(RequireTopDownStart(app, currentTime))
  }

  override fun requireTopDownEnd(app: UFuncApp, result: UExecRes) {
    traces.add(RequireTopDownEnd(app, result, currentTime))
  }


  override fun requireBottomUpInitialStart(app: UFuncApp) {
    traces.add(RequireBottomUpInitialStart(app, currentTime))
  }

  override fun requireBottomUpInitialEnd(app: UFuncApp, result: UExecRes?) {
    traces.add(RequireBottomUpInitialEnd(app, result, currentTime))
  }

  override fun requireBottomUpStart(app: UFuncApp) {
    traces.add(RequireBottomUpStart(app, currentTime))
  }

  override fun requireBottomUpEnd(app: UFuncApp, result: UExecRes?) {
    traces.add(RequireBottomUpEnd(app, result, currentTime))
  }


  override fun checkVisitedStart(app: UFuncApp) {
    traces.add(CheckVisitedStart(app, currentTime))
  }

  override fun checkVisitedEnd(app: UFuncApp, output: Out?) {
    traces.add(CheckVisitedEnd(app, output, currentTime))
  }


  override fun checkCachedStart(app: UFuncApp) {
    traces.add(CheckCachedStart(app, currentTime))

  }

  override fun checkCachedEnd(app: UFuncApp, output: Out?) {
    traces.add(CheckCachedEnd(app, output, currentTime))
  }


  override fun checkStoredStart(app: UFuncApp) {
    traces.add(CheckStoredStart(app, currentTime))

  }

  override fun checkStoredEnd(app: UFuncApp, output: Out?) {
    traces.add(CheckStoredEnd(app, output, currentTime))
  }


  override fun checkPathGenStart(app: UFuncApp, pathGen: PathGen) {
    traces.add(CheckGenStart(app, pathGen, currentTime))
  }

  override fun checkPathGenEnd(app: UFuncApp, pathGen: PathGen, reason: InconsistentPathGen?) {
    traces.add(CheckGenEnd(app, pathGen, reason, currentTime))
  }


  override fun checkPathReqStart(app: UFuncApp, req: PathReq) {
    traces.add(CheckPathReqStart(app, req, currentTime))
  }

  override fun checkPathReqEnd(app: UFuncApp, req: PathReq, reason: InconsistentPathReq?) {
    traces.add(CheckPathReqEnd(app, req, reason, currentTime))
  }


  override fun checkCallReqStart(app: UFuncApp, req: CallReq) {
    traces.add(CheckBuildReqStart(app, req, currentTime))
  }

  override fun checkCallReqEnd(app: UFuncApp, req: CallReq, reason: InconsistentCallReq?) {
    traces.add(CheckBuildReqEnd(app, req, reason, currentTime))
  }


  override fun executeStart(app: UFuncApp, reason: ExecReason) {
    traces.add(RebuildStart(app, reason, currentTime))
  }

  override fun executeEnd(app: UFuncApp, reason: ExecReason, result: UExecRes) {
    traces.add(RebuildEnd(app, reason, result, currentTime))
  }


  override fun invokeObserverStart(observer: Function<Unit>, app: UFuncApp, output: Out) {
    traces.add(InvokeObserverStart(observer, app, output, currentTime))
  }

  override fun invokeObserverEnd(observer: Function<Unit>, app: UFuncApp, output: Out) {
    traces.add(InvokeObserverEnd(observer, app, output, currentTime))
  }


  override fun error(message: String) {}
  override fun warn(message: String) {}
  override fun info(message: String) {}
  override fun trace(message: String) {}
}

interface Trace {
  val time: Long
}

data class RequireTopDownInitialStart(val app: UFuncApp, override val time: Long) : Trace
data class RequireTopDownInitialEnd(val app: UFuncApp, val result: UExecRes, override val time: Long) : Trace
data class RequireTopDownStart(val app: UFuncApp, override val time: Long) : Trace
data class RequireTopDownEnd(val app: UFuncApp, val result: UExecRes, override val time: Long) : Trace
data class RequireBottomUpInitialStart(val app: UFuncApp, override val time: Long) : Trace
data class RequireBottomUpInitialEnd(val app: UFuncApp, val result: UExecRes?, override val time: Long) : Trace
data class RequireBottomUpStart(val app: UFuncApp, override val time: Long) : Trace
data class RequireBottomUpEnd(val app: UFuncApp, val result: UExecRes?, override val time: Long) : Trace
data class CheckVisitedStart(val app: UFuncApp, override val time: Long) : Trace
data class CheckVisitedEnd(val app: UFuncApp, val output: Out?, override val time: Long) : Trace
data class CheckCachedStart(val app: UFuncApp, override val time: Long) : Trace
data class CheckCachedEnd(val app: UFuncApp, val output: Out?, override val time: Long) : Trace
data class CheckStoredStart(val app: UFuncApp, override val time: Long) : Trace
data class CheckStoredEnd(val app: UFuncApp, val output: Out?, override val time: Long) : Trace
data class CheckGenStart(val app: UFuncApp, val pathGen: PathGen, override val time: Long) : Trace
data class CheckGenEnd(val app: UFuncApp, val pathGen: PathGen, val reason: InconsistentPathGen?, override val time: Long) : Trace
data class CheckPathReqStart(val app: UFuncApp, val eq: PathReq, override val time: Long) : Trace
data class CheckPathReqEnd(val app: UFuncApp, val eq: PathReq, val reason: InconsistentPathReq?, override val time: Long) : Trace
data class CheckBuildReqStart(val app: UFuncApp, val req: CallReq, override val time: Long) : Trace
data class CheckBuildReqEnd(val app: UFuncApp, val req: CallReq, val reason: ExecReason?, override val time: Long) : Trace
data class RebuildStart(val app: UFuncApp, val reason: ExecReason, override val time: Long) : Trace
data class RebuildEnd(val app: UFuncApp, val reason: ExecReason, val result: UExecRes, override val time: Long) : Trace
data class InvokeObserverStart(val observer: Function<Unit>, val app: UFuncApp, val output: Out, override val time: Long) : Trace
data class InvokeObserverEnd(val observer: Function<Unit>, val app: UFuncApp, val output: Out, override val time: Long) : Trace