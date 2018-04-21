package mb.pie.runtime.core

import mb.pie.runtime.core.impl.Funcs


interface Layer {
  fun <I : In, O : Out> requireTopDownStart(app: FuncApp<I, O>)

  fun <I : In, O : Out> requireTopDownEnd(app: FuncApp<I, O>)

  fun <I : In, O : Out> validate(app: FuncApp<I, O>, data: FuncAppData<O>, funcs: Funcs, txn: StoreReadTxn)
}
