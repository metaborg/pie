package mb.pie.runtime.store

import mb.pie.api.*
import mb.pie.vfs.path.PPath

/**
 * A build store that does not store anything and always returns null. For debugging or benchmarking purposes only.
 * DO NOT USE in production, as it makes the build algorithm inconsistent.
 */
class NoopStore : Store, StoreReadTxn, StoreWriteTxn {
  override fun readTxn() = this
  override fun writeTxn() = this
  override fun sync() {}
  override fun close() {}

  override fun output(key: TaskKey): Output<Out>? = null
  override fun taskReqs(key: TaskKey): List<TaskReq> = listOf()
  override fun callersOf(key: TaskKey): Set<TaskKey> = setOf()
  override fun fileReqs(key: TaskKey): List<FileReq> = listOf()
  override fun requireesOf(file: PPath): Set<TaskKey> = setOf()
  override fun fileGens(key: TaskKey): List<FileGen> = listOf()
  override fun generatorOf(file: PPath): TaskKey? = null
  override fun data(key: TaskKey): UTaskData? = null
  override fun numSourceFiles(): Int = 0

  override fun setOutput(key: TaskKey, output: Out) {}
  override fun setTaskReqs(key: TaskKey, taskReqs: ArrayList<TaskReq>) {}
  override fun setFileReqs(key: TaskKey, fileReqs: ArrayList<FileReq>) {}
  override fun setFileGens(key: TaskKey, fileGens: ArrayList<FileGen>) {}
  override fun setData(key: TaskKey, data: UTaskData) {}
  override fun drop() {}

  override fun toString() = "NoopStore"
}
