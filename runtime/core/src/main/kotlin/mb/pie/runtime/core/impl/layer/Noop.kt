package mb.pie.runtime.core.impl.layer

import mb.pie.runtime.core.*


/**
 * A build layer that does nothing. For debugging or benchmarking purposes only.
 * DO NOT USE in production, as it disables checking for inconsistencies in the build.
 */
class NoopLayer : Layer {
  override fun <I : In, O : Out> requireTopDownStart(task: Task<I, O>) {}

  override fun <I : In, O : Out> requireTopDownEnd(task: Task<I, O>) {}

  override fun <I : In, O : Out> validatePreWrite(task: Task<I, O>, data: TaskData<O>, txn: StoreReadTxn) {}

  override fun <I : In, O : Out> validatePostWrite(task: Task<I, O>, data: TaskData<O>, txn: StoreReadTxn) {}
}
