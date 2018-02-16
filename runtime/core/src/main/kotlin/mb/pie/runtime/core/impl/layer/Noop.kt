package mb.pie.runtime.core.impl.layer

import mb.pie.runtime.core.*
import mb.pie.runtime.core.impl.Funcs

/**
 * A build layer that does nothing. For debugging or benchmarking purposes only.
 * DO NOT USE in production, as it disables checking for inconsistencies in the build.
 */
class NoopLayer : Layer {
  override fun <I : In, O : Out> requireTopDownStart(app: FuncApp<I, O>) {}

  override fun <I : In, O : Out> requireTopDownEnd(app: FuncApp<I, O>) {}

  override fun <I : In, O : Out> validatePreWrite(app: FuncApp<I, O>, data: FuncAppData<O>, funcs: Funcs, txn: StoreReadTxn) {}

  override fun <I : In, O : Out> validatePostWrite(app: FuncApp<I, O>, data: FuncAppData<O>, funcs: Funcs, txn: StoreReadTxn) {}
}