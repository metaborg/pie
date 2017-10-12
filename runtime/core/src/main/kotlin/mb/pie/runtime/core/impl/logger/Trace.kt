package mb.pie.runtime.core.impl.logger

import mb.pie.runtime.core.*
import mb.pie.runtime.core.impl.*
import java.lang.management.ManagementFactory

class TraceBuildLogger : BuildLogger {
  private val mxBean = ManagementFactory.getThreadMXBean()
  private inline val currentTime get() = mxBean.currentThreadCpuTime

  val traces = mutableListOf<Trace>()


  override fun requireStart(app: UBuildApp) {
    traces.add(RequireStart(app, currentTime))
  }

  override fun requireEnd(app: UBuildApp, info: UBuildInfo) {
    traces.add(RequireEnd(app, info, currentTime))
  }


  override fun checkConsistentStart(app: UBuildApp) {
    traces.add(CheckConsistentStart(app, currentTime))
  }

  override fun checkConsistentEnd(app: UBuildApp, result: UBuildRes?) {
    traces.add(CheckConsistentEnd(app, result, currentTime))
  }


  override fun checkCachedStart(app: UBuildApp) {
    traces.add(CheckCachedStart(app, currentTime))

  }

  override fun checkCachedEnd(app: UBuildApp, result: UBuildRes?) {
    traces.add(CheckCachedEnd(app, result, currentTime))
  }


  override fun checkStoredStart(app: UBuildApp) {
    traces.add(CheckStoredStart(app, currentTime))

  }

  override fun checkStoredEnd(app: UBuildApp, result: UBuildRes?) {
    traces.add(CheckStoredEnd(app, result, currentTime))
  }


  override fun checkGenStart(app: UBuildApp, gen: Gen) {
    traces.add(CheckGenStart(app, gen, currentTime))
  }

  override fun checkGenEnd(app: UBuildApp, gen: Gen, reason: InconsistentGenPath?) {
    traces.add(CheckGenEnd(app, gen, reason, currentTime))
  }


  override fun checkPathReqStart(app: UBuildApp, req: PathReq) {
    traces.add(CheckPathReqStart(app, req, currentTime))
  }

  override fun checkPathReqEnd(app: UBuildApp, req: PathReq, reason: InconsistentPathReq?) {
    traces.add(CheckPathReqEnd(app, req, reason, currentTime))
  }


  override fun checkBuildReqStart(app: UBuildApp, req: UBuildReq) {
    traces.add(CheckBuildReqStart(app, req, currentTime))
  }

  override fun checkBuildReqEnd(app: UBuildApp, req: UBuildReq, reason: BuildReason?) {
    traces.add(CheckBuildReqEnd(app, req, reason, currentTime))
  }


  override fun rebuildStart(app: UBuildApp, reason: BuildReason) {
    traces.add(RebuildStart(app, reason, currentTime))
  }

  override fun rebuildEnd(app: UBuildApp, reason: BuildReason, result: UBuildRes) {
    traces.add(RebuildEnd(app, reason, result, currentTime))
  }
}

interface Trace
data class RequireStart(val app: UBuildApp, val time: Long) : Trace
data class RequireEnd(val app: UBuildApp, val info: UBuildInfo, val time: Long) : Trace
data class CheckConsistentStart(val app: UBuildApp, val time: Long) : Trace
data class CheckConsistentEnd(val app: UBuildApp, val result: UBuildRes?, val time: Long) : Trace
data class CheckCachedStart(val app: UBuildApp, val time: Long) : Trace
data class CheckCachedEnd(val app: UBuildApp, val result: UBuildRes?, val time: Long) : Trace
data class CheckStoredStart(val app: UBuildApp, val time: Long) : Trace
data class CheckStoredEnd(val app: UBuildApp, val result: UBuildRes?, val time: Long) : Trace
data class CheckGenStart(val app: UBuildApp, val gen: Gen, val time: Long) : Trace
data class CheckGenEnd(val app: UBuildApp, val gen: Gen, val reason: InconsistentGenPath?, val time: Long) : Trace
data class CheckPathReqStart(val app: UBuildApp, val eq: PathReq, val time: Long) : Trace
data class CheckPathReqEnd(val app: UBuildApp, val eq: PathReq, val reason: InconsistentPathReq?, val time: Long) : Trace
data class CheckBuildReqStart(val app: UBuildApp, val req: UBuildReq, val time: Long) : Trace
data class CheckBuildReqEnd(val app: UBuildApp, val req: UBuildReq, val reason: BuildReason?, val time: Long) : Trace
data class RebuildStart(val app: UBuildApp, val reason: BuildReason, val time: Long) : Trace
data class RebuildEnd(val app: UBuildApp, val reason: BuildReason, val result: UBuildRes, val time: Long) : Trace