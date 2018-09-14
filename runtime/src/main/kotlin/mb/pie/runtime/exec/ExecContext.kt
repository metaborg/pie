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

  override fun <I : In> require(task: STask<I>, stamper: OutputStamper?): Out {
    // OPTO: toTask will incur a cast of the task definition, can that be avoided?
    return require(task.toTask(taskDefs), stamper)
  }

  override fun <I : In> require(taskDefId: String, input: I, stamper: OutputStamper?): Out {
    // OPTO: getTaskDef will incur a cast, can that be avoided?
    val taskDef = taskDefs.getTaskDef<I, Out>(taskDefId)
      ?: throw RuntimeException("Cannot execute task with identifier $taskDefId, it cannot be found")
    return require(Task(taskDef, input), stamper)
  }

  override fun require(key: TaskKey, stamper: OutputStamper?): Out {
    // OPTO: toTask will incur a cast of the task definition, can that be avoided?
    val task = store.readTxn().use { txn -> key.toTask(taskDefs, txn) }
    return require(task, stamper)
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

