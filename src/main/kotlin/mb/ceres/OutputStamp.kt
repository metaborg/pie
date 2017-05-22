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
  override fun <O : Out> stamp(output: O): OutputStamp {
    return ValueOutputStamp(output, this)
  }
}

class HashOutputStamper : OutputStamper {
  override fun <O : Out> stamp(output: O): OutputStamp {
    return ValueOutputStamp(output.hashCode(), this)
  }
}