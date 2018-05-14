package mb.pie.runtime.core.stamp

import mb.pie.runtime.core.Out
import mb.pie.runtime.core.impl.stamp.output.EqualsOutputStamper
import mb.pie.runtime.core.impl.stamp.output.InconsequentialOutputStamper
import java.io.Serializable


/**
 * Stamper for customizable change detection on task outputs. Stampers must be [Serializable].
 */
interface OutputStamper : Serializable {
  fun <O : Out> stamp(output: O): OutputStamp
}

/**
 * Stamp produced by an [OutputStamper]. Stamps must be [Serializable].
 */
interface OutputStamp : Serializable {
  val stamper: OutputStamper
}

/**
 * Common task output stampers.
 */
object OutputStampers {
  val equals = EqualsOutputStamper()
  val inconsequential = InconsequentialOutputStamper.instance
}
