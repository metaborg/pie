package mb.pie.runtime.core

import mb.pie.runtime.core.impl.Funcs


interface Layer {
  fun <I : In, O : Out> requireTopDownStart(app: FuncApp<I, O>)

  fun <I : In, O : Out> requireTopDownEnd(app: FuncApp<I, O>)

  fun <I : In, O : Out> validatePreWrite(app: FuncApp<I, O>, data: FuncAppData<O>, funcs: Funcs, txn: StoreReadTxn)

  fun <I : In, O : Out> validatePostWrite(app: FuncApp<I, O>, data: FuncAppData<O>, funcs: Funcs, txn: StoreReadTxn)
}
