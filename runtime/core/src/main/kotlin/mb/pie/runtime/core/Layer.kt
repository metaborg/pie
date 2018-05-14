package mb.pie.runtime.core


/**
 * Internal layer for intercepting parts of task execution, used for validation.
 */
interface Layer {
  fun <I : In, O : Out> requireTopDownStart(task: Task<I, O>)

  fun <I : In, O : Out> requireTopDownEnd(task: Task<I, O>)

  fun <I : In, O : Out> validatePreWrite(task: Task<I, O>, data: TaskData<O>, txn: StoreReadTxn)

  fun <I : In, O : Out> validatePostWrite(task: Task<I, O>, data: TaskData<O>, txn: StoreReadTxn)
}
