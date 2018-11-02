package mb.pie.api.fs.stamp

import mb.pie.api.fs.FileSystemResource
import mb.pie.api.stamp.ResourceStamp
import java.util.*

typealias FileSystemStamp = ResourceStamp<FileSystemResource>

data class ValueResourceStamp<out V>(private val value: V?, override val stamper: FileSystemStamper) : FileSystemStamp {
  override fun toString(): String {
    return value.toString()
  }
}

data class ByteArrayResourceStamp(private val value: ByteArray?, override val stamper: FileSystemStamper) : FileSystemStamp {
  override fun equals(other: Any?): Boolean {
    if(this === other) return true
    if(other?.javaClass != javaClass) return false

    other as ByteArrayResourceStamp

    if(!Arrays.equals(value, other.value)) return false // Override required for array equality
    if(stamper != other.stamper) return false

    return true
  }

  override fun hashCode(): Int {
    var result = value?.let { Arrays.hashCode(it) } ?: 0 // Override required for array hashcode
    result = 31 * result + stamper.hashCode()
    return result
  }

  override fun toString(): String {
    return if(value == null) "null" else javax.xml.bind.DatatypeConverter.printHexBinary(value)
  }
}
