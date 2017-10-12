package mb.pie.runtime.core.impl

import mb.pie.runtime.core.*
import mb.vfs.path.PPath
import java.io.Serializable

interface Req : Serializable {
  fun <I : In, O : Out> makeConsistent(requiringApp: BuildApp<I, O>, requiringResult: BuildRes<I, O>, build: Build, logger: BuildLogger): BuildReason?
}

data class PathReq(val path: PPath, val stamp: PathStamp) : Req {
  override fun <I : In, O : Out> makeConsistent(requiringApp: BuildApp<I, O>, requiringResult: BuildRes<I, O>, build: Build, logger: BuildLogger): InconsistentPathReq? {
    logger.checkPathReqStart(requiringApp, this)
    val newStamp = stamp.stamper.stamp(path)
    val reason = if(stamp != newStamp) {
      InconsistentPathReq(requiringResult, this, newStamp)
    } else {
      null
    }
    logger.checkPathReqEnd(requiringApp, this, reason)
    return reason
  }
}

data class BuildReq<out AI : In, out AO : Out>(val app: BuildApp<AI, AO>, val stamp: OutputStamp) : Req {
  override fun <I : In, O : Out> makeConsistent(requiringApp: BuildApp<I, O>, requiringResult: BuildRes<I, O>, build: Build, logger: BuildLogger): BuildReason? {
    logger.checkBuildReqStart(requiringApp, this)
    val result = build.require(app).result
    val reason = if(!result.isConsistent) {
      // CHANGED: paper algorithm did not check if the output changed, which would cause inconsistencies.
      // If output is not consistent, requirement is not requireEnd
      // TODO: is this necessary?
      InconsistentBuildReqTransientOutput(requiringResult, this, result)
    } else {
      val newStamp = stamp.stamper.stamp(result.output)
      if(stamp != newStamp) {
        // If stamp has changed, requirement is not consistent
        InconsistentBuildReq(requiringResult, this, newStamp)
      } else {
        null
      }
    }
    logger.checkBuildReqEnd(requiringApp, this, reason)
    return reason
  }
}
typealias UBuildReq = BuildReq<*, *>


data class Gen(val path: PPath, val stamp: PathStamp) : Serializable