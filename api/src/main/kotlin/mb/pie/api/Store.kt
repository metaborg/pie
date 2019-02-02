package mb.pie.api

/**
 * Internal storage for tasks, outputs, and dependency information.
 */
interface Store : AutoCloseable {
  /**
   * Opens a read transaction. Transaction must be [closed][close] after usage to free up internal resources.
   */
  fun readTxn(): StoreReadTxn

  /**
   * Opens a write transaction. Transaction must be [closed][close] after usage to commit written data and to free up
   * internal resources.
   */
  fun writeTxn(): StoreWriteTxn

  /**
   * Force synchronization of in-memory data to persistent storage.
   */
  fun sync()
}

/**
 * Storage transaction. Must be closed after use.
 */
interface StoreTxn : AutoCloseable {
  /**
   * Closes the transaction. Commits written data and frees up internal resources. Failure to close a transaction may
   * cause memory leaks and written data to not be visible to other transactions.
   */
  override fun close()
}

/**
 * Storage read transaction. Must be closed after use.
 */
interface StoreReadTxn : StoreTxn {
  /**
   * @return input for task with [key], or `null` if no input is stored.
   */
  fun input(key: TaskKey): In?

  /**
   * @return wrapper around output for [key], or `null` if no output is stored.
   */
  fun output(key: TaskKey): Output<*>?

  /**
   * @return observability of [key].
   */
  fun observability(key: TaskKey): Observability

  /**
   * @return task require dependencies (calls) of task [key].
   */
  fun taskRequires(key: TaskKey): List<TaskRequireDep>

  /**
   * @return callers of task [key].
   */
  fun callersOf(key: TaskKey): Set<TaskKey>


  /**
   * @return resource require dependencies of task [key].
   */
  fun resourceRequires(key: TaskKey): List<ResourceRequireDep>

  /**
   * @return tasks that require resource [key].
   */
  fun requireesOf(key: ResourceKey): Set<TaskKey>


  /**
   * @return resource provide dependencies of task [key].
   */
  fun resourceProvides(key: TaskKey): List<ResourceProvideDep>

  /**
   * @return task that provides resource [key], or `null` if no task provides it.
   */
  fun providerOf(key: ResourceKey): TaskKey?


  /**
   * @return output and dependencies for task [key], or `null` when no output was stored.
   */
  fun data(key: TaskKey): TaskData<*, *>?


  /**
   * @return number of source files: required files for which there is no generator.
   */
  fun numSourceFiles(): Int
}

/**
 * Storage read/write transaction. Must be closed after use.
 */
interface StoreWriteTxn : StoreReadTxn {
  /**
   * Sets the input of task [key] to [input].
   */
  fun setInput(key: TaskKey, input: In)

  /**
   * Sets the output of task [key] to [output].
   */
  fun setOutput(key: TaskKey, output: Out)

  /**
   * Sets the observability of a [key] to [observability].
   */
  fun setObservability(key : TaskKey,observability: Observability )

  /**
   * Sets the task require dependencies of task [key] to [taskRequires].
   */
  fun setTaskRequires(key: TaskKey, taskRequires: ArrayList<TaskRequireDep>)

  /**
   * Sets the resource require dependencies of task [key] to [resourceRequires].
   */
  fun setResourceRequires(key: TaskKey, resourceRequires: ArrayList<ResourceRequireDep>)

  /**
   * Sets the resource provide dependencies of task [key] to [resourceProvides].
   */
  fun setResourceProvides(key: TaskKey, resourceProvides: ArrayList<ResourceProvideDep>)

  /**
   * Sets the output and dependencies for task [key] to [data].
   */
  fun setData(key: TaskKey, data: TaskData<*, *>)


  /**
   * Removes all data from (drops) the store.
   */
  fun drop()
}


/**
 * Wrapper for a task output, to distinguish a null output object from a non-existent output.
 */
data class Output<out O : Out>(val output: O)

/**
 * Attempts to cast untyped output to typed output.
 */
@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
inline fun <O : Out> Output<*>.cast() = Output(this.output as O)


/**
 * Wrapper for task data: outputs and dependencies.
 */
data class TaskData<out I : In, out O : Out>(val input: I, val output: O, val taskRequires: ArrayList<TaskRequireDep>, val resourceRequires: ArrayList<ResourceRequireDep>, val resourceProvides: ArrayList<ResourceProvideDep>,val observability: Observability)

/**
 * Attempts to cast untyped task data to typed task data.
 */
@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
inline fun <I : In, O : Out> TaskData<*, *>.cast() = this as TaskData<I, O>
