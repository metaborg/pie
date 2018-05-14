package mb.pie.runtime.core.impl.logger

import mb.pie.runtime.core.*
import mb.pie.runtime.core.impl.*
import java.io.OutputStream
import java.io.PrintWriter
import java.util.concurrent.atomic.AtomicInteger


open class StreamLogger(infoStream: OutputStream = System.out, traceStream: OutputStream? = System.out, private val descLimit: Int = 200) : Logger {
  private val infoWriter: PrintWriter = PrintWriter(infoStream, true)
  private val traceWriter: PrintWriter? = if(traceStream == null) null else PrintWriter(traceStream, true)
  private var indentation = AtomicInteger(0)
  private val indent get() = " ".repeat(indentation.get())


  override fun requireTopDownInitialStart(task: UTask) {}
  override fun requireTopDownInitialEnd(task: UTask, output: Out) {}

  override fun requireTopDownStart(task: UTask) {
    traceWriter?.println("${indent}v ${task.toShortString(descLimit)}")
    indentation.incrementAndGet()
  }

  override fun requireTopDownEnd(task: UTask, output: Out) {
    indentation.decrementAndGet()
    traceWriter?.println("$indent✔ ${task.toShortString(descLimit)} -> ${output.toString().toShortString(descLimit)}")
  }


  override fun requireBottomUpInitialStart(task: UTask) {}
  override fun requireBottomUpInitialEnd(task: UTask, output: Out) {}

  override fun requireBottomUpStart(task: UTask) {
    traceWriter?.println("$indent^ ${task.toShortString(descLimit)}")
    indentation.incrementAndGet()
  }

  override fun requireBottomUpEnd(task: UTask, output: Out) {
    indentation.decrementAndGet()
    traceWriter?.println("$indent✔ ${task.toShortString(descLimit)} -> ${output.toString().toShortString(descLimit)}")
  }


  override fun checkVisitedStart(task: UTask) {}
  override fun checkVisitedEnd(task: UTask, output: Out) {}


  override fun checkCachedStart(task: UTask) {}
  override fun checkCachedEnd(task: UTask, output: Out) {}


  override fun checkStoredStart(task: UTask) {}
  override fun checkStoredEnd(task: UTask, output: Out) {}


  override fun checkFileGenStart(task: UTask, fileGen: FileGen) {}

  override fun checkFileGenEnd(task: UTask, fileGen: FileGen, reason: InconsistentFileGen?) {
    if(reason != null) {
      traceWriter?.println("$indent␦ ${fileGen.file} (inconsistent: ${fileGen.stamp} vs ${reason.newStamp})")
    } else {
      traceWriter?.println("$indent␦ ${fileGen.file} (consistent: ${fileGen.stamp})")
    }
  }


  override fun checkFileReqStart(task: UTask, fileReq: FileReq) {}

  override fun checkFileReqEnd(task: UTask, fileReq: FileReq, reason: InconsistentFileReq?) {
    if(reason != null) {
      traceWriter?.println("$indent␦ ${fileReq.file} (inconsistent: ${fileReq.stamp} vs ${reason.newStamp})")
    } else {
      traceWriter?.println("$indent␦ ${fileReq.file} (consistent: ${fileReq.stamp})")
    }
  }


  override fun checkTaskReqStart(task: UTask, taskReq: TaskReq) {}

  override fun checkTaskReqEnd(task: UTask, taskReq: TaskReq, reason: InconsistentTaskReq?) {
    when(reason) {
      is InconsistentTaskReq ->
        traceWriter?.println("$indent␦ ${taskReq.callee.toShortString(descLimit)} (inconsistent: ${taskReq.stamp} vs ${reason.newStamp})")
      null ->
        traceWriter?.println("$indent␦ ${taskReq.callee.toShortString(descLimit)} (consistent: ${taskReq.stamp})")
    }
  }


  override fun executeStart(task: UTask, reason: ExecReason) {
    infoWriter.println("$indent> ${task.toShortString(descLimit)} (reason: $reason)")
  }

  override fun executeEnd(task: UTask, reason: ExecReason, data: UTaskData) {
    infoWriter.println("$indent< ${data.output.toString().toShortString(descLimit)}")
  }


  override fun invokeObserverStart(observer: Function<Unit>, task: UTask, output: Out) {
    infoWriter.println("$indent@ ${observer.toString().toShortString(50)}(${output.toString().toShortString(200)})")
  }

  override fun invokeObserverEnd(observer: Function<Unit>, task: UTask, output: Out) {}


  override fun error(message: String) {
    infoWriter.println("$indent$message")
  }

  override fun warn(message: String) {
    infoWriter.println("$indent$message")
  }

  override fun info(message: String) {
    infoWriter.println("$indent$message")
  }

  override fun trace(message: String) {
    traceWriter?.println("$indent$message")
  }
}
