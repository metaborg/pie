package mb.pie.runtime.core.impl.layer

import mb.pie.runtime.core.*
import mb.pie.runtime.core.impl.Funcs

/**
 * A build layer that does nothing. For debugging or benchmarking purposes only.
 * DO NOT USE in production, as it disables checking for inconsistencies in the build.
 */
class NoopLayer : Layer {
  override fun <I : In, O : Out> requireStart(app: FuncApp<I, O>) {}

  override fun <I : In, O : Out> requireEnd(app: FuncApp<I, O>) {}

  override fun <I : In, O : Out> validate(app: FuncApp<I, O>, result: ExecRes<I, O>, funcs: Funcs, readTnx: StoreReadTxn) {}
}