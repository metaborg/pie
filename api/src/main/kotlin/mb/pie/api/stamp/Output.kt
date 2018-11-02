package mb.pie.api.stamp

import mb.pie.api.Out
import mb.pie.api.stamp.output.EqualsOutputStamper
import mb.pie.api.stamp.output.InconsequentialOutputStamper
import java.io.Serializable

/**
 * Stamper for customizable change detection on task outputs. Stampers must be [Serializable].
 */
interface OutputStamper : Serializable {
  fun stamp(output: Out): OutputStamp
}

/**
 * Stamp produced by an [OutputStamper]. Stamps must be [Serializable].
 */
interface OutputStamp : Serializable {
  val stamper: OutputStamper
}
