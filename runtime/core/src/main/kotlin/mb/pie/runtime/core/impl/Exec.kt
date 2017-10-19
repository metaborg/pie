package mb.pie.runtime.core.impl

import mb.pie.runtime.core.*


interface Exec {
  fun <I : In, O : Out> require(app: FuncApp<I, O>): ExecInfo<I, O>
}

interface Funcs {
  fun <I : In, O : Out> getFunc(id: String): Func<I, O>
  fun getUFunc(id: String): UFunc
  fun getAnyFunc(id: String): AnyFunc
}
