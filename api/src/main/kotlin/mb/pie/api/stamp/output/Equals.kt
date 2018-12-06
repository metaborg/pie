package mb.pie.api.stamp.output

import mb.pie.api.Out
import mb.pie.api.stamp.OutputStamp
import mb.pie.api.stamp.OutputStamper

/**
 * Output stamper that copies outputs into a stamp, and compares these stamps by equality.
 */
class EqualsOutputStamper : OutputStamper {
  override fun stamp(output: Out): OutputStamp {
    return ValueOutputStamp(output, this)
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
    return "Equals"
  }
}

/**
 * Output stamper that first applies given [func] to an output, copies the result of that into a stamp, and compares these stamps by equality.
 * Given [function][func] must be [Serializable].
 */
data class FuncEqualsOutputStamper(private val func: (Out) -> Out) : OutputStamper {
  override fun stamp(output: Out): OutputStamp {
    val value = func(output)
    return ValueOutputStamp(value, this)
  }
}
