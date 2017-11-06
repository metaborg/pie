package mb.pie.runtime.core.impl

import mb.pie.runtime.core.*
import mb.util.async.Cancelled
import mb.vfs.path.PPath
import java.io.Serializable


interface Req : Serializable {
  fun <I : In, O : Out> makeConsistent(requiringApp: FuncApp<I, O>, requiringResult: ExecRes<I, O>, exec: Exec, cancel: Cancelled, logger: Logger): ExecReason?
}

data class PathReq(val path: PPath, val stamp: PathStamp) : Req, ConsistencyChecker {
  override fun isConsistent(): Boolean {
    val newStamp = stamp.stamper.stamp(path)
    return stamp == newStamp
  }

  override fun <I : In, O : Out> makeConsistent(requiringApp: FuncApp<I, O>, requiringResult: ExecRes<I, O>, exec: Exec, cancel: Cancelled, logger: Logger): InconsistentPathReq? {
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

data class ExecReq<out AI : In, out AO : Out>(val app: FuncApp<AI, AO>, val stamp: OutputStamp) : Req {
  override fun <I : In, O : Out> makeConsistent(requiringApp: FuncApp<I, O>, requiringResult: ExecRes<I, O>, exec: Exec, cancel: Cancelled, logger: Logger): ExecReason? {
    val result = exec.require(app, cancel).result
    logger.checkBuildReqStart(requiringApp, this)
    val reason = if(!result.isInternallyConsistent) {
      // CHANGED: paper algorithm did not check if the output changed, which would cause inconsistencies.
      // If output is not consistent, requirement is not consistent.
      // TODO: is this necessary?
      InconsistentExecReqTransientOutput(requiringResult, this, result)
    } else {
      val newStamp = stamp.stamper.stamp(result.output)
      if(stamp != newStamp) {
        // If stamp has changed, requirement is not consistent
        InconsistentExecReq(requiringResult, this, newStamp)
      } else {
        null
      }
    }
    logger.checkBuildReqEnd(requiringApp, this, reason)
    return reason
  }
}
typealias UExecReq = ExecReq<*, *>
