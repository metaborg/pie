package mb.pie.api

/**
 * Internal layer for intercepting parts of task execution, used for validation.
 */
interface Layer {
  fun requireTopDownStart(key: TaskKey, input: In)
  fun requireTopDownEnd(key: TaskKey)
  fun <I : In, O : Out> validatePreWrite(key: TaskKey, data: TaskData<I, O>, txn: StoreReadTxn)
  fun <I : In, O : Out> validatePostWrite(key: TaskKey, data: TaskData<I, O>, txn: StoreReadTxn)
}
