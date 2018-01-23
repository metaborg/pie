package mb.pie.runtime.core.impl

import mb.pie.runtime.core.stamp.PathStamp
import mb.pie.runtime.core.impl.exec.ConsistencyChecker
import mb.vfs.path.PPath
import java.io.Serializable


data class Gen(val path: PPath, val stamp: PathStamp) : Serializable, ConsistencyChecker {
  override fun isConsistent(): Boolean {
    val newStamp = stamp.stamper.stamp(path)
    return stamp == newStamp
  }
}