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


  override fun requireTopDownInitialStart(app: UFuncApp) {}
  override fun requireTopDownInitialEnd(app: UFuncApp, result: UExecRes) {}

  override fun requireTopDownStart(app: UFuncApp) {
    traceWriter?.println("${indent}v ${app.toShortString(descLimit)}")
    indentation.incrementAndGet()
  }

  override fun requireTopDownEnd(app: UFuncApp, result: UExecRes) {
    indentation.decrementAndGet()
    traceWriter?.println("$indent✔ ${app.toShortString(descLimit)} -> ${result.toShortString(descLimit)}")
  }


  override fun requireBottomUpInitialStart(app: UFuncApp) {}
  override fun requireBottomUpInitialEnd(app: UFuncApp, result: UExecRes?) {}

  override fun requireBottomUpStart(app: UFuncApp) {
    traceWriter?.println("$indent^ ${app.toShortString(descLimit)}")
    indentation.incrementAndGet()
  }

  override fun requireBottomUpEnd(app: UFuncApp, result: UExecRes?) {
    indentation.decrementAndGet()
    traceWriter?.println("$indent✔ ${app.toShortString(descLimit)} -> ${result?.toShortString(descLimit)}")
  }


  override fun checkVisitedStart(app: UFuncApp) {}
  override fun checkVisitedEnd(app: UFuncApp, output: Out) {}


  override fun checkCachedStart(app: UFuncApp) {}
  override fun checkCachedEnd(app: UFuncApp, output: Out) {}


  override fun checkStoredStart(app: UFuncApp) {}
  override fun checkStoredEnd(app: UFuncApp, output: Out) {}


  override fun checkPathGenStart(app: UFuncApp, pathGen: PathGen) {}

  override fun checkPathGenEnd(app: UFuncApp, pathGen: PathGen, reason: InconsistentPathGen?) {
    if(reason != null) {
      traceWriter?.println("$indent␦ ${pathGen.path} (inconsistent: ${pathGen.stamp} vs ${reason.newStamp})")
    } else {
      traceWriter?.println("$indent␦ ${pathGen.path} (consistent: ${pathGen.stamp})")
    }
  }


  override fun checkPathReqStart(app: UFuncApp, req: PathReq) {}

  override fun checkPathReqEnd(app: UFuncApp, req: PathReq, reason: InconsistentPathReq?) {
    if(reason != null) {
      traceWriter?.println("$indent␦ ${req.path} (inconsistent: ${req.stamp} vs ${reason.newStamp})")
    } else {
      traceWriter?.println("$indent␦ ${req.path} (consistent: ${req.stamp})")
    }
  }


  override fun checkCallReqStart(app: UFuncApp, req: CallReq) {}

  override fun checkCallReqEnd(app: UFuncApp, req: CallReq, reason: InconsistentCallReq?) {
    when(reason) {
      is InconsistentCallReq ->
        traceWriter?.println("$indent␦ ${req.callee.toShortString(descLimit)} (inconsistent: ${req.stamp} vs ${reason.newStamp})")
//      is InconsistentExecReqTransientOutput ->
//        traceWriter?.println("$indent␦ ${req.callee.toShortString(descLimit)} (inconsistent transient output: ${reason.inconsistentResult.toShortString(descLimit)})")
      null ->
        traceWriter?.println("$indent␦ ${req.callee.toShortString(descLimit)} (consistent: ${req.stamp})")
    }
  }


  override fun executeStart(app: UFuncApp, reason: ExecReason) {
    infoWriter.println("$indent> ${app.toShortString(descLimit)} (reason: $reason)")
  }

  override fun executeEnd(app: UFuncApp, reason: ExecReason, result: UExecRes) {
    infoWriter.println("$indent< ${result.toShortString(descLimit)}")
  }


  override fun invokeObserverStart(observer: Function<Unit>, app: UFuncApp, output: Out) {
    infoWriter.println("$indent@ ${observer.toString().toShortString(50)}(${output.toString().toShortString(200)})")
  }

  override fun invokeObserverEnd(observer: Function<Unit>, app: UFuncApp, output: Out) {}


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