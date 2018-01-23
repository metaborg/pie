package mb.pie.runtime.core

import mb.pie.runtime.core.impl.*
import mb.pie.runtime.core.stamp.OutputStamp
import mb.pie.runtime.core.stamp.PathStamp


interface Logger {
  fun requireInitialStart(app: UFuncApp)
  fun requireInitialEnd(app: UFuncApp, info: UExecInfo)

  fun requireStart(app: UFuncApp)
  fun requireEnd(app: UFuncApp, info: UExecInfo)

  fun checkConsistentStart(app: UFuncApp)
  fun checkConsistentEnd(app: UFuncApp, result: UExecRes?)

  fun checkCachedStart(app: UFuncApp)
  fun checkCachedEnd(app: UFuncApp, result: UExecRes?)

  fun checkStoredStart(app: UFuncApp)
  fun checkStoredEnd(app: UFuncApp, result: UExecRes?)

  fun checkGenStart(app: UFuncApp, gen: Gen)
  fun checkGenEnd(app: UFuncApp, gen: Gen, reason: InconsistentGenPath?)

  fun checkPathReqStart(app: UFuncApp, req: PathReq)
  fun checkPathReqEnd(app: UFuncApp, req: PathReq, reason: InconsistentPathReq?)

  fun checkBuildReqStart(app: UFuncApp, req: UCallReq)
  fun checkBuildReqEnd(app: UFuncApp, req: UCallReq, reason: ExecReason?)

  fun rebuildStart(app: UFuncApp, reason: ExecReason)
  fun rebuildEnd(app: UFuncApp, reason: ExecReason, result: UExecRes)
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
  override fun toString() = "no stored or cached result"


  override fun equals(other: Any?): Boolean {
    if(this === other) return true
    if(other?.javaClass != javaClass) return false
    return true
  }

  override fun hashCode(): Int {
    return 0
  }
}

data class InconsistentTransientOutput(val inconsistentResult: UExecRes) : ExecReason {
  override fun toString() = "transient output is inconsistent"
}

data class InconsistentGenPath(val generatingResult: UExecRes, val gen: Gen, val newStamp: PathStamp) : ExecReason {
  override fun toString() = "generated path ${gen.path} is inconsistent"
}

data class InconsistentPathReq(val requiringResult: UExecRes, val req: PathReq, val newStamp: PathStamp) : ExecReason {
  override fun toString() = "required path ${req.path} is inconsistent"
}

data class InconsistentExecReq(val requiringResult: UExecRes, val req: UCallReq, val newStamp: OutputStamp) : ExecReason {
  override fun toString() = "required build ${req.callee.toShortString(100)} is inconsistent"
}

data class InconsistentExecReqTransientOutput(val requiringResult: UExecRes, val req: UCallReq, val inconsistentResult: UExecRes) : ExecReason {
  override fun toString() = "transient output of required build ${req.callee.toShortString(100)} is inconsistent"
}
