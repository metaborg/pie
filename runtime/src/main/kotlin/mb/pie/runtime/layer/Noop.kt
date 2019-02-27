package mb.pie.runtime.layer

import mb.pie.api.*

/**
 * A build layer that does nothing. For debugging or benchmarking purposes only.
 * DO NOT USE in production, as it disables checking for inconsistencies in the build.
 */
public class NoopLayer : Layer {
  override fun requireTopDownStart(key: TaskKey, input: In) {}
  override fun requireTopDownEnd(key: TaskKey) {}
  override fun <I : In, O : Out> validatePreWrite(key: TaskKey, data: TaskData<I, O>, txn: StoreReadTxn) {}
  override fun <I : In, O : Out> validatePostWrite(key: TaskKey, data: TaskData<I, O>, txn: StoreReadTxn) {}
}
