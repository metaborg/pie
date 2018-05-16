package mb.pie.api.stamp.path

import mb.pie.api.stamp.FileStamp
import mb.pie.api.stamp.FileStamper
import mb.vfs.path.PPath

class ExistsFileStamper : FileStamper {
  override fun stamp(path: PPath): FileStamp {
    return ValueFileStamp(path.exists(), this)
  }


  override fun equals(other: Any?): Boolean {
    if(this === other) return true
    if(other?.javaClass != javaClass) return false
    return true
  }

  override fun hashCode(): Int {
    return 0
  }

  override fun toString(): String {
    return "Exists"
  }
}
