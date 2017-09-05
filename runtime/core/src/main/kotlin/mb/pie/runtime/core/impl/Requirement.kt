package mb.pie.runtime.core.impl

import mb.pie.runtime.core.*
import mb.vfs.path.PPath
import java.io.Serializable

interface Req : Serializable {
  fun <I : In, O : Out> makeConsistent(requiringApp: BuildApp<I, O>, requiringResult: BuildRes<I, O>, build: Build, reporter: BuildReporter): BuildReason?
}

data class PathReq(val path: PPath, val stamp: PathStamp) : Req {
  override fun <I : In, O : Out> makeConsistent(requiringApp: BuildApp<I, O>, requiringResult: BuildRes<I, O>, build: Build, reporter: BuildReporter): BuildReason? {
    val newStamp = stamp.stamper.stamp(path)
    reporter.checkReqPath(requiringApp, path, stamp, newStamp)
    if (stamp != newStamp) {
      return InconsistentPathReq(requiringResult, this, newStamp)
    }
    return null
  }
}

data class BuildReq<out AI : In, out AO : Out>(val app: BuildApp<AI, AO>, val stamp: OutputStamp) : Req {
  override fun <I : In, O : Out> makeConsistent(requiringApp: BuildApp<I, O>, requiringResult: BuildRes<I, O>, build: Build, reporter: BuildReporter): BuildReason? {
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


data class Gen(val path: PPath, val stamp: PathStamp) : Serializable