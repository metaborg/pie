package mb.ceres

import mb.ceres.impl.EqualsOutputStamper
import mb.ceres.impl.InconsequentialOutputStamper
import java.io.Serializable

interface OutputStamper : Serializable {
  fun <O : Out> stamp(output: O): OutputStamp
}

interface OutputStamp : Serializable {
  val stamper: OutputStamper
}

object OutputStampers {
  val equals = EqualsOutputStamper()
  val inconsequential = InconsequentialOutputStamper.instance
}