package mb.pie.api.fs.stamp

import mb.pie.api.stamp.ResourceStamp
import mb.pie.api.stamp.ResourceStamper
import java.util.*

data class ValueResourceStamp<out V>(private val value: V?, override val stamper: ResourceStamper) : ResourceStamp {
  override fun toString(): String {
    return value.toString()
  }
}

data class ByteArrayResourceStamp(private val value: ByteArray?, override val stamper: ResourceStamper) : ResourceStamp {
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
