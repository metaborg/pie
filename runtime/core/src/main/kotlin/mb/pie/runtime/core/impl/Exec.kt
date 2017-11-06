package mb.pie.runtime.core.impl

import mb.pie.runtime.core.*
import mb.util.async.Cancelled
import mb.util.async.NullCancelled


interface Exec {
  fun <I : In, O : Out> require(app: FuncApp<I, O>, cancel: Cancelled = NullCancelled()): ExecInfo<I, O>
}
