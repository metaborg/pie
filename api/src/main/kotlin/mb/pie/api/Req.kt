package mb.pie.api

import mb.pie.api.exec.ExecReason
import mb.pie.api.stamp.FileStamp
import mb.pie.api.stamp.OutputStamp
import mb.pie.vfs.path.PPath
import java.io.Serializable

interface FileDep {
  /**
   * @return an execution reason when this file requirement is inconsistent, `null` otherwise.
   */
  fun checkConsistency(): ExecReason?

  /**
   * @return `true` when this file requirement is consistent, `false` otherwise.
   */
  fun isConsistent(): Boolean
}


data class FileReq(
  val file: PPath,
  val stamp: FileStamp
) : FileDep, Serializable {
  override fun checkConsistency(): InconsistentFileReq? {
    val newStamp = stamp.stamper.stamp(file)
    if(stamp != newStamp) {
      return InconsistentFileReq(this, newStamp)
    }
    return null
  }

  override fun isConsistent(): Boolean {
    val newStamp = stamp.stamper.stamp(file)
    return stamp == newStamp
  }

  override fun toString(): String {
    return "FileReq($file, $stamp)";
  }
}

data class InconsistentFileReq(val req: FileReq, val newStamp: FileStamp) : ExecReason {
  override fun toString() = "inconsistent required file ${req.file}"
}


data class FileGen(
  val file: PPath,
  val stamp: FileStamp
) : FileDep, Serializable {
  override fun checkConsistency(): InconsistentFileGen? {
    val newStamp = stamp.stamper.stamp(file)
    if(stamp != newStamp) {
      return InconsistentFileGen(this, newStamp)
    }
    return null
  }

  override fun isConsistent(): Boolean {
    val newStamp = stamp.stamper.stamp(file)
    return stamp == newStamp
  }
}

data class InconsistentFileGen(val fileGen: FileGen, val newStamp: FileStamp) : ExecReason {
  override fun toString() = "inconsistent generated file ${fileGen.file}"
}


data class TaskReq(
  val callee: TaskKey,
  val stamp: OutputStamp
) : Serializable {
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
  fun calleeEqual(other: TaskKey): Boolean {
    return other == callee
  }

  override fun toString(): String {
    return "TaskReq(${callee.toShortString(100)}, $stamp)";
  }
}

data class InconsistentTaskReq(val req: TaskReq, val newStamp: OutputStamp) : ExecReason {
  override fun toString() = "inconsistent required task ${req.callee.toShortString(100)}"
}
