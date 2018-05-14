package mb.pie.runtime.core.impl

import mb.pie.runtime.core.*
import mb.pie.runtime.core.stamp.OutputStamp
import mb.pie.runtime.core.stamp.PathStamp
import mb.vfs.path.PPath
import java.io.Serializable


//interface Req : Serializable {
//  fun <I : In, O : Out> makeConsistent(requiree: FuncApp<I, O>, requireeRes: ExecRes<I, O>, exec: Exec, cancel: Cancelled, logger: Logger): ExecReason?
//}

internal interface PathConsistencyChecker {
  fun checkConsistency(): ExecReason?
  fun isConsistent(): Boolean
}

data class PathReq(val path: PPath, val stamp: PathStamp) : PathConsistencyChecker, Serializable {
  /**
   * @return an execution reason when this path requirement is inconsistent, `null` otherwise.
   */
  override fun checkConsistency(): InconsistentPathReq? {
    val newStamp = stamp.stamper.stamp(path)
    if(stamp != newStamp) {
      return InconsistentPathReq(this, newStamp)
    }
    return null
  }

  /**
   * @return `true` when this path requirement is consistent, `false` otherwise.
   */
  override fun isConsistent(): Boolean {
    val newStamp = stamp.stamper.stamp(path)
    return stamp == newStamp
  }

  override fun toString(): String {
    return "PathReq($path, $stamp)";
  }
}

data class PathGen(val path: PPath, val stamp: PathStamp) : PathConsistencyChecker, Serializable {
  /**
   * @return an execution reason when this path generates is inconsistent, `null` otherwise.
   */
  override fun checkConsistency(): InconsistentPathGen? {
    val newStamp = stamp.stamper.stamp(path)
    if(stamp != newStamp) {
      return InconsistentPathGen(this, newStamp)
    }
    return null
  }

  /**
   * @return `true` when this path generates is consistent, `false` otherwise.
   */
  override fun isConsistent(): Boolean {
    val newStamp = stamp.stamper.stamp(path)
    return stamp == newStamp
  }
}

data class CallReq(val callee: UFuncApp, val stamp: OutputStamp) : Serializable {
  /**
   * @return an execution reason when this call requirement is inconsistent w.r.t. [output], `null` otherwise.
   */
  fun checkConsistency(output: Out): InconsistentCallReq? {
    val newStamp = stamp.stamper.stamp(output)
    if(stamp != newStamp) {
      return InconsistentCallReq(this, newStamp)
    }
    return null
  }

  /**
   * @return `true` when this call requirement is consistent w.r.t. [output], `false` otherwise.
   */
  fun isConsistent(output: Out): Boolean {
    val newStamp = stamp.stamper.stamp(output)
    return newStamp == stamp
  }

  /**
   * @return `true` when this call requirement's callee is equal to [other], `false` otherwise.
   */
  fun calleeEqual(other: UFuncApp): Boolean {
    return when {
      other.id != callee.id -> false
      other == callee -> true
      else -> false
    }
  }

  override fun toString(): String {
    return "CallReq(${callee.toShortString(100)}, $stamp)";
  }
}
