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


  override fun <I : In, O : Out> require(task: Task<I, O>): O {
    return require(task, defaultOutputStamper)
  }

  override fun <I : In, O : Out> require(task: Task<I, O>, stamper: OutputStamper): O {
    cancel.throwIfCancelled()
    val key = task.key()
    val output = requireTask.require(key, task, cancel)
    val stamp = stamper.stamp(output)
    taskReqs.add(TaskReq(key, stamp))
    Stats.addCallReq()
    return output
  }

  override fun <I : In, O : Out> require(taskDef: TaskDef<I, O>, input: I): O {
    return require(Task(taskDef, input), defaultOutputStamper)
  }

  override fun <I : In, O : Out> require(taskDef: TaskDef<I, O>, input: I, stamper: OutputStamper): O {
    return require(Task(taskDef, input), stamper)
  }

  override fun <I : In> require(task: STask<I>): Out {
    return require(task.toTask(taskDefs), defaultOutputStamper)
  }

  override fun <I : In> require(task: STask<I>, stamper: OutputStamper): Out {
    return require(task.toTask(taskDefs), stamper)
  }

  override fun <I : In> require(taskDefId: String, input: I): Out {
    val taskDef = getTaskDef<I>(taskDefId)
    return require(Task(taskDef, input), defaultOutputStamper)
  }

  override fun <I : In> require(taskDefId: String, input: I, stamper: OutputStamper): Out {
    val taskDef = getTaskDef<I>(taskDefId)
    return require(Task(taskDef, input), stamper)
  }

  private fun <I : In> getTaskDef(id: String) = taskDefs.getTaskDef<I, Out>(id)
    ?: throw RuntimeException("Cannot retrieve task with identifier $id, it cannot be found")


  override fun require(file: PPath) {
    return require(file, defaultFileReqStamper)
  }

  override fun require(file: PPath, stamper: FileStamper) {
    val stamp = stamper.stamp(file)
    fileReqs.add(FileReq(file, stamp))
    Stats.addFileReq()
  }

  override fun generate(file: PPath) {
    return generate(file, defaultFileGenStamper)
  }

  override fun generate(file: PPath, stamper: FileStamper) {
    val stamp = stamper.stamp(file)
    fileGens.add(FileGen(file, stamp))
    Stats.addFileGen()
  }


  data class Reqs(val taskReqs: ArrayList<TaskReq>, val fileReqs: ArrayList<FileReq>, val fileGens: ArrayList<FileGen>)

  fun reqs() = Reqs(taskReqs, fileReqs, fileGens)
}
