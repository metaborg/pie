package mb.pie.runtime.exec

import mb.pie.api.*
import mb.pie.api.exec.Cancelled
import mb.pie.api.exec.NullCancelled
import mb.pie.api.stamp.FileStamper
import mb.pie.api.stamp.OutputStamper
import mb.pie.api.stamp.output.InconsequentialOutputStamper
import mb.pie.vfs.path.PPath

interface RequireTask {
  fun <I : In, O : Out> require(key: TaskKey, task: Task<I, O>, cancel: Cancelled = NullCancelled()): O
}

internal class ExecContextImpl(
  override val logger: Logger,
  private val requireTask: RequireTask,
  private val defaultOutputStamper: OutputStamper,
  private val defaultFileReqStamper: FileStamper,
  private val defaultFileGenStamper: FileStamper,
  private val cancel: Cancelled
) : ExecContext {
  private val taskReqs = arrayListOf<TaskReq>()
  private val fileReqs = arrayListOf<FileReq>()
  private val fileGens = arrayListOf<FileGen>()


  override fun <I : In, O : Out> requireOutput(task: Task<I, O>, stamper: OutputStamper?): O {
    cancel.throwIfCancelled()
    val key = task.key()
    val output = requireTask.require(key, task, cancel)
    val stamp = (stamper ?: defaultOutputStamper).stamp(output)
    taskReqs.add(TaskReq(key, stamp))
    Stats.addCallReq()
    return output
  }

  override fun <I : In> requireExec(task: Task<I, *>) {
    cancel.throwIfCancelled()
    val key = task.key()
    val output = requireTask.require(key, task, cancel)
    val stamp = InconsequentialOutputStamper.instance.stamp(output)
    taskReqs.add(TaskReq(key, stamp))
    Stats.addCallReq()
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

