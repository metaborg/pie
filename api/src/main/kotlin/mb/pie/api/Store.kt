package mb.pie.api

import mb.pie.vfs.path.PPath

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
   * @return task requirements (calls) of [key].
   */
  fun taskReqs(key: TaskKey): List<TaskReq>

  /**
   * @return callers of [key].
   */
  fun callersOf(key: TaskKey): Set<TaskKey>


  /**
   * @return file requirements of [key].
   */
  fun fileReqs(key: TaskKey): List<FileReq>

  /**
   * @return tasks that require [file].
   */
  fun requireesOf(file: PPath): Set<TaskKey>


  /**
   * @return file generates of [key].
   */
  fun fileGens(key: TaskKey): List<FileGen>

  /**
   * @return file that generates [file], or `null` if it does not exist.
   */
  fun generatorOf(file: PPath): TaskKey?


  /**
   * @return output, task requirements, file requirements, and file generates for [key], or `null` when no output was stored.
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
   * Sets the input of [key] to [input].
   */
  fun setInput(key: TaskKey, input: In)

  /**
   * Sets the output of [key] to [output].
   */
  fun setOutput(key: TaskKey, output: Out)

  /**
   * Sets the task requirements of [key] to [taskReqs].
   */
  fun setTaskReqs(key: TaskKey, taskReqs: ArrayList<TaskReq>)

  /**
   * Sets the file requirements of [key] to [fileReqs].
   */
  fun setFileReqs(key: TaskKey, fileReqs: ArrayList<FileReq>)

  /**
   * Sets the generated fileGens of [key] to [fileGens].
   */
  fun setFileGens(key: TaskKey, fileGens: ArrayList<FileGen>)

  /**
   * Sets the output, call requirements, file reqs, and file generates for [key] to [data].
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
data class TaskData<out I : In, out O : Out>(val input: I, val output: O, val taskReqs: ArrayList<TaskReq>, val fileReqs: ArrayList<FileReq>, val fileGens: ArrayList<FileGen>)

/**
 * Attempts to cast untyped task data to typed task data.
 */
@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
inline fun <I : In, O : Out> TaskData<*, *>.cast() = this as TaskData<I, O>
