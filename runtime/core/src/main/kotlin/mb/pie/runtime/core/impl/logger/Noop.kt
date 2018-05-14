package mb.pie.runtime.core.impl.logger

import mb.pie.runtime.core.*
import mb.pie.runtime.core.impl.*


class NoopLogger : Logger {
  override fun requireTopDownInitialStart(app: UFuncApp) {}
  override fun requireTopDownInitialEnd(app: UFuncApp, result: UExecRes) {}
  override fun requireTopDownStart(app: UFuncApp) {}
  override fun requireTopDownEnd(app: UFuncApp, result: UExecRes) {}
  override fun requireBottomUpInitialStart(app: UFuncApp) {}
  override fun requireBottomUpInitialEnd(app: UFuncApp, result: UExecRes?) {}
  override fun requireBottomUpStart(app: UFuncApp) {}
  override fun requireBottomUpEnd(app: UFuncApp, result: UExecRes?) {}
  override fun checkVisitedStart(app: UFuncApp) {}
  override fun checkVisitedEnd(app: UFuncApp, output: Out) {}
  override fun checkCachedStart(app: UFuncApp) {}
  override fun checkCachedEnd(app: UFuncApp, output: Out) {}
  override fun checkStoredStart(app: UFuncApp) {}
  override fun checkStoredEnd(app: UFuncApp, output: Out) {}
  override fun checkPathGenStart(app: UFuncApp, pathGen: PathGen) {}
  override fun checkPathGenEnd(app: UFuncApp, pathGen: PathGen, reason: InconsistentPathGen?) {}
  override fun checkPathReqStart(app: UFuncApp, req: PathReq) {}
  override fun checkPathReqEnd(app: UFuncApp, req: PathReq, reason: InconsistentPathReq?) {}
  override fun checkCallReqStart(app: UFuncApp, req: CallReq) {}
  override fun checkCallReqEnd(app: UFuncApp, req: CallReq, reason: InconsistentCallReq?) {}
  override fun executeStart(app: UFuncApp, reason: ExecReason) {}
  override fun executeEnd(app: UFuncApp, reason: ExecReason, result: UExecRes) {}
  override fun invokeObserverStart(observer: Function<Unit>, app: UFuncApp, output: Out) {}
  override fun invokeObserverEnd(observer: Function<Unit>, app: UFuncApp, output: Out) {}
  override fun error(message: String) {}
  override fun warn(message: String) {}
  override fun info(message: String) {}
  override fun trace(message: String) {}
}
