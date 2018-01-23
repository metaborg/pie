package mb.pie.runtime.core.stamp

import mb.pie.runtime.core.Out
import mb.pie.runtime.core.impl.stamp.output.EqualsOutputStamper
import mb.pie.runtime.core.impl.stamp.output.InconsequentialOutputStamper
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
