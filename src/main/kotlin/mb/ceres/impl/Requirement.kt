package mb.ceres.impl

import mb.ceres.*
import java.io.Serializable

interface Req : Serializable {
  fun <I : In, O : Out> makeConsistent(requiringResult: BuildRes<I, O>, build: Build): BuildReason?
}

data class PathReq(val path: CPath, val stamp: PathStamp) : Req {
  override fun <I : In, O : Out> makeConsistent(requiringResult: BuildRes<I, O>, build: Build): BuildReason? {
    val newStamp = stamp.stamper.stamp(path)
    if (stamp != newStamp) {
      return InconsistentPathReq(requiringResult, this, newStamp)
    }
    return null
  }
}

data class BuildReq<out AI : In, out AO : Out>(val app: BuildApp<AI, AO>, val stamp: OutputStamp) : Req {
  override fun <I : In, O : Out> makeConsistent(requiringResult: BuildRes<I, O>, build: Build): BuildReason? {
    val result = build.require(app).result

    // CHANGED: paper algorithm did not check if the output changed, which would cause inconsistencies.
    // If output is not consistent, requirement is not consistent
    if (!result.isConsistent) {
      return InconsistentBuildReqTransientOutput(requiringResult, this, result)
    }

    // If stamp has changed, requirement is not consistent
    val newStamp = stamp.stamper.stamp(result.output)
    if (stamp != newStamp) {
      return InconsistentBuildReq(requiringResult, this, newStamp)
    }
    return null
  }
}
typealias UBuildReq = BuildReq<*, *>


data class Gen(val path: CPath, val stamp: PathStamp) : Serializable