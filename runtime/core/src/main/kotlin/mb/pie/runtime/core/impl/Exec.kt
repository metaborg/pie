package mb.pie.runtime.core.impl

import mb.pie.runtime.core.*
import mb.util.async.Cancelled
import mb.util.async.NullCancelled


interface Exec {
  fun <I : In, O : Out> require(app: FuncApp<I, O>, cancel: Cancelled = NullCancelled()): ExecRes<O>
}


data class ExecRes<out O : Out>(val output: O, val reason: ExecReason?) {
  constructor(output: O) : this(output, null)

  val wasExecuted = reason != null

  /**
   * @return [output] of this result as a short string, with up to [maxLength] characters.
   */
  fun toShortString(maxLength: Int) = output.toString().toShortString(maxLength)
}
typealias UExecRes = ExecRes<*>
