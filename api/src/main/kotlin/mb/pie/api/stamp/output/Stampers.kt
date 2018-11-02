package mb.pie.api.stamp.output

import mb.pie.api.Out

/**
 * Common task output stampers.
 */
object OutputStampers {
  val equals = EqualsOutputStamper()
  fun funcEquals(func: (Out) -> Out) = FuncEqualsOutputStamper(func)

  val inconsequential = InconsequentialOutputStamper.instance
}
