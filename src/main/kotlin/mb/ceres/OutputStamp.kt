package mb.ceres

import java.io.Serializable

interface OutputStamper {
  fun <O : Out> stamp(output: O): OutputStamp
}

interface OutputStamp : Serializable {
  val stamper: OutputStamper
}

data class ValueOutputStamp<out V>(val value: V, override val stamper: OutputStamper) : OutputStamp

class EqualsOutputStamper : OutputStamper {
  companion object {
    val instance = EqualsOutputStamper()
  }

  override fun <O : Out> stamp(output: O): OutputStamp {
    return ValueOutputStamp(output, this)
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
