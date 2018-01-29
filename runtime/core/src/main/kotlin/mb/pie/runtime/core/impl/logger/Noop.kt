package mb.pie.runtime.core.impl.logger

import mb.pie.runtime.core.*
import mb.pie.runtime.core.impl.*

class NoopLogger : Logger {
  override fun requireTopDownInitialStart(app: UFuncApp) {}
  override fun requireTopDownInitialEnd(app: UFuncApp, info: UExecInfo) {}
  override fun requireTopDownStart(app: UFuncApp) {}
  override fun requireTopDownEnd(app: UFuncApp, info: UExecInfo) {}
  override fun requireBottomUpInitialStart(app: UFuncApp) {}
  override fun requireBottomUpInitialEnd(app: UFuncApp, info: UExecInfo?) {}
  override fun requireBottomUpStart(app: UFuncApp) {}
  override fun requireBottomUpEnd(app: UFuncApp, info: UExecInfo?) {}
  override fun checkVisitedStart(app: UFuncApp) {}
  override fun checkVisitedEnd(app: UFuncApp, result: UExecRes?) {}
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
  override fun invokeObserverStart(observer: Function<Unit>, app: UFuncApp, output: Out) {}
  override fun invokeObserverEnd(observer: Function<Unit>, app: UFuncApp, output: Out) {}
  override fun error(message: String) {}
  override fun warn(message: String) {}
  override fun info(message: String) {}
  override fun trace(message: String) {}
}