package mb.pie.runtime.core.impl

import mb.pie.runtime.core.*
import mb.util.async.Cancelled
import mb.vfs.path.PPath
import java.io.Serializable


interface Req : Serializable {
  fun <I : In, O : Out> makeConsistent(requiree: FuncApp<I, O>, requireeRes: ExecRes<I, O>, exec: Exec, cancel: Cancelled, logger: Logger): ExecReason?
}

data class PathReq(val path: PPath, val stamp: PathStamp) : Req, ConsistencyChecker {
  override fun isConsistent(): Boolean {
    val newStamp = stamp.stamper.stamp(path)
    return stamp == newStamp
  }

  override fun <I : In, O : Out> makeConsistent(requiree: FuncApp<I, O>, requireeRes: ExecRes<I, O>, exec: Exec, cancel: Cancelled, logger: Logger): InconsistentPathReq? {
    logger.checkPathReqStart(requiree, this)
    val newStamp = stamp.stamper.stamp(path)
    val reason = if(stamp != newStamp) {
      InconsistentPathReq(requireeRes, this, newStamp)
    } else {
      null
    }
    logger.checkPathReqEnd(requiree, this, reason)
    return reason
  }
}

data class CallReq<out AI : In, out AO : Out>(val callee: FuncApp<AI, AO>, val stamp: OutputStamp) : Req {
  override fun <I : In, O : Out> makeConsistent(requiree: FuncApp<I, O>, requireeRes: ExecRes<I, O>, exec: Exec, cancel: Cancelled, logger: Logger): ExecReason? {
    val result = exec.require(callee, cancel).result
    logger.checkBuildReqStart(requiree, this)
    val reason = if(!result.isInternallyConsistent) {
      // CHANGED: paper algorithm did not check if the output changed, which would cause inconsistencies.
      // If output is not consistent, requirement is not consistent.
      // TODO: is this necessary?
      InconsistentExecReqTransientOutput(requireeRes, this, result)
    } else {
      val newStamp = stamp.stamper.stamp(result.output)
      if(stamp != newStamp) {
        // If stamp has changed, requirement is not consistent
        InconsistentExecReq(requireeRes, this, newStamp)
      } else {
        null
      }
    }
    logger.checkBuildReqEnd(requiree, this, reason)
    return reason
  }

  /**
   * @return `true` when this call requirement is consistent w.r.t. [calleeRes], `false` otherwise.
   */
  fun isConsistent(calleeRes: UExecRes) = stamp.stamper.stamp(calleeRes.output) == stamp

  /**
   * @return `true` when this call requirement's callee is equal to [other], or when it overlaps with a call to [other], `false` otherwise.
   */
  fun equalsOrOverlaps(other: UFuncApp, funcs: Funcs): Boolean {
    return when {
      other.id != callee.id -> false
      other == callee -> true
      else -> {
        val func = funcs.getAnyFunc(other.id);
        when {
          func.mayOverlap(callee.input, other.input) -> true
          else -> false
        }
      }
    }
  }
}


typealias UCallReq = CallReq<*, *>

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
inline fun <I : In, O : Out> UCallReq.cast() = this as CallReq<I, O>
