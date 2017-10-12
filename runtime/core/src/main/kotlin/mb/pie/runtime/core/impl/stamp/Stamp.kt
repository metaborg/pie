package mb.pie.runtime.core.impl.stamp

import mb.pie.runtime.core.PathStamp
import mb.pie.runtime.core.PathStamper
import java.util.*

data class ValuePathStamp<out V>(val value: V?, override val stamper: PathStamper) : PathStamp {
  override fun toString(): String {
    return value.toString()
  }
}

data class ByteArrayPathStamp(val value: ByteArray?, override val stamper: PathStamper) : PathStamp {
  override fun equals(other: Any?): Boolean {
    if(this === other) return true
    if(other?.javaClass != javaClass) return false

    other as ByteArrayPathStamp

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
