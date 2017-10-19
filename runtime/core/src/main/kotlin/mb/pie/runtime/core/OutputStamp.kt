package mb.pie.runtime.core

import mb.pie.runtime.core.impl.EqualsOutputStamper
import mb.pie.runtime.core.impl.InconsequentialOutputStamper
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
