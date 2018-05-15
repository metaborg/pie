package mb.pie.runtime.impl

import mb.pie.runtime.*
import mb.pie.runtime.stamp.FileStamp
import mb.pie.runtime.stamp.OutputStamp
import mb.vfs.path.PPath
import java.io.Serializable


//interface Req : Serializable {
//  fun <I : In, O : Out> makeConsistent(requiree: Task<I, O>, requireeRes: ExecRes<I, O>, exec: Exec, cancel: Cancelled, logger: Logger): ExecReason?
//}

internal interface FileConsistencyChecker {
  fun checkConsistency(): ExecReason?
  fun isConsistent(): Boolean
}

data class FileReq(val file: PPath, val stamp: FileStamp) : FileConsistencyChecker, Serializable {
  /**
   * @return an execution reason when this file requirement is inconsistent, `null` otherwise.
   */
  override fun checkConsistency(): InconsistentFileReq? {
    val newStamp = stamp.stamper.stamp(file)
    if(stamp != newStamp) {
      return InconsistentFileReq(this, newStamp)
    }
    return null
  }

  /**
   * @return `true` when this file requirement is consistent, `false` otherwise.
   */
  override fun isConsistent(): Boolean {
    val newStamp = stamp.stamper.stamp(file)
    return stamp == newStamp
  }

  override fun toString(): String {
    return "FileReq($file, $stamp)";
  }
}

data class FileGen(val file: PPath, val stamp: FileStamp) : FileConsistencyChecker, Serializable {
  /**
   * @return an execution reason when this file generates is inconsistent, `null` otherwise.
   */
  override fun checkConsistency(): InconsistentFileGen? {
    val newStamp = stamp.stamper.stamp(file)
    if(stamp != newStamp) {
      return InconsistentFileGen(this, newStamp)
    }
    return null
  }

  /**
   * @return `true` when this file generates is consistent, `false` otherwise.
   */
  override fun isConsistent(): Boolean {
    val newStamp = stamp.stamper.stamp(file)
    return stamp == newStamp
  }
}

data class TaskReq(val callee: UTask, val stamp: OutputStamp) : Serializable {
  /**
   * @return an execution reason when this call requirement is inconsistent w.r.t. [output], `null` otherwise.
   */
  fun checkConsistency(output: Out): InconsistentTaskReq? {
    val newStamp = stamp.stamper.stamp(output)
    if(stamp != newStamp) {
      return InconsistentTaskReq(this, newStamp)
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
  fun calleeEqual(other: UTask): Boolean {
    return when {
      other.id != callee.id -> false
      other == callee -> true
      else -> false
    }
  }

  override fun toString(): String {
    return "TaskReq(${callee.toShortString(100)}, $stamp)";
  }
}
