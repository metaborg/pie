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


  override fun requireInitialStart(app: UFuncApp) {}

  override fun requireInitialEnd(app: UFuncApp, info: UExecInfo) {}

  override fun requireStart(app: UFuncApp) {
    traceWriter?.println("$indent? ${app.toShortString(descLimit)}")
    indentation.incrementAndGet()
  }

  override fun requireEnd(app: UFuncApp, info: UExecInfo) {
    indentation.decrementAndGet()
    traceWriter?.println("$indent✔ ${app.toShortString(descLimit)}")
  }


  override fun checkConsistentStart(app: UFuncApp) {

  }

  override fun checkConsistentEnd(app: UFuncApp, result: UExecRes?) {

  }


  override fun checkCachedStart(app: UFuncApp) {

  }

  override fun checkCachedEnd(app: UFuncApp, result: UExecRes?) {

  }


  override fun checkStoredStart(app: UFuncApp) {

  }

  override fun checkStoredEnd(app: UFuncApp, result: UExecRes?) {

  }


  override fun checkGenStart(app: UFuncApp, gen: Gen) {

  }

  override fun checkGenEnd(app: UFuncApp, gen: Gen, reason: InconsistentGenPath?) {
    if(reason != null) {
      traceWriter?.println("$indent␦ ${gen.path} (inconsistent: ${gen.stamp} vs ${reason.newStamp})")
    } else {
      traceWriter?.println("$indent␦ ${gen.path} (consistent: ${gen.stamp})")
    }
  }


  override fun checkPathReqStart(app: UFuncApp, req: PathReq) {

  }

  override fun checkPathReqEnd(app: UFuncApp, req: PathReq, reason: InconsistentPathReq?) {
    if(reason != null) {
      traceWriter?.println("$indent␦ ${req.path} (inconsistent: ${req.stamp} vs ${reason.newStamp})")
    } else {
      traceWriter?.println("$indent␦ ${req.path} (consistent: ${req.stamp})")
    }
  }


  override fun checkBuildReqStart(app: UFuncApp, req: UExecReq) {

  }

  override fun checkBuildReqEnd(app: UFuncApp, req: UExecReq, reason: ExecReason?) {
    when(reason) {
      is InconsistentExecReq ->
        traceWriter?.println("$indent␦ ${req.app.toShortString(descLimit)} (inconsistent: ${req.stamp} vs ${reason.newStamp})")
      is InconsistentExecReqTransientOutput ->
        traceWriter?.println("$indent␦ ${req.app.toShortString(descLimit)} (inconsistent transient output: ${reason.inconsistentResult.toShortString(descLimit)})")
      null ->
        traceWriter?.println("$indent␦ ${req.app.toShortString(descLimit)} (consistent: ${req.stamp})")
    }
  }


  override fun rebuildStart(app: UFuncApp, reason: ExecReason) {
    infoWriter.println("$indent> ${app.toShortString(descLimit)} (reason: $reason)")
  }

  override fun rebuildEnd(app: UFuncApp, reason: ExecReason, result: UExecRes) {
    infoWriter.println("$indent< ${result.toShortString(descLimit)}")
  }
}