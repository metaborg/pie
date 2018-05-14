package mb.pie.runtime.core

import mb.pie.runtime.core.impl.*
import mb.pie.runtime.core.stamp.OutputStamp
import mb.pie.runtime.core.stamp.PathStamp


interface Logger {
  fun requireTopDownInitialStart(app: UFuncApp)
  fun requireTopDownInitialEnd(app: UFuncApp, result: UExecRes)

  fun requireTopDownStart(app: UFuncApp)
  fun requireTopDownEnd(app: UFuncApp, result: UExecRes)

  fun requireBottomUpInitialStart(app: UFuncApp)
  fun requireBottomUpInitialEnd(app: UFuncApp, result: UExecRes?)

  fun requireBottomUpStart(app: UFuncApp)
  fun requireBottomUpEnd(app: UFuncApp, result: UExecRes?)

  fun checkVisitedStart(app: UFuncApp)
  fun checkVisitedEnd(app: UFuncApp, output: Out)

  fun checkCachedStart(app: UFuncApp)
  fun checkCachedEnd(app: UFuncApp, output: Out)

  fun checkStoredStart(app: UFuncApp)
  fun checkStoredEnd(app: UFuncApp, output: Out)

  fun checkPathGenStart(app: UFuncApp, pathGen: PathGen)
  fun checkPathGenEnd(app: UFuncApp, pathGen: PathGen, reason: InconsistentPathGen?)

  fun checkPathReqStart(app: UFuncApp, req: PathReq)
  fun checkPathReqEnd(app: UFuncApp, req: PathReq, reason: InconsistentPathReq?)

  fun checkCallReqStart(app: UFuncApp, req: CallReq)
  fun checkCallReqEnd(app: UFuncApp, req: CallReq, reason: InconsistentCallReq?)

  fun executeStart(app: UFuncApp, reason: ExecReason)
  fun executeEnd(app: UFuncApp, reason: ExecReason, result: UExecRes)

  fun invokeObserverStart(observer: Function<Unit>, app: UFuncApp, output: Out)
  fun invokeObserverEnd(observer: Function<Unit>, app: UFuncApp, output: Out)

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

data class InconsistentPathGen(val pathGen: PathGen, val newStamp: PathStamp) : ExecReason {
  override fun toString() = "generated path ${pathGen.path} is inconsistent"
}

data class InconsistentPathReq(val req: PathReq, val newStamp: PathStamp) : ExecReason {
  override fun toString() = "required path ${req.path} is inconsistent"
}

data class InconsistentCallReq(val req: CallReq, val newStamp: OutputStamp) : ExecReason {
  override fun toString() = "required build ${req.callee.toShortString(100)} is inconsistent"
}
