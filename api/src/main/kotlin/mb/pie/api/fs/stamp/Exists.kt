package mb.pie.api.fs.stamp

import mb.pie.api.fs.FileSystemResource

class ExistsResourceStamper : FileSystemStamper {
  override fun stamp(resource: FileSystemResource): FileSystemStamp {
    return ValueResourceStamp(resource.node.exists(), this)
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
