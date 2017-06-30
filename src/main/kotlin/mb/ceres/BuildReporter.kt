package mb.ceres

import mb.ceres.impl.BuildReq
import mb.ceres.impl.Gen
import mb.ceres.impl.PathReq

interface BuildReporter {
  fun <I : In, O : Out> require(app: BuildApp<I, O>)
  fun <I : In, O : Out> build(app: BuildApp<I, O>, reason: BuildReason)
  fun <I : In, O : Out> buildSuccess(app: BuildApp<I, O>, reason: BuildReason, result: BuildRes<I, O>)
  fun <I : In, O : Out> buildFailed(app: BuildApp<I, O>, reason: BuildReason, exception: BuildException)
}

interface BuildReason {
  override fun toString(): String
}

class NoResultReason : BuildReason {
  override fun toString() = "no stored or cached result"


  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other?.javaClass != javaClass) return false
    return true
  }

  override fun hashCode(): Int {
    return 0
  }
}

data class InconsistentTransientOutput<out I : In, out O : Out>(val inconsistentResult: BuildRes<I, O>) : BuildReason {
  override fun toString() = "transient output is inconsistent"
}

data class InconsistentGenPath<out I : In, out O : Out>(val generatingResult: BuildRes<I, O>, val gen: Gen, val newStamp: PathStamp) : BuildReason {
  override fun toString() = "generated path ${gen.path} is inconsistent"
}

data class InconsistentPathReq<out I : In, out O : Out>(val requiringResult: BuildRes<I, O>, val req: PathReq, val newStamp: PathStamp) : BuildReason {
  override fun toString() = "required path ${req.path} is inconsistent"
}

data class InconsistentBuildReq<out I : In, out O : Out>(val requiringResult: BuildRes<I, O>, val req: BuildReq<I, O>, val newStamp: OutputStamp) : BuildReason {
  override fun toString() = "required build ${req.app.toShortString(100)} is inconsistent"
}

data class InconsistentBuildReqTransientOutput<out I : In, out O : Out>(val requiringResult: BuildRes<I, O>, val req: BuildReq<I, O>, val inconsistentResult: BuildRes<I, O>) : BuildReason {
  override fun toString() = "transient output of required build ${req.app.toShortString(100)} is inconsistent"
}
