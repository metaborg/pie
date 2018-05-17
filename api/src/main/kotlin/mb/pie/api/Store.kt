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
   * @return wrapper around output for [task], or `null` if no output is stored.
   */
  fun output(task: UTask): UOutput?


  /**
   * @return task requirements (calls) of [task].
   */
  fun taskReqs(task: UTask): List<TaskReq>

  /**
   * @return callers of [task].
   */
  fun callersOf(task: UTask): Set<UTask>


  /**
   * @return file requirements of [task].
   */
  fun fileReqs(task: UTask): List<FileReq>

  /**
   * @return tasks that require [file].
   */
  fun requireesOf(file: PPath): Set<UTask>


  /**
   * @return file generates of [task].
   */
  fun fileGens(task: UTask): List<FileGen>

  /**
   * @return file that generates [file], or `null` if it does not exist.
   */
  fun generatorOf(file: PPath): UTask?


  /**
   * @return output, task requirements, file requirements, and file generates for [task], or `null` when no output was stored.
   */
  fun data(task: UTask): UTaskData?


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
   * Sets the output of [task] to [output].
   */
  fun setOutput(task: UTask, output: Out)

  /**
   * Sets the task requirements of [task] to [taskReqs].
   */
  fun setTaskReqs(task: UTask, taskReqs: ArrayList<TaskReq>)

  /**
   * Sets the file requirements of [task] to [fileReqs].
   */
  fun setFileReqs(task: UTask, fileReqs: ArrayList<FileReq>)

  /**
   * Sets the generated fileGens of [task] to [fileGens].
   */
  fun setFileGens(task: UTask, fileGens: ArrayList<FileGen>)

  /**
   * Sets the output, call requirements, file reqs, and file generates for [task] to [data].
   */
  fun setData(task: UTask, data: UTaskData)

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
 * Untyped wrapper for a task output, to distinguish a null output object from a non-existent output.
 */
typealias UOutput = Output<*>

/**
 * Attempts to cast untyped output to typed output.
 */
@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
inline fun <O : Out> UOutput.cast() = Output(this.output as O)


/**
 * Wrapper for task data: outputs and dependencies.
 */
data class TaskData<out O : Out>(val output: O, val taskReqs: ArrayList<TaskReq>, val fileReqs: ArrayList<FileReq>, val fileGens: ArrayList<FileGen>)

/**
 * Untyped wrapper for task data: outputs and dependencies.
 */
typealias UTaskData = TaskData<*>

/**
 * Attempts to cast untyped task data to typed task data.
 */
@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
inline fun <O : Out> UTaskData.cast() = this as TaskData<O>
