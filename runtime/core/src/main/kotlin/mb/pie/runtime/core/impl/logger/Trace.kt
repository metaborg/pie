package mb.pie.runtime.core.impl.logger

import mb.pie.runtime.core.*
import mb.pie.runtime.core.impl.*
import java.lang.management.ManagementFactory

class TraceLogger : Logger {
  private val mxBean = ManagementFactory.getThreadMXBean()
  private inline val currentTime get() = mxBean.currentThreadCpuTime

  val traces = mutableListOf<Trace>()


  override fun requireInitialStart(app: UFuncApp) {
    traces.add(RequireInitialStart(app, currentTime))
  }

  override fun requireInitialEnd(app: UFuncApp, info: UExecInfo) {
    traces.add(RequireInitialEnd(app, info, currentTime))
  }


  override fun requireStart(app: UFuncApp) {
    traces.add(RequireStart(app, currentTime))
  }

  override fun requireEnd(app: UFuncApp, info: UExecInfo) {
    traces.add(RequireEnd(app, info, currentTime))
  }


  override fun checkConsistentStart(app: UFuncApp) {
    traces.add(CheckConsistentStart(app, currentTime))
  }

  override fun checkConsistentEnd(app: UFuncApp, result: UExecRes?) {
    traces.add(CheckConsistentEnd(app, result, currentTime))
  }


  override fun checkCachedStart(app: UFuncApp) {
    traces.add(CheckCachedStart(app, currentTime))

  }

  override fun checkCachedEnd(app: UFuncApp, result: UExecRes?) {
    traces.add(CheckCachedEnd(app, result, currentTime))
  }


  override fun checkStoredStart(app: UFuncApp) {
    traces.add(CheckStoredStart(app, currentTime))

  }

  override fun checkStoredEnd(app: UFuncApp, result: UExecRes?) {
    traces.add(CheckStoredEnd(app, result, currentTime))
  }


  override fun checkGenStart(app: UFuncApp, gen: Gen) {
    traces.add(CheckGenStart(app, gen, currentTime))
  }

  override fun checkGenEnd(app: UFuncApp, gen: Gen, reason: InconsistentGenPath?) {
    traces.add(CheckGenEnd(app, gen, reason, currentTime))
  }


  override fun checkPathReqStart(app: UFuncApp, req: PathReq) {
    traces.add(CheckPathReqStart(app, req, currentTime))
  }

  override fun checkPathReqEnd(app: UFuncApp, req: PathReq, reason: InconsistentPathReq?) {
    traces.add(CheckPathReqEnd(app, req, reason, currentTime))
  }


  override fun checkBuildReqStart(app: UFuncApp, req: UExecReq) {
    traces.add(CheckBuildReqStart(app, req, currentTime))
  }

  override fun checkBuildReqEnd(app: UFuncApp, req: UExecReq, reason: ExecReason?) {
    traces.add(CheckBuildReqEnd(app, req, reason, currentTime))
  }


  override fun rebuildStart(app: UFuncApp, reason: ExecReason) {
    traces.add(RebuildStart(app, reason, currentTime))
  }

  override fun rebuildEnd(app: UFuncApp, reason: ExecReason, result: UExecRes) {
    traces.add(RebuildEnd(app, reason, result, currentTime))
  }
}

interface Trace {
  val time: Long
}

data class RequireInitialStart(val app: UFuncApp, override val time: Long) : Trace
data class RequireInitialEnd(val app: UFuncApp, val info: UExecInfo, override val time: Long) : Trace
data class RequireStart(val app: UFuncApp, override val time: Long) : Trace
data class RequireEnd(val app: UFuncApp, val info: UExecInfo, override val time: Long) : Trace
data class CheckConsistentStart(val app: UFuncApp, override val time: Long) : Trace
data class CheckConsistentEnd(val app: UFuncApp, val result: UExecRes?, override val time: Long) : Trace
data class CheckCachedStart(val app: UFuncApp, override val time: Long) : Trace
data class CheckCachedEnd(val app: UFuncApp, val result: UExecRes?, override val time: Long) : Trace
data class CheckStoredStart(val app: UFuncApp, override val time: Long) : Trace
data class CheckStoredEnd(val app: UFuncApp, val result: UExecRes?, override val time: Long) : Trace
data class CheckGenStart(val app: UFuncApp, val gen: Gen, override val time: Long) : Trace
data class CheckGenEnd(val app: UFuncApp, val gen: Gen, val reason: InconsistentGenPath?, override val time: Long) : Trace
data class CheckPathReqStart(val app: UFuncApp, val eq: PathReq, override val time: Long) : Trace
data class CheckPathReqEnd(val app: UFuncApp, val eq: PathReq, val reason: InconsistentPathReq?, override val time: Long) : Trace
data class CheckBuildReqStart(val app: UFuncApp, val req: UExecReq, override val time: Long) : Trace
data class CheckBuildReqEnd(val app: UFuncApp, val req: UExecReq, val reason: ExecReason?, override val time: Long) : Trace
data class RebuildStart(val app: UFuncApp, val reason: ExecReason, override val time: Long) : Trace
data class RebuildEnd(val app: UFuncApp, val reason: ExecReason, val result: UExecRes, override val time: Long) : Trace