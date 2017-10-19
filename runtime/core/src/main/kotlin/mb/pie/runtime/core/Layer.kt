package mb.pie.runtime.core

import mb.pie.runtime.core.impl.Funcs


interface Layer {
  fun <I : In, O : Out> requireStart(app: FuncApp<I, O>)

  fun <I : In, O : Out> requireEnd(app: FuncApp<I, O>)

  fun <I : In, O : Out> validate(app: FuncApp<I, O>, result: ExecRes<I, O>, funcs: Funcs, readTnx: StoreReadTxn)
}
