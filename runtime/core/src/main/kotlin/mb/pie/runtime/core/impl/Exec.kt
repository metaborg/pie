package mb.pie.runtime.core.impl

import mb.pie.runtime.core.*


interface Exec {
  fun <I : In, O : Out> require(app: FuncApp<I, O>): ExecInfo<I, O>
}
