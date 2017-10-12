package mb.pie.runtime.core

import mb.pie.runtime.core.impl.*

interface BuildLogger {
  fun requireStart(app: UBuildApp)
  fun requireEnd(app: UBuildApp, info: UBuildInfo)

  fun checkConsistentStart(app: UBuildApp)
  fun checkConsistentEnd(app: UBuildApp, result: UBuildRes?)

  fun checkCachedStart(app: UBuildApp)
  fun checkCachedEnd(app: UBuildApp, result: UBuildRes?)

  fun checkStoredStart(app: UBuildApp)
  fun checkStoredEnd(app: UBuildApp, result: UBuildRes?)

  fun checkGenStart(app: UBuildApp, gen: Gen)
  fun checkGenEnd(app: UBuildApp, gen: Gen, reason: InconsistentGenPath?)

  fun checkPathReqStart(app: UBuildApp, req: PathReq)
  fun checkPathReqEnd(app: UBuildApp, req: PathReq, reason: InconsistentPathReq?)

  fun checkBuildReqStart(app: UBuildApp, req: UBuildReq)
  fun checkBuildReqEnd(app: UBuildApp, req: UBuildReq, reason: BuildReason?)

  fun rebuildStart(app: UBuildApp, reason: BuildReason)
  fun rebuildEnd(app: UBuildApp, reason: BuildReason, result: UBuildRes)
}

interface BuildReason {
  override fun toString(): String
}

class NoResultReason : BuildReason {
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

data class InconsistentTransientOutput(val inconsistentResult: UBuildRes) : BuildReason {
  override fun toString() = "transient output is inconsistent"
}

data class InconsistentGenPath(val generatingResult: UBuildRes, val gen: Gen, val newStamp: PathStamp) : BuildReason {
  override fun toString() = "generated path ${gen.path} is inconsistent"
}

data class InconsistentPathReq(val requiringResult: UBuildRes, val req: PathReq, val newStamp: PathStamp) : BuildReason {
  override fun toString() = "required path ${req.path} is inconsistent"
}

data class InconsistentBuildReq(val requiringResult: UBuildRes, val req: UBuildReq, val newStamp: OutputStamp) : BuildReason {
  override fun toString() = "required build ${req.app.toShortString(100)} is inconsistent"
}

data class InconsistentBuildReqTransientOutput(val requiringResult: UBuildRes, val req: UBuildReq, val inconsistentResult: UBuildRes) : BuildReason {
  override fun toString() = "transient output of required build ${req.app.toShortString(100)} is inconsistent"
}
