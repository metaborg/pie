package mb.pie.runtime.exec

import mb.pie.api.*
import mb.pie.api.exec.Cancelled
import mb.pie.api.stamp.FileStamper
import mb.pie.api.stamp.OutputStamper
import mb.pie.vfs.path.PPath

internal class ExecContextImpl(
  private val requireTask: RequireTask,
  private val cancel: Cancelled,
  private val taskDefs: TaskDefs,
  private val store: Store,
  private val defaultOutputStamper: OutputStamper,
  private val defaultFileReqStamper: FileStamper,
  private val defaultFileGenStamper: FileStamper,
  override val logger: Logger
) : ExecContext {
  private val taskReqs = arrayListOf<TaskReq>()
  private val fileReqs = arrayListOf<FileReq>()
  private val fileGens = arrayListOf<FileGen>()


  override fun <I : In, O : Out> require(task: Task<I, O>, stamper: OutputStamper?): O {
    cancel.throwIfCancelled()
    val key = task.key()
    val output = requireTask.require(key, task, cancel)
    val stamp = (stamper ?: defaultOutputStamper).stamp(output)
    taskReqs.add(TaskReq(key, stamp))
    Stats.addCallReq()
    return output
  }

  override fun <I : In, O : Out> require(taskDef: TaskDef<I, O>, input: I, stamper: OutputStamper?): O {
    return require(Task(taskDef, input), stamper)
  }

  override fun <I : In, O : Out> require(task: STask<I>, stamper: OutputStamper?): O {
    return require(task.toTask<O>(taskDefs), stamper)
  }

  override fun <I : In, O : Out> require(taskDefId: String, input: I, stamper: OutputStamper?): O {
    val taskDef = taskDefs.getTaskDef<I, O>(taskDefId)
      ?: throw RuntimeException("Cannot execute task with identifier $taskDefId, it cannot be found")
    return require(Task(taskDef, input), stamper)
  }

  override fun <O : Out> require(key: TaskKey, stamper: OutputStamper?): O {
    val task = store.readTxn().use { txn -> key.toTask(taskDefs, txn) }
    @Suppress("UNCHECKED_CAST")
    return require(task, stamper) as O
  }


  override fun require(file: PPath, stamper: FileStamper?) {
    val stamp = (stamper ?: defaultFileReqStamper).stamp(file)
    fileReqs.add(FileReq(file, stamp))
    Stats.addFileReq()
  }

  override fun generate(file: PPath, stamper: FileStamper?) {
    val stamp = (stamper ?: defaultFileGenStamper).stamp(file)
    fileGens.add(FileGen(file, stamp))
    Stats.addFileGen()
  }


  data class Reqs(val taskReqs: ArrayList<TaskReq>, val fileReqs: ArrayList<FileReq>, val fileGens: ArrayList<FileGen>)

  fun reqs() = Reqs(taskReqs, fileReqs, fileGens)
}

