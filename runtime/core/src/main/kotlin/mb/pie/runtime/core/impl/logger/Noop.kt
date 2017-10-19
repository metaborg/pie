package mb.pie.runtime.core.impl.logger

import mb.pie.runtime.core.*
import mb.pie.runtime.core.impl.*

class NoopBuildLogger : BuildLogger {
  override fun requireInitialStart(app: UBuildApp) {}
  override fun requireInitialEnd(app: UBuildApp, info: UBuildInfo) {}
  override fun requireStart(app: UBuildApp) {}
  override fun requireEnd(app: UBuildApp, info: UBuildInfo) {}
  override fun checkConsistentStart(app: UBuildApp) {}
  override fun checkConsistentEnd(app: UBuildApp, result: UBuildRes?) {}
  override fun checkCachedStart(app: UBuildApp) {}
  override fun checkCachedEnd(app: UBuildApp, result: UBuildRes?) {}
  override fun checkStoredStart(app: UBuildApp) {}
  override fun checkStoredEnd(app: UBuildApp, result: UBuildRes?) {}
  override fun checkGenStart(app: UBuildApp, gen: Gen) {}
  override fun checkGenEnd(app: UBuildApp, gen: Gen, reason: InconsistentGenPath?) {}
  override fun checkPathReqStart(app: UBuildApp, req: PathReq) {}
  override fun checkPathReqEnd(app: UBuildApp, req: PathReq, reason: InconsistentPathReq?) {}
  override fun checkBuildReqStart(app: UBuildApp, req: UBuildReq) {}
  override fun checkBuildReqEnd(app: UBuildApp, req: UBuildReq, reason: BuildReason?) {}
  override fun rebuildStart(app: UBuildApp, reason: BuildReason) {}
  override fun rebuildEnd(app: UBuildApp, reason: BuildReason, result: UBuildRes) {}
}