package mb.pie.runtime.impl.stamp.path

import mb.pie.runtime.stamp.FileStamp
import mb.pie.runtime.stamp.FileStamper
import java.util.*


data class ValueFileStamp<out V>(private val value: V?, override val stamper: FileStamper) : FileStamp {
  override fun toString(): String {
    return value.toString()
  }
}

data class ByteArrayFileStamp(private val value: ByteArray?, override val stamper: FileStamper) : FileStamp {
  override fun equals(other: Any?): Boolean {
    if(this === other) return true
    if(other?.javaClass != javaClass) return false

    other as ByteArrayFileStamp

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
