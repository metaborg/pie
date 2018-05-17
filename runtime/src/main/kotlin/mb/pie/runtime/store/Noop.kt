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

  override fun output(task: UTask): Output<Out>? = null
  override fun taskReqs(task: UTask): List<TaskReq> = listOf()
  override fun callersOf(task: UTask): Set<UTask> = setOf()
  override fun fileReqs(task: UTask): List<FileReq> = listOf()
  override fun requireesOf(file: PPath): Set<UTask> = setOf()
  override fun fileGens(task: UTask): List<FileGen> = listOf()
  override fun generatorOf(file: PPath): UTask? = null
  override fun data(task: UTask): UTaskData? = null
  override fun numSourceFiles(): Int = 0

  override fun setOutput(task: UTask, output: Out) {}
  override fun setTaskReqs(task: UTask, taskReqs: ArrayList<TaskReq>) {}
  override fun setFileReqs(task: UTask, fileReqs: ArrayList<FileReq>) {}
  override fun setFileGens(task: UTask, fileGens: ArrayList<FileGen>) {}
  override fun setData(task: UTask, data: UTaskData) {}
  override fun drop() {}

  override fun toString() = "NoopStore"
}
