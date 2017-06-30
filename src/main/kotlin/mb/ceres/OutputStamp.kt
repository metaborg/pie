package mb.ceres

import mb.ceres.impl.EqualsOutputStamper
import java.io.Serializable

interface OutputStamper : Serializable {
  fun <O : Out> stamp(output: O): OutputStamp
}

interface OutputStamp : Serializable {
  val stamper: OutputStamper
}

object OutputStampers {
  val equals = EqualsOutputStamper()
}