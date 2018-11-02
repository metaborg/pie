package mb.pie.api.fs.stamp

import mb.pie.api.Resource
import mb.pie.api.fs.FileSystemResource
import mb.pie.api.stamp.ResourceStamp
import mb.pie.api.stamp.ResourceStamper

class ExistsResourceStamper : ResourceStamper {
  override fun stamp(resource: Resource): ResourceStamp {
    val node = (resource as FileSystemResource).node
    return ValueResourceStamp(node.exists(), this)
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
