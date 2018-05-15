package mb.pie.runtime.impl

import mb.pie.runtime.*
import mb.pie.runtime.stamp.FileStamper
import mb.pie.runtime.stamp.OutputStamper
import mb.util.async.Cancelled
import mb.util.async.NullCancelled
import mb.vfs.path.PPath


interface RequireTask {
  fun <I : In, O : Out> require(task: Task<I, O>, cancel: Cancelled = NullCancelled()): O
}

internal class ExecContextImpl(
  private val requireTask: RequireTask,
  private val cancel: Cancelled
) : ExecContext {
  private val taskReqs = arrayListOf<TaskReq>()
  private val fileReqs = arrayListOf<FileReq>()
  private val fileGens = arrayListOf<FileGen>()


  override fun <I : In, O : Out> requireOutput(task: Task<I, O>, stamper: OutputStamper): O {
    cancel.throwIfCancelled()
    val output = requireTask.require(task, cancel)
    val stamp = stamper.stamp(output)
    taskReqs.add(TaskReq(task, stamp))
    Stats.addCallReq()
    return output
  }

  override fun requireExec(task: UTask, stamper: OutputStamper) {
    cancel.throwIfCancelled()
    val output = requireTask.require(task, cancel)
    val stamp = stamper.stamp(output)
    taskReqs.add(TaskReq(task, stamp))
    Stats.addCallReq()
  }


  override fun require(file: PPath, stamper: FileStamper) {
    val stamp = stamper.stamp(file)
    fileReqs.add(FileReq(file, stamp))
    Stats.addFileReq()
  }

  override fun generate(file: PPath, stamper: FileStamper) {
    val stamp = stamper.stamp(file)
    fileGens.add(FileGen(file, stamp))
    Stats.addFileGen()
  }


  data class Reqs(val taskReqs: ArrayList<TaskReq>, val fileReqs: ArrayList<FileReq>, val fileGens: ArrayList<FileGen>)

  fun reqs() = Reqs(taskReqs, fileReqs, fileGens)
}

