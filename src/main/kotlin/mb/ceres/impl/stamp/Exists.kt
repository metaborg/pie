package mb.ceres.impl.stamp

import mb.ceres.PathStamp
import mb.ceres.PathStamper
import mb.vfs.path.PPath

class ExistsPathStamper : PathStamper {
  override fun stamp(path: PPath): PathStamp {
    return ValuePathStamp(path.exists(), this)
  }


  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other?.javaClass != javaClass) return false
    return true
  }

  override fun hashCode(): Int {
    return 0
  }
}


