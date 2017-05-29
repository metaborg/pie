package mb.ceres

import mb.ceres.internal.EqualsOutputStamper
import java.io.Serializable

interface OutputStamper {
  fun <O : Out> stamp(output: O): OutputStamp
}

interface OutputStamp : Serializable {
  val stamper: OutputStamper
}

object OutputStampers {
  val equals = EqualsOutputStamper()
}