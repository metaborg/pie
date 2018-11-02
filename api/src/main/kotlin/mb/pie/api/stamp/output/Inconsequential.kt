package mb.pie.api.stamp.output

import mb.pie.api.Out
import mb.pie.api.stamp.OutputStamp
import mb.pie.api.stamp.OutputStamper

/**
 * Output stamper that always produces the same stamp: the [inconsequential stamp][InconsequentialStamp], effectively ignoring the output.
 */
class InconsequentialOutputStamper : OutputStamper {
  companion object {
    val instance = InconsequentialOutputStamper()
  }

  override fun stamp(output: Out): OutputStamp {
    return InconsequentialStamp.instance
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
    return "Inconsequential";
  }
}

class InconsequentialStamp : OutputStamp {
  companion object {
    val instance = InconsequentialStamp()
  }

  override val stamper: OutputStamper = InconsequentialOutputStamper.instance

  override fun equals(other: Any?): Boolean {
    if(this === other) return true
    if(other?.javaClass != javaClass) return false
    return true
  }

  override fun hashCode(): Int {
    return 0
  }

  override fun toString(): String {
    return "InconsequentialStamp"
  }
}
