package mb.pie.runtime.core.impl.logger

import mb.pie.runtime.core.*
import mb.pie.runtime.core.impl.*

class NoopLogger : Logger {
  override fun requireInitialStart(app: UFuncApp) {}
  override fun requireInitialEnd(app: UFuncApp, info: UExecInfo) {}
  override fun requireStart(app: UFuncApp) {}
  override fun requireEnd(app: UFuncApp, info: UExecInfo) {}
  override fun checkConsistentStart(app: UFuncApp) {}
  override fun checkConsistentEnd(app: UFuncApp, result: UExecRes?) {}
  override fun checkCachedStart(app: UFuncApp) {}
  override fun checkCachedEnd(app: UFuncApp, result: UExecRes?) {}
  override fun checkStoredStart(app: UFuncApp) {}
  override fun checkStoredEnd(app: UFuncApp, result: UExecRes?) {}
  override fun checkGenStart(app: UFuncApp, gen: Gen) {}
  override fun checkGenEnd(app: UFuncApp, gen: Gen, reason: InconsistentGenPath?) {}
  override fun checkPathReqStart(app: UFuncApp, req: PathReq) {}
  override fun checkPathReqEnd(app: UFuncApp, req: PathReq, reason: InconsistentPathReq?) {}
  override fun checkBuildReqStart(app: UFuncApp, req: UCallReq) {}
  override fun checkBuildReqEnd(app: UFuncApp, req: UCallReq, reason: ExecReason?) {}
  override fun rebuildStart(app: UFuncApp, reason: ExecReason) {}
  override fun rebuildEnd(app: UFuncApp, reason: ExecReason, result: UExecRes) {}
}